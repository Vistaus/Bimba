package ml.adamsprogs.bimba.models

import org.onebusaway.gtfs.model.AgencyAndId
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

data class Departure(val line: AgencyAndId, val mode: List<Int>, val time: Int, val lowFloor: Boolean, //time in seconds since midnight
                     val modification: List<String>, val direction: String, val vm: Boolean = false,
                     var tomorrow: Boolean = false, val onStop: Boolean = false) {

    val isModified: Boolean
        get() {
            return modification.isNotEmpty()
        }

    override fun toString(): String {
        return "$line|$mode|$time|$lowFloor|$modification|$direction|$vm|$tomorrow|$onStop"
    }

    fun copy(): Departure {
        return Departure.fromString(this.toString())
    }

    companion object {
        private fun filterDepartures(departures: List<Departure>): ArrayList<Departure> {
            val filtered = ArrayList<Departure>()
            val lines = HashMap<String, Int>()
            val sortedDepartures = departures.sortedBy { it.timeTill() }
            for (departure in sortedDepartures) {
                var lineExistedTimes = lines[departure.line]
                if (departure.timeTill() >= 0 && lineExistedTimes ?: 0 < 3) {
                    lineExistedTimes = (lineExistedTimes ?: 0) + 1
                    lines[departure.line] = lineExistedTimes
                    filtered.add(departure)
                }
            }
            return filtered
        }

        fun createDepartures(stopId: String): Map<String, List<Departure>> {
            val timetable = Timetable.getTimetable()
            val departures = timetable.getStopDepartures(stopId)
            return createDepartures(departures)
        }

        fun createDepartures(departures: Map<String, List<Departure>>): Map<String, List<Departure>> { //todo if departure.timeTill < 0 -> show ‘just departed’
            val moreDepartures = HashMap<String, ArrayList<Departure>>()
            for ((k, v) in departures) {
                moreDepartures[k] = ArrayList()
                for (departure in v)
                    moreDepartures[k]!!.add(departure.copy())
            }
            val rolledDepartures = HashMap<String, ArrayList<Departure>>()

            for ((_, tomorrowDepartures) in moreDepartures) {
                tomorrowDepartures.forEach { it.tomorrow = true }
            }

            for ((mode, _) in departures) {
                rolledDepartures[mode] = (departures[mode] as ArrayList<Departure> +
                        moreDepartures[mode] as ArrayList<Departure>) as ArrayList<Departure>
                rolledDepartures[mode] = filterDepartures(rolledDepartures[mode]!!)
            }

            return rolledDepartures
        }

        fun fromString(string: String): Departure {
            val array = string.split("|")
            if (array.size != 9)
                throw IllegalArgumentException()
            return Departure(array[0], array[1], array[2], array[3] == "true", array[4], array[5],
                    array[6] == "true", array[7] == "true", array[8] == "true")
        }
    }

    fun timeTill(): Long {
        val time = Calendar.getInstance()
        time.set(Calendar.HOUR_OF_DAY, Integer.parseInt(this.time.split(":")[0]))
        time.set(Calendar.MINUTE, Integer.parseInt(this.time.split(":")[1]))
        time.set(Calendar.SECOND, 0)
        time.set(Calendar.MILLISECOND, 0)
        val now = Calendar.getInstance()
        if (this.tomorrow)
            time.add(Calendar.DAY_OF_MONTH, 1)
        return (time.timeInMillis - now.timeInMillis) / (1000 * 60)
    }
}
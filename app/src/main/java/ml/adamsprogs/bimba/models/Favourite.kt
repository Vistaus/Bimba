package ml.adamsprogs.bimba.models

import android.os.Parcel
import android.os.Parcelable
import ml.adamsprogs.bimba.MessageReceiver
import ml.adamsprogs.bimba.getMode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class Favourite : Parcelable, MessageReceiver.OnVmListener {
    private var isRegisteredOnVmListener: Boolean = false
    var name: String
        private set
    var timetables: HashSet<Plate>
        private set
    private val vmDeparturesMap = HashMap<String, ArrayList<Departure>>()
    private var vmDepartures = ArrayList<Departure>()
    val timetable = Timetable.getTimetable()
    val size: Int
        get() = timetables.size

    constructor(parcel: Parcel) {
        val array = ArrayList<String>()
        parcel.readStringList(array)
        val timetables = HashSet<Plate>()
        array.mapTo(timetables) { Plate.fromString(it) }
        this.name = parcel.readString()
        this.timetables = timetables
    }

    constructor(name: String, timetables: HashSet<Plate>) {
        this.name = name
        this.timetables = timetables

    }

    override fun describeContents(): Int {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        val parcel = timetables.map { it.toString() }
        dest?.writeStringList(parcel)
        dest?.writeString(name)
    }

    private fun filterVmDepartures() {
        this.vmDepartures
                .filter { it.timeTill() < 0 }
                .forEach { this.vmDepartures.remove(it) }
    }

    fun delete(plate: Plate) {
        timetables.remove(timetables.find { it.stop == plate.stop && it.line == plate.line })
    }

    fun registerOnVm(receiver: MessageReceiver) {
        if (!isRegisteredOnVmListener) {
            receiver.addOnVmListener(this)
            isRegisteredOnVmListener = true
        }
    }

    fun rename(newName: String) {
        name = newName
    }

    companion object CREATOR : Parcelable.Creator<Favourite> {
        override fun createFromParcel(parcel: Parcel): Favourite {
            return Favourite(parcel)
        }

        override fun newArray(size: Int): Array<Favourite?> {
            return arrayOfNulls(size)
        }
    }

    fun nextDeparture(): Departure? {
        filterVmDepartures()
        if (timetables.isEmpty() && vmDepartures.isEmpty())
            return null

        if (vmDepartures.isNotEmpty()) {
            return vmDepartures.minBy { it.timeTill() }
        }

        val today = Calendar.getInstance().getMode()
        val tomorrowCal = Calendar.getInstance()
        tomorrowCal.add(Calendar.DAY_OF_MONTH, 1)
        val tomorrow = tomorrowCal.getMode()

        val departures = timetable.getStopDepartures(timetables)
        val todayDepartures = departures[today]!!
        val tomorrowDepartures = ArrayList<Departure>()
        val twoDayDepartures = ArrayList<Departure>()
        departures[tomorrow]!!.mapTo(tomorrowDepartures) {it.copy()}
        tomorrowDepartures.forEach {it.tomorrow = true}

        todayDepartures.forEach {twoDayDepartures.add(it)}
        tomorrowDepartures.forEach {twoDayDepartures.add(it)}

        if (twoDayDepartures.isEmpty())
            return null

        return twoDayDepartures
                .filter { it.timeTill() >= 0 }
                .minBy { it.timeTill() }
    }

    fun allDepartures(): HashMap<String, ArrayList<Departure>>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun fullTimetable(): HashMap<String, ArrayList<Departure>>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onVm(vmDepartures: ArrayList<Departure>?, requester: String) {
        val requesterName = requester.split(";")[0]
        val requesterTimetable: String = try {
            requester.split(";")[1]
        } catch (e: IndexOutOfBoundsException) {
            ""
        }
        if (vmDepartures != null && requesterName == name) {
            vmDeparturesMap[requesterTimetable] = vmDepartures
            this.vmDepartures = vmDeparturesMap.flatMap { it.value } as ArrayList<Departure>
        }
        filterVmDepartures()
    }
}

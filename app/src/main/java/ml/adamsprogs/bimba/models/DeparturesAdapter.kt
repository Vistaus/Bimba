package ml.adamsprogs.bimba.models

import android.content.Context
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ml.adamsprogs.bimba.R
import android.view.LayoutInflater
import java.util.*

class DeparturesAdapter(val context: Context, val departures: List<Departure>) :
        RecyclerView.Adapter<DeparturesAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return departures.size
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val departure = departures[position]
        val now = Calendar.getInstance()
        val departureTime = Calendar.getInstance()
        departureTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(departure.time.split(":")[0]))
        departureTime.set(Calendar.MINUTE, Integer.parseInt(departure.time.split(":")[1]))
        val departureIn = (departureTime.timeInMillis - now.timeInMillis) / (1000 * 60)
        val departureTimeShown: String
        val timeString: Int

        if (departureIn > 60) {
            timeString = R.string.departure_at
            departureTimeShown = departure.time
        } else {
            timeString = R.string.departure_in
            departureTimeShown = "$departureIn"
        }

        val line = holder?.lineTextView
        line?.text = departure.line
        val time = holder?.timeTextView
        time?.text = context.getString(timeString, departureTimeShown)
        val direction = holder?.directionTextView
        direction?.text = context.getString(R.string.departure_to, departure.direction)
        val icon = holder?.typeIcon
        if (departure.vm)
            icon?.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_vm, null))
        else
            icon?.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_timetable, null))
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val context = parent?.context
        val inflater = LayoutInflater.from(context)

        val contactView = inflater.inflate(R.layout.departure_row, parent, false)
        val viewHolder = ViewHolder(contactView)
        return viewHolder
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lineTextView: TextView = itemView.findViewById(R.id.lineNumber) as TextView
        val timeTextView: TextView = itemView.findViewById(R.id.departureTime) as TextView
        val directionTextView: TextView = itemView.findViewById(R.id.departureDirection) as TextView
        val typeIcon: ImageView = itemView.findViewById(R.id.departureTypeIcon) as ImageView
    }
}
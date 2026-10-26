package com.example.g4sjavelin.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.g4sjavelin.R
import com.example.g4sjavelin.data.Shift
import com.example.g4sjavelin.notifications.ShiftReminderReceiver
import java.text.SimpleDateFormat
import java.util.*

class ShiftAdapter(
    private var shifts: List<Shift>,
    private val isBookingScreen: Boolean,
    private val onBookClicked: (Shift) -> Unit,
    private val onQrClicked: (Shift) -> Unit // ✅ new callback
) : RecyclerView.Adapter<ShiftAdapter.ShiftViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShiftViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shift, parent, false)
        return ShiftViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShiftViewHolder, position: Int) {
        val shift = shifts[position]
        holder.bind(shift)
    }

    override fun getItemCount(): Int = shifts.size

    fun updateData(newShifts: List<Shift>) {
        shifts = newShifts
        notifyDataSetChanged()
    }

    inner class ShiftViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.textTitle)
        private val location: TextView = itemView.findViewById(R.id.textLocation)
        private val time: TextView = itemView.findViewById(R.id.textTime)
        private val bookButton: Button = itemView.findViewById(R.id.buttonBook)
        private val qrButton: Button = itemView.findViewById(R.id.buttonViewQR) // ✅ new button in layout

        fun bind(shift: Shift) {
            title.text = shift.title
            location.text = shift.location
            time.text = "${shift.date} | ${shift.startTime} - ${shift.endTime}"

            // ✅ Toggle Book/Cancel based on state
            if (shift.isBooked) {
                bookButton.text = "Cancel"
                bookButton.setBackgroundColor(itemView.context.getColor(android.R.color.holo_red_dark))

                // Show QR button only if booked
                qrButton.visibility = View.VISIBLE
                qrButton.setOnClickListener { onQrClicked(shift) }
            } else {
                bookButton.text = "Book"
                bookButton.setBackgroundColor(itemView.context.getColor(R.color.purple_500))

                qrButton.visibility = View.GONE
            }

            bookButton.setOnClickListener {
                // Call back to Activity/VM
                onBookClicked(shift)

                // If booking → schedule reminder
                if (!shift.isBooked) {
                    scheduleReminder(itemView.context, shift)
                }
            }

            // ✅ Location click → open Google Maps
            location.setOnClickListener {
                val gmmIntentUri = Uri.parse("geo:0,0?q=${shift.location}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")

                try {
                    itemView.context.startActivity(mapIntent)
                } catch (e: Exception) {
                    Toast.makeText(itemView.context, "Google Maps not installed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // ✅ Function to schedule reminder
        private fun scheduleReminder(context: Context, shift: Shift) {
            try {
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val shiftDateTime = formatter.parse("${shift.date} ${shift.startTime}")

                shiftDateTime?.let {
                    val triggerTime = it.time - (60 * 60 * 1000) // 1 hour before

                    val intent = Intent(context, ShiftReminderReceiver::class.java).apply {
                        putExtra("title", shift.title)
                        putExtra("time", "${shift.startTime} - ${shift.endTime}")
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        shift.id,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)

                    Toast.makeText(context, "Reminder set 1 hour before shift", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to schedule reminder", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

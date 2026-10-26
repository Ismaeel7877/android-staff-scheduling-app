package com.example.g4sjavelin.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.g4sjavelin.R

class ShiftReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 🔹 Debug Toast so you know the broadcast fired
        Toast.makeText(context, "Shift reminder received!", Toast.LENGTH_LONG).show()

        val title = intent.getStringExtra("title") ?: "Shift Reminder"
        val time = intent.getStringExtra("time") ?: ""

        val notification = NotificationCompat.Builder(context, "shift_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Upcoming Shift")
            .setContentText("$title at $time")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // dismiss when tapped
            .build()

        // 🔹 Check POST_NOTIFICATIONS permission (Android 13+)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        ) {
            with(NotificationManagerCompat.from(context)) {
                notify(System.currentTimeMillis().toInt(), notification)
            }
        } else {
            Toast.makeText(context, "Notification permission not granted!", Toast.LENGTH_SHORT).show()
        }
    }
}

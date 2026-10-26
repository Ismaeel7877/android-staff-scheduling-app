package com.example.g4sjavelin.ui

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.g4sjavelin.R
import com.example.g4sjavelin.data.Shift
import com.example.g4sjavelin.notifications.ShiftReminderReceiver
import com.example.g4sjavelin.viewmodel.ShiftViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.io.FileWriter
import java.util.*

class MyBookingsActivity : ComponentActivity() {

    private val shiftViewModel: ShiftViewModel by viewModels()
    private lateinit var adapter: ShiftAdapter
    private lateinit var exportButton: Button

    private var currentBookings: List<Shift> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        // 🔹 Create Notification Channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "shift_channel",
                "Shift Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for your booked shifts"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        // 🔹 Request Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    2
                )
            }
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewBookings)
        recyclerView.layoutManager = LinearLayoutManager(this)

        exportButton = findViewById(R.id.buttonExportBookings)

        adapter = ShiftAdapter(
            shifts = emptyList(),
            isBookingScreen = true,
            onBookClicked = { shift ->
                val updatedShift = shift.copy(isBooked = !shift.isBooked)
                shiftViewModel.update(updatedShift)

                if (updatedShift.isBooked) {
                    scheduleReminder(this, updatedShift)
                }
            },
            onQrClicked = { shift ->
                // Launch QR Code activity
                val intent = Intent(this, QrCodeActivity::class.java)
                // Pass all shift details for the QR
                val qrData = "Title: ${shift.title}\nDate: ${shift.date}\nStart: ${shift.startTime}\nEnd: ${shift.endTime}\nLocation: ${shift.location}"
                intent.putExtra("QR_DATA", qrData)
                startActivity(intent)
            }
        )
        recyclerView.adapter = adapter

        shiftViewModel.bookedShifts.observe(this) { bookings ->
            currentBookings = bookings
            adapter.updateData(bookings)
        }

        exportButton.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        1
                    )
                } else {
                    exportShifts()
                }
            } else {
                exportShifts()
            }
        }
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

// Optional: Highlight the current menu item
        bottomNav.selectedItemId = R.id.nav_bookings // Use the correct ID from menu_bottom_nav
        // ✅ Bottom Navigation
        findViewById<BottomNavigationView>(R.id.bottomNavigation).setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_bookings ->true
                    R.id.nav_home -> {   startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    // 🔹 Export Booked Shifts to CSV in Downloads
    private fun exportShifts() {
        if (currentBookings.isEmpty()) {
            Toast.makeText(this, "No booked shifts to export.", Toast.LENGTH_SHORT).show()
            return
        }

        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()

        val csvFile = File(downloadsDir, "BookedShifts.csv")
        val writer = FileWriter(csvFile)

        writer.append("Title,Location,Date,Start Time,End Time\n")
        for (shift in currentBookings) {
            writer.append("${shift.title},${shift.location},${shift.date},${shift.startTime},${shift.endTime}\n")
        }
        writer.flush()
        writer.close()

        Toast.makeText(this, "Exported to Downloads/BookedShifts.csv", Toast.LENGTH_LONG).show()
    }

    // 🔹 Schedule Reminder Notification
    private fun scheduleReminder(context: Context, shift: Shift) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Notification permission not granted!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ShiftReminderReceiver::class.java).apply {
                putExtra("title", shift.title)
                putExtra("time", "${shift.date} ${shift.startTime}")
            }

            val requestCode = shift.hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val shiftCalendar = Calendar.getInstance().apply {
                val dateTimeString = "${shift.date} ${shift.startTime}"
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                time = sdf.parse(dateTimeString) ?: Date()
                add(Calendar.SECOND, 10) // For testing
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                shiftCalendar.timeInMillis,
                pendingIntent
            )

            Toast.makeText(
                context,
                "Reminder set for shift: ${shiftCalendar.time}",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(context, "SecurityException: ${e.message}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                "Failed to schedule reminder: ${e.javaClass.simpleName} - ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

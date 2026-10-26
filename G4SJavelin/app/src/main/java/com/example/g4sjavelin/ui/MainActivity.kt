package com.example.g4sjavelin.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.g4sjavelin.R
import com.example.g4sjavelin.viewmodel.ShiftViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private val shiftViewModel: ShiftViewModel by viewModels()
    private lateinit var adapter: ShiftAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // ✅ If user is not logged in → go to LoginActivity
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        // ✅ Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewShifts).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        adapter = ShiftAdapter(
            shifts = emptyList(),
            isBookingScreen = false,
            onBookClicked = { shift ->
                val updatedShift = shift.copy(isBooked = !shift.isBooked)
                shiftViewModel.update(updatedShift)
            },
            onQrClicked = { shift ->
                // Open QR code activity
                val intent = Intent(this, QrCodeActivity::class.java).apply {
                    putExtra("shiftId", shift.id)
                    putExtra("shiftTitle", shift.title)
                }
                startActivity(intent)
            }
        )
        recyclerView.adapter = adapter

        // ✅ Observe shifts
        shiftViewModel.allShifts.observe(this) { shifts ->
            adapter.updateData(shifts)
        }

        // ✅ Logout button
        findViewById<Button>(R.id.buttonLogout).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }



        // ✅ Announcements button
        findViewById<Button>(R.id.buttonAnnouncements).setOnClickListener {
            startActivity(Intent(this, AnnouncementsActivity::class.java))
        }

        // ✅ Bottom Navigation
        findViewById<BottomNavigationView>(R.id.bottomNavigation).setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_bookings -> {
                    startActivity(Intent(this, MyBookingsActivity::class.java))
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
}

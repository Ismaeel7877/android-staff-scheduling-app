package com.example.g4sjavelin.ui

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.g4sjavelin.R
import com.example.g4sjavelin.network.LocalApiService

class AnnouncementsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcements)

        val announcementsText = findViewById<TextView>(R.id.textAnnouncements)

        val api = LocalApiService(this)
        val announcements = api.getAnnouncements()

        announcementsText.text = announcements.joinToString("\n\n") {
            "📢 ${it.title}\n${it.message}"
        }
    }
}

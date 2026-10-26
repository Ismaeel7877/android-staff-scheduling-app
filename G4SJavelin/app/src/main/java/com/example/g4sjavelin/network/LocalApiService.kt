package com.example.g4sjavelin.network

import android.content.Context
import com.example.g4sjavelin.data.Announcement
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocalApiService(private val context: Context) {

    fun getAnnouncements(): List<Announcement> {
        val json = context.assets.open("announcements.json")
            .bufferedReader()
            .use { it.readText() }

        val listType = object : TypeToken<List<Announcement>>() {}.type
        return Gson().fromJson(json, listType)
    }
}

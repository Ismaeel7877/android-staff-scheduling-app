package com.example.g4sjavelin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shifts")
data class Shift(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val location: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val isBooked: Boolean = false
)

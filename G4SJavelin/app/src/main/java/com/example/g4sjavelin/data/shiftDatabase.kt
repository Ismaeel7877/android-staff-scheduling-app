package com.example.g4sjavelin.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Shift::class], version = 1, exportSchema = false)
abstract class ShiftDatabase : RoomDatabase() {

    abstract fun shiftDao(): ShiftDao

    companion object {
        @Volatile
        private var INSTANCE: ShiftDatabase? = null

        fun getDatabase(context: Context): ShiftDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShiftDatabase::class.java,
                    "shift_database"
                )
                    .addCallback(ShiftDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class ShiftDatabaseCallback : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = database.shiftDao()
                    // Clear old data first
                    dao.clearAll()
                    // Insert fresh shifts
                    populateDatabase(dao)
                }
            }
        }

        private suspend fun populateDatabase(shiftDao: ShiftDao) {
            val shifts = listOf(
                Shift(title = "Man Utd vs Sunderland", location = "Manchester United Football Club", date = "2025-10-04", startTime = "11:45", endTime = "17:30"),
                Shift(title = "Burnley FC vs Leeds Unt", location = "Burnley Football Club", date = "2025-10-18", startTime = "12:00", endTime = "17:15"),
                Shift(title = "Burnley FC vs Arsenal", location = "Burnley Football Club", date = "2025-11-01", startTime = "12:00", endTime = "17:30"),
                Shift(title = "Sale Shark v Gloucester Rugby", location = "The CITY OF SALFORD STADIUM", date = "2025-11-02", startTime = "12:30", endTime = "17:30"),
                Shift(title = "Burnley FC vs Man Utd", location = "Burnley Football Club", date = "2025-09-28", startTime = "22:21", endTime = "24:30"),

            Shift(title = "Burnley FC vs Chelsea", location = "Burnley Football Club", date = "2025-11-22", startTime = "10:00", endTime = "15:30")
            )
            shifts.forEach { shiftDao.insert(it) }
        }
    }
}
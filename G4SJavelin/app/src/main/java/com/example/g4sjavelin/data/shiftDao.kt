package com.example.g4sjavelin.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ShiftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shift: Shift)

    @Update
    suspend fun update(shift: Shift)

    @Delete
    suspend fun delete(shift: Shift)

    @Query("SELECT * FROM shifts ORDER BY date ASC")
    fun getAllShifts(): LiveData<List<Shift>>

    @Query("SELECT * FROM shifts WHERE isBooked = 1")
    fun getBookedShifts(): LiveData<List<Shift>>

    // 👇 Non-LiveData version (for seeding checks)
    @Query("SELECT * FROM shifts")
    suspend fun getAllShiftsNow(): List<Shift>

    @Query("DELETE FROM shifts")
    suspend fun clearAll()

}

package com.example.g4sjavelin.data

import androidx.lifecycle.LiveData

class ShiftRepository(private val shiftDao: ShiftDao) {
    val allShifts: LiveData<List<Shift>> = shiftDao.getAllShifts()
    val bookedShifts: LiveData<List<Shift>> = shiftDao.getBookedShifts()

    suspend fun insert(shift: Shift) {
        shiftDao.insert(shift)
    }

    suspend fun update(shift: Shift) {
        shiftDao.update(shift)
    }

    suspend fun delete(shift: Shift) {
        shiftDao.delete(shift)
    }
}

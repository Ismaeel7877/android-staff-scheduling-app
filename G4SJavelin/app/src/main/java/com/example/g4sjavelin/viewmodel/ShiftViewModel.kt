package com.example.g4sjavelin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.g4sjavelin.data.Shift
import com.example.g4sjavelin.data.ShiftDatabase
import kotlinx.coroutines.launch

class ShiftViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ShiftDatabase.getDatabase(application).shiftDao()
    val allShifts: LiveData<List<Shift>> = dao.getAllShifts()
    val bookedShifts: LiveData<List<Shift>> = dao.getBookedShifts()

    fun insert(shift: Shift) {
        viewModelScope.launch {
            dao.insert(shift)
        }
    }

    fun update(shift: Shift) {
        viewModelScope.launch {
            dao.update(shift)
        }
    }

    fun delete(shift: Shift) {
        viewModelScope.launch {
            dao.delete(shift)
        }
    }
}

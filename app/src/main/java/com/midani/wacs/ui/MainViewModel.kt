package com.midani.wacs.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.midani.wacs.data.WacsDatabase
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = WacsDatabase.getInstance(application)
    private val dao = database.detectedNumberDao()
    
    val pendingNumbers = dao.getPending()
    val pendingCount = dao.getPendingCount()
    
    fun markAsSaved(id: Long, name: String) {
        viewModelScope.launch {
            dao.markAsSaved(id, name)
        }
    }
    
    fun ignoreNumber(id: Long) {
        viewModelScope.launch {
            dao.markAsIgnored(id)
        }
    }
    
    fun deleteNumber(id: Long) {
        viewModelScope.launch {
            dao.delete(id)
        }
    }
}

package com.application.fitbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.fitbuddy.models.SpotLog
import com.application.fitbuddy.repository.SpotLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SpotLogViewModel @Inject constructor(private val repository: SpotLogRepository) : ViewModel() {

    suspend fun insert(spotLog: SpotLog, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                repository.insert(spotLog)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Failed to insert spot log")
            }
        }
    }

    fun update(spotLog: SpotLog, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.update(spotLog)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Failed to update spot log")
            }
        }
    }

    fun delete(spotLog: SpotLog, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.delete(spotLog)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Failed to delete spot log")
            }
        }
    }

    fun getLogsForSpot(spotId: Int, onSuccess: (List<SpotLog>) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val logs = repository.getLogsForSpot(spotId)
                onSuccess(logs)
            } catch (e: Exception) {
                onFailure(e.message ?: "Unknown error")
            }
        }
    }
}

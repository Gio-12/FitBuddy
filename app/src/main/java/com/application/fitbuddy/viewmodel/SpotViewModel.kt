package com.application.fitbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.fitbuddy.models.Spot
import com.application.fitbuddy.repository.SpotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SpotViewModel @Inject constructor(private val repository: SpotRepository) : ViewModel() {

    suspend fun insert(spot: Spot, onSuccess: (Long) -> Unit, onFailure: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val spotId = repository.insert(spot)
                onSuccess(spotId)
            } catch (e: Exception) {
                onFailure(e.message ?: "Failed to insert spot")
            }
        }
    }

    fun update(spot: Spot, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.update(spot)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Failed to update spot")
            }
        }
    }

    fun delete(spot: Spot, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.delete(spot)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Failed to delete spot")
            }
        }
    }

    fun getSpotsForUser(userUsername: String, onSuccess: (List<Spot>) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val spots = repository.getSpotsForUser(userUsername)
                onSuccess(spots)
            } catch (e: Exception) {
                onFailure(e.message ?: "Unknown error")
            }
        }
    }
}

package com.application.fitbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.fitbuddy.models.Action
import com.application.fitbuddy.repository.ActionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ActionViewModel @Inject constructor(private val repository: ActionRepository) : ViewModel() {

    suspend fun insert(action: Action, onSuccess: (Long) -> Unit, onFailure: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val actionId = repository.insert(action)
                onSuccess(actionId)
            } catch (e: Exception) {
                onFailure(e.message ?: "Failed to insert action")
            }
        }
    }

    fun update(action: Action, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.update(action)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Failed to update action")
            }
        }
    }

    fun delete(action: Action, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.delete(action)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Failed to delete action")
            }
        }
    }

    fun getActionById(actionId: Long, onSuccess: (Action?) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val action = repository.getActionById(actionId)
                onSuccess(action)
            } catch (e: Exception) {
                onFailure(e.message ?: "Unknown error")
            }
        }
    }

    fun getActionsForUser(userUsername: String, onSuccess: (List<Action>) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val actions = repository.getActionsForUser(userUsername)
                onSuccess(actions)
            } catch (e: Exception) {
                onFailure(e.message ?: "Unknown error")
            }
        }
    }

    fun getActionsForPeriod(username: String, startTime: Long, endTime: Long, onSuccess: (List<Action>) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val actions = repository.getActionsForPeriod(username, startTime, endTime)
                onSuccess(actions)
            } catch (e: Exception) {
                onFailure(e.message ?: "Unknown error")
            }
        }
    }
}

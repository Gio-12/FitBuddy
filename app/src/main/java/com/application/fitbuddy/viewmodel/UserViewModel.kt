package com.application.fitbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.fitbuddy.models.User
import com.application.fitbuddy.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val repository: UserRepository) : ViewModel() {

    fun insert(user: User, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insert(user)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Failed to insert user")
            }
        }
    }

    fun update(user: User, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.update(user)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Failed to update user")
            }
        }
    }

    fun delete(user: User, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.delete(user)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Failed to delete user")
            }
        }
    }

    fun getUserByUsername(username: String, onSuccess: (User?) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val user = repository.getUserByUsername(username)
                onSuccess(user)
            } catch (e: Exception) {
                onFailure(e.message ?: "Unknown error")
            }
        }
    }

    fun getUserWithPassword(username: String, password: String, onSuccess: (User?) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val user = repository.getUserWithPassword(username, password)
                onSuccess(user)
            } catch (e: Exception) {
                onFailure(e.message ?: "Unknown error")
            }
        }
    }

    fun searchUsers(query: String, onSuccess: (List<String>) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val users = repository.searchUsers(query)
                onSuccess(users)
            } catch (e: Exception) {
                onFailure(e.message ?: "Unknown error")
            }
        }
    }

    fun getAllUsers(onSuccess: (List<String>) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val users = repository.getAllUsers()
                onSuccess(users)
            } catch (e: Exception) {
                onFailure(e.message ?: "Unknown error")
            }
        }
    }
}

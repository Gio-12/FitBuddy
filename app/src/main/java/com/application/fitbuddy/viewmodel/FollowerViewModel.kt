package com.application.fitbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.fitbuddy.models.Follower
import com.application.fitbuddy.repository.FollowerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FollowerViewModel @Inject constructor(private val repository: FollowerRepository) : ViewModel() {

    suspend fun insert(follower: Follower, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                repository.insert(follower)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Failed to insert follower")
            }
        }
    }

    fun update(follower: Follower, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.update(follower)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Failed to update follower")
            }
        }
    }

    fun delete(follower: Follower, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.delete(follower)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Failed to delete follower")
            }
        }
    }

    fun removeFollowing(userFK: String, followerFK: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.removeFollowing(userFK, followerFK)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Failed to remove following")
            }
        }
    }

    fun getFollowerById(followerId: Long, onSuccess: (Follower?) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val follower = repository.getFollowerById(followerId)
                onSuccess(follower)
            } catch (e: Exception) {
                onFailure(e.message ?: "Unknown error")
            }
        }
    }

    fun getFollowersForUser(userFK: String, onSuccess: (List<String>) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val followers = repository.getFollowersForUser(userFK)
                onSuccess(followers)
            } catch (e: Exception) {
                onFailure(e.message ?: "Unknown error")
            }
        }
    }

    fun getFollowingForUser(followerFK: String, onSuccess: (List<String>) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val followers = repository.getFollowingForUser(followerFK)
                onSuccess(followers)
            } catch (e: Exception) {
                onFailure(e.message ?: "Unknown error")
            }
        }
    }
}

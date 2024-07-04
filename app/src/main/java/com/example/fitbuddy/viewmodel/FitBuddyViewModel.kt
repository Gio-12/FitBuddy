package com.example.fitbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitbuddy.repository.FitBuddyRepository
import com.example.fitbuddy.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FitBuddyViewModel @Inject constructor(
    private val repository: FitBuddyRepository
) : ViewModel() {

    //ACTIONS

    suspend fun insertAction(action: Action): Long {
        return withContext(Dispatchers.IO) {
            repository.insertAction(action)
        }
    }

    suspend fun updateAction(action: Action) {
        viewModelScope.launch {
            repository.updateAction(action)
        }
    }

    suspend fun getActionsForUser(userUsername: String) {
        viewModelScope.launch {
            repository.getActionsForUser(userUsername)
        }
    }

    //FOLLOWER

    suspend fun insertFollower(follower: Follower) {
        viewModelScope.launch {
            repository.insertFollower(follower)
        }
    }

    suspend fun getFollowersForUser(userFK: String) {
        viewModelScope.launch {
            repository.getFollowersForUser(userFK)
        }
    }

    //SPOT

    suspend fun insertSpot(spot: Spot) {
        viewModelScope.launch {
            repository.insertSpot(spot)
        }
    }

    suspend fun getSpotsForUser(userUsername: String) {
        viewModelScope.launch {
            repository.getSpotsForUser(userUsername)
        }
    }

    //SPOTS LOGS

    suspend fun insertSpotLog(spotLog: SpotLog) {
        viewModelScope.launch {
            repository.insertSpotLog(spotLog)
        }
    }

    suspend fun getLogsForSpot(spotId: Int) {
        viewModelScope.launch {
            repository.getLogsForSpot(spotId)
        }
    }

    //USER

    suspend fun insertUser(user: User) {
        viewModelScope.launch {
            repository.insertUser(user)
        }
    }

    suspend fun getUserByUsername(username: String): User? {
        return withContext(Dispatchers.IO) {
            repository.getUserByUsername(username)
        }
    }

    suspend fun getUserWithPassword(username: String, password: String): User?  {
        return withContext(Dispatchers.IO) {
            repository.getUserWithPassword(username, password)
        }
    }

    suspend fun getAllUsers(): List<User> {
        return withContext(Dispatchers.IO) {
            repository.getAllUsers()
        }
    }

    suspend fun delete(user: User) {
        viewModelScope.launch {
            repository.delete(user)
        }
    }
}

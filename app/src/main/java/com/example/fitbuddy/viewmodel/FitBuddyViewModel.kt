package com.example.fitbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitbuddy.db.FitBuddyRepository
import com.example.fitbuddy.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FitBuddyViewModel @Inject constructor(
    private val repository: FitBuddyRepository
) : ViewModel() {

    fun insertAction(action: Action) {
        viewModelScope.launch {
            repository.insertAction(action)
        }
    }

    fun getActionsForUser(userUsername: String) {
        viewModelScope.launch {
            repository.getActionsForUser(userUsername)
        }
    }

    fun insertFollower(follower: Follower) {
        viewModelScope.launch {
            repository.insertFollower(follower)
        }
    }

    fun getFollowersForUser(userFK: String) {
        viewModelScope.launch {
            repository.getFollowersForUser(userFK)
        }
    }

    fun insertSpot(spot: Spot) {
        viewModelScope.launch {
            repository.insertSpot(spot)
        }
    }

    fun getSpotsForUser(userUsername: String) {
        viewModelScope.launch {
            repository.getSpotsForUser(userUsername)
        }
    }

    fun insertSpotLog(spotLog: SpotLog) {
        viewModelScope.launch {
            repository.insertSpotLog(spotLog)
        }
    }

    fun getLogsForSpot(spotId: Int) {
        viewModelScope.launch {
            repository.getLogsForSpot(spotId)
        }
    }

    fun insertUser(user: User) {
        viewModelScope.launch {
            repository.insertUser(user)
        }
    }

    fun getUserByUsername(username: String) {
        viewModelScope.launch {
            repository.getUserByUsername(username)
        }
    }
}

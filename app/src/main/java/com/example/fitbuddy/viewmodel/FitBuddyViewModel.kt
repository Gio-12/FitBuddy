package com.example.fitbuddy.viewmodel

import android.util.Log
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
    private val TAG = "FitBuddyViewModel"

    suspend fun insertAction(action: Action): Long {
        return withContext(Dispatchers.IO) {
            try {
                val actionId = repository.insertAction(action)
                Log.d(TAG, "Action saved to database: $actionId")
                actionId
            } catch (e: Exception) {
                Log.e(TAG, "Error saving action to database", e)
                -1L
            }
        }
    }

    suspend fun updateAction(action: Action) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAction(action)
        }
    }

    suspend fun getActionById(actionId: Long): Action? {
        return withContext(Dispatchers.IO) {
            try {
                val action = repository.getActionById(actionId)
                Log.d(TAG, "Action retrieved from database: $actionId")
                action
            } catch (e: Exception) {
                Log.e(TAG, "Error retrieving action from database", e)
                null
            }
        }
    }

    suspend fun getActionsForUser(userUsername: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getActionsForUser(userUsername)
        }
    }

    //FOLLOWER

    suspend fun insertFollower(follower: Follower) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertFollower(follower)
        }
    }

    suspend fun getFollowersForUser(userFK: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getFollowersForUser(userFK)
        }
    }

    //SPOT

    suspend fun insertSpot(spot: Spot) : Long {
        return withContext(Dispatchers.IO) {
            try {
                val spotId = repository.insertSpot(spot)
                Log.d(TAG, "Spot saved to database: $spotId")
                spotId
            } catch (e: Exception) {
                Log.e(TAG, "Error saving spot to database", e)
                -1
            }
        }
    }

    suspend fun getSpotsForUser(userUsername: String) : List<Spot> {
        return withContext(Dispatchers.IO) {
            try {
                val spots = repository.getSpotsForUser(userUsername)
                Log.d(TAG, "Spots retrieved")
                spots
            } catch (e: Exception) {
                Log.e(TAG, "Error retrieving spots from database", e)
                emptyList<Spot>()
            }
        }
    }

    //SPOTS LOGS

    suspend fun insertSpotLog(spotLog: SpotLog) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertSpotLog(spotLog)
        }
    }

    suspend fun getLogsForSpot(spotId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getLogsForSpot(spotId)
        }
    }

    //USER

    suspend fun insertUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(user)
        }
    }
}

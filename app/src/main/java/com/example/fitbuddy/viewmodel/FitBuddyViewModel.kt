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

    suspend fun getActionsForPeriod(username: String, startTime: Long, endTime: Long) : List<Action>{
        return withContext(Dispatchers.IO) {
            try {
                val action = repository.getActionsForPeriod(username, startTime, endTime)
                Log.d(TAG, "Actions retrieved from database")
                action
            } catch (e: Exception) {
                Log.e(TAG, "Error retrieving actions from database", e)
                emptyList<Action>()
            }
        }
    }
    //FOLLOWER

    suspend fun insertFollower(follower: Follower) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertFollower(follower)
        }
    }

    fun removeFollowing(username: String, loggedUsername: String) {
        viewModelScope.launch {
            repository.removeFollowing(username, loggedUsername)
        }
    }

    suspend fun getFollowersForUser(userFK: String) : List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val followers = repository.getFollowersForUser(userFK)
                Log.d(TAG, "Spots retrieved")
                followers
            } catch (e: Exception) {
                Log.e(TAG, "Error retrieving spots from database", e)
                emptyList<String>()
            }
        }
    }

    suspend fun getFollowingForUser(userFK: String) : List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val following = repository.getFollowingForUser(userFK)
                Log.d(TAG, "Spots retrieved")
                following
            } catch (e: Exception) {
                Log.e(TAG, "Error retrieving spots from database", e)
                emptyList<String>()
            }
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

    suspend fun getLogsForSpot(spotId: Int) : List<SpotLog>{
        return withContext(Dispatchers.IO) {
            try {
                val spotsLogs =  repository.getLogsForSpot(spotId)
                Log.d(TAG, "Spots retrieved")
                spotsLogs
            } catch (e: Exception) {
                Log.e(TAG, "Error retrieving spots from database", e)
                emptyList<SpotLog>()
            }
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

    suspend fun searchUsers(query: String): List<String> {
        return repository.searchUsers(query)
    }

    suspend fun getAllUsers(): List<String> {
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

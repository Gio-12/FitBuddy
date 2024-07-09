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

    private val tag = "FitBuddyViewModel"

    // ACTIONS
    suspend fun insertAction(action: Action): Long {
        return withContext(Dispatchers.IO) {
            try {
                val actionId = repository.insertAction(action)
                Log.d(tag, "Action saved to database: $actionId")
                actionId
            } catch (e: Exception) {
                Log.e(tag, "Error saving action to database", e)
                -1L
            }
        }
    }

    suspend fun updateAction(action: Action) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateAction(action)
                Log.d(tag, "Action updated in database: ${action.id}")
            } catch (e: Exception) {
                Log.e(tag, "Error updating action in database", e)
            }
        }
    }

    suspend fun getActionById(actionId: Long): Action? {
        return withContext(Dispatchers.IO) {
            try {
                val action = repository.getActionById(actionId)
                Log.d(tag, "Action retrieved from database: $actionId")
                action
            } catch (e: Exception) {
                Log.e(tag, "Error retrieving action from database", e)
                null
            }
        }
    }

    suspend fun getActionsForUser(userUsername: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getActionsForUser(userUsername)
                Log.d(tag, "Actions retrieved for user: $userUsername")
            } catch (e: Exception) {
                Log.e(tag, "Error retrieving actions for user from database", e)
            }
        }
    }

    suspend fun getActionsForPeriod(username: String, startTime: Long, endTime: Long): List<Action> {
        return withContext(Dispatchers.IO) {
            try {
                val actions = repository.getActionsForPeriod(username, startTime, endTime)
                Log.d(tag, "Actions retrieved from database for period")
                actions
            } catch (e: Exception) {
                Log.e(tag, "Error retrieving actions from database for period", e)
                emptyList()
            }
        }
    }

    // FOLLOWER
    suspend fun insertFollower(follower: Follower) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertFollower(follower)
                Log.d(tag, "Follower inserted: ${follower.followerFK}")
            } catch (e: Exception) {
                Log.e(tag, "Error inserting follower", e)
            }
        }
    }

    fun removeFollowing(username: String, loggedUsername: String) {
        viewModelScope.launch {
            try {
                repository.removeFollowing(username, loggedUsername)
                Log.d(tag, "Following removed: $username for user: $loggedUsername")
            } catch (e: Exception) {
                Log.e(tag, "Error removing following", e)
            }
        }
    }

    suspend fun getFollowersForUser(userFK: String): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val followers = repository.getFollowersForUser(userFK)
                Log.d(tag, "Followers retrieved for user: $userFK")
                followers
            } catch (e: Exception) {
                Log.e(tag, "Error retrieving followers for user from database", e)
                emptyList()
            }
        }
    }

    suspend fun getFollowingForUser(userFK: String): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val following = repository.getFollowingForUser(userFK)
                Log.d(tag, "Following retrieved for user: $userFK")
                following
            } catch (e: Exception) {
                Log.e(tag, "Error retrieving following for user from database", e)
                emptyList()
            }
        }
    }

    // SPOT
    suspend fun insertSpot(spot: Spot): Long {
        return withContext(Dispatchers.IO) {
            try {
                val spotId = repository.insertSpot(spot)
                Log.d(tag, "Spot saved to database: $spotId")
                spotId
            } catch (e: Exception) {
                Log.e(tag, "Error saving spot to database", e)
                -1
            }
        }
    }

    suspend fun getSpotsForUser(userUsername: String): List<Spot> {
        return withContext(Dispatchers.IO) {
            try {
                val spots = repository.getSpotsForUser(userUsername)
                Log.d(tag, "Spots retrieved for user: $userUsername")
                spots
            } catch (e: Exception) {
                Log.e(tag, "Error retrieving spots for user from database", e)
                emptyList()
            }
        }
    }

    // SPOT LOGS
    suspend fun insertSpotLog(spotLog: SpotLog) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertSpotLog(spotLog)
                Log.d(tag, "SpotLog inserted: ${spotLog.id}")
            } catch (e: Exception) {
                Log.e(tag, "Error inserting spot log", e)
            }
        }
    }

    suspend fun getLogsForSpot(spotId: Int): List<SpotLog> {
        return withContext(Dispatchers.IO) {
            try {
                val spotLogs = repository.getLogsForSpot(spotId)
                Log.d(tag, "SpotLogs retrieved for spot: $spotId")
                spotLogs
            } catch (e: Exception) {
                Log.e(tag, "Error retrieving spot logs from database", e)
                emptyList()
            }
        }
    }

    // USER
    suspend fun insertUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertUser(user)
                Log.d(tag, "User inserted: ${user.username}")
            } catch (e: Exception) {
                Log.e(tag, "Error inserting user", e)
            }
        }
    }

    suspend fun getUserByUsername(username: String): User? {
        return withContext(Dispatchers.IO) {
            try {
                val user = repository.getUserByUsername(username)
                Log.d(tag, "User retrieved by username: $username")
                user
            } catch (e: Exception) {
                Log.e(tag, "Error retrieving user by username", e)
                null
            }
        }
    }

    suspend fun getUserWithPassword(username: String, password: String): User? {
        return withContext(Dispatchers.IO) {
            try {
                val user = repository.getUserWithPassword(username, password)
                Log.d(tag, "User retrieved by username and password: $username")
                user
            } catch (e: Exception) {
                Log.e(tag, "Error retrieving user by username and password", e)
                null
            }
        }
    }

    suspend fun searchUsers(query: String): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val users = repository.searchUsers(query)
                Log.d(tag, "Users retrieved by search query: $query")
                users
            } catch (e: Exception) {
                Log.e(tag, "Error searching users", e)
                emptyList()
            }
        }
    }

    suspend fun getAllUsers(): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val users = repository.getAllUsers()
                Log.d(tag, "All users retrieved")
                users
            } catch (e: Exception) {
                Log.e(tag, "Error retrieving all users", e)
                emptyList()
            }
        }
    }

    suspend fun delete(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.delete(user)
                Log.d(tag, "User deleted: ${user.username}")
            } catch (e: Exception) {
                Log.e(tag, "Error deleting user", e)
            }
        }
    }
}

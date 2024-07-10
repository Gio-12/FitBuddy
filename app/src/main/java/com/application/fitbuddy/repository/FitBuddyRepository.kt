package com.application.fitbuddy.repository

import com.application.fitbuddy.dao.*
import com.application.fitbuddy.models.*

class FitBuddyRepository(
    private val actionDao: ActionDao,
    private val followerDao: FollowerDao,
    private val spotDao: SpotDao,
    private val spotLogDao: SpotLogDao,
    private val userDao: UserDao
) {

    //ACTIONS
    suspend fun insertAction(action: Action): Long {
        return actionDao.insert(action)
    }
    suspend fun updateAction(action: Action) = actionDao.update(action)

    suspend fun getActionById(actionId: Long) = actionDao.getActionById(actionId)

    suspend fun getActionsForUser(userUsername: String) = actionDao.getActionsForUser(userUsername)

    suspend fun getActionsForPeriod(username: String, startTime: Long, endTime: Long) = actionDao.getActionsForPeriod(username, startTime, endTime)

    //FOLLOWERS

    suspend fun insertFollower(follower: Follower) = followerDao.insert(follower)

    suspend fun removeFollowing(username: String, loggedUsername: String) = followerDao.removeFollowing(username, loggedUsername)

    suspend fun getFollowersForUser(userFK: String): List<String> {
        return followerDao.getFollowersForUser(userFK)
    }
    suspend fun getFollowingForUser(followerFK: String): List<String> {
        return followerDao.getFollowingForUser(followerFK)
    }

    //SPOTS

    suspend fun insertSpot(spot: Spot): Long {
        return spotDao.insert(spot)
    }

    suspend fun getSpotsForUser(userUsername: String): List<Spot> {
        return spotDao.getSpotsForUser(userUsername)
    }

    //SPOTS LOGS

    suspend fun insertSpotLog(spotLog: SpotLog) = spotLogDao.insert(spotLog)

    suspend fun getLogsForSpot(spotId: Int) = spotLogDao.getLogsForSpot(spotId)

    //USERS

    suspend fun insertUser(user: User) = userDao.insert(user)

    suspend fun updateUser(user: User) = userDao.update(user)

    suspend fun getUserByUsername(username: String) = userDao.getUserByUsername(username)

    suspend fun getUserWithPassword(username: String, password: String): User? {
        return userDao.getUserWithPassword(username, password)
    }

    suspend fun searchUsers(query: String): List<String> {
        return userDao.searchUsers(query)
    }

    suspend fun getAllUsers(): List<String> {
        return userDao.getAllUsers()
    }

    suspend fun delete(user: User)  = userDao.delete(user)
}

package com.example.fitbuddy.db

import com.example.fitbuddy.dao.*
import com.example.fitbuddy.models.*

class FitBuddyRepository(
    private val actionDao: ActionDao,
    private val followerDao: FollowerDao,
    private val spotDao: SpotDao,
    private val spotLogDao: SpotLogDao,
    private val userDao: UserDao
) {
    suspend fun insertAction(action: Action) = actionDao.insert(action)

    suspend fun getActionsForUser(userUsername: String) = actionDao.getActionsForUser(userUsername)

    suspend fun insertFollower(follower: Follower) = followerDao.insert(follower)

    suspend fun getFollowersForUser(userFK: String) = followerDao.getFollowersForUser(userFK)

    suspend fun insertSpot(spot: Spot) = spotDao.insert(spot)

    suspend fun getSpotsForUser(userUsername: String) = spotDao.getSpotsForUser(userUsername)

    suspend fun insertSpotLog(spotLog: SpotLog) = spotLogDao.insert(spotLog)

    suspend fun getLogsForSpot(spotId: Int) = spotLogDao.getLogsForSpot(spotId)

    suspend fun insertUser(user: User) = userDao.insert(user)

    suspend fun getUserByUsername(username: String) = userDao.getUserByUsername(username)
}

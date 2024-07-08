package com.example.fitbuddy.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fitbuddy.models.Follower

@Dao
interface FollowerDao {
    @Insert
    suspend fun insert(follower: Follower)

    @Query("DELETE FROM follower WHERE userFK = :username AND followerFK = :loggedUsername")
    suspend fun removeFollowing(username: String, loggedUsername: String)

    @Query("SELECT followerFK FROM follower WHERE userFK = :userFK")
    suspend fun getFollowersForUser(userFK: String): List<String>

    @Query("SELECT userFK FROM follower WHERE followerFK = :followerFK")
    suspend fun getFollowingForUser(followerFK: String): List<String>
}
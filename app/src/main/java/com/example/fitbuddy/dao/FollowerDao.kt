package com.example.fitbuddy.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fitbuddy.models.Follower

@Dao
interface FollowerDao {
    @Insert
    suspend fun insert(follower: Follower)

    @Query("SELECT * FROM follower WHERE userFK = :userFK")
    suspend fun getFollowersForUser(userFK: String): List<Follower>

    //VOGLIO ANCHE QUELLI CHE NON FOLLOWANO
}
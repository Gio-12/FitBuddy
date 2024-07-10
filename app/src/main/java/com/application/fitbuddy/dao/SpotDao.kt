package com.application.fitbuddy.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.application.fitbuddy.models.Spot

@Dao
interface SpotDao {
    @Insert
    suspend fun insert(spot: Spot): Long

    @Update
    suspend fun update(spot: Spot)

    @Delete
    suspend fun delete(spot: Spot)

    @Query("SELECT * FROM spots WHERE user_username = :userUsername")
    suspend fun getSpotsForUser(userUsername: String): List<Spot>


}
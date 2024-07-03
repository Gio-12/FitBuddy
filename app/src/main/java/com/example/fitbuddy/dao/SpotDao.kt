package com.example.fitbuddy.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.fitbuddy.models.Spot

@Dao
interface SpotDao {
    @Insert
    suspend fun insert(spot: Spot)

    @Delete
    suspend fun deleteLocation(spot: Spot)

    @Query("SELECT * FROM spots WHERE user_username = :userUsername")
    suspend fun getSpotsForUser(userUsername: String): List<Spot>

    //VOGLIO ANCHE LO SPOT IN CORRISPONDENZA DI LAT E LONG
}
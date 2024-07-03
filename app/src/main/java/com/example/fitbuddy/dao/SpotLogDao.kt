package com.example.fitbuddy.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fitbuddy.models.SpotLog

@Dao
interface SpotLogDao {
    @Insert
    suspend fun insert(spotLog: SpotLog)

    @Query("SELECT * FROM spot_logs WHERE spot_id = :spotId")
    suspend fun getLogsForSpot(spotId: Int): List<SpotLog>
}

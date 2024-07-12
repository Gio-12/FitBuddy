package com.application.fitbuddy.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.application.fitbuddy.models.SpotLog

@Dao
interface SpotLogDao {
    @Insert
    suspend fun insert(spotLog: SpotLog) : Long

    @Update
    suspend fun update(spotLog: SpotLog)

    @Delete
    suspend fun delete(spotLog: SpotLog)

    @Query("SELECT * FROM spot_logs WHERE spot_id = :spotId")
    suspend fun getLogsForSpot(spotId: Int): List<SpotLog>
}

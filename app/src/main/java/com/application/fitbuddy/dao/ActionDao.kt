package com.application.fitbuddy.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.application.fitbuddy.models.Action

@Dao
interface ActionDao {
    @Insert
    suspend fun insert(action: Action) : Long

    @Update
    suspend fun update(action: Action)

    @Query("SELECT * FROM actions WHERE id = :actionId")
    suspend fun getActionById(actionId: Long): Action?

    @Query("SELECT * FROM actions WHERE user_username = :userUsername")
    suspend fun getActionsForUser(userUsername: String): List<Action>

    @Query("SELECT * FROM actions WHERE user_username = :username AND start_time BETWEEN :startTime AND :endTime")
    suspend fun getActionsForPeriod(username: String, startTime: Long, endTime: Long): List<Action>

    @Delete
    suspend fun delete(action: Action)
}

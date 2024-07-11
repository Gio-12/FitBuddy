package com.application.fitbuddy.repository

import com.application.fitbuddy.dao.SpotLogDao
import com.application.fitbuddy.models.SpotLog
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class SpotLogRepository(
    private val spotLogDao: SpotLogDao,
    private val database: FirebaseDatabase
) {

    private val spotLogsRef: DatabaseReference = database.getReference("spot_logs")

    suspend fun insert(spotLog: SpotLog) {
        return try {
            // Insert into RoomDB
            val spotLogId = spotLogDao.insert(spotLog)
            // Insert into Firebase
            spotLogsRef.child(spotLog.id.toString()).setValue(spotLog).await()
            spotLogId
        } catch (e: Exception) {
            throw SpotLogRepositoryException("Failed to insert spot log", e)
        }
    }

    suspend fun update(spotLog: SpotLog) {
        try {
            // Update in RoomDB
            spotLogDao.update(spotLog)
            // Update in Firebase
            spotLogsRef.child(spotLog.id.toString()).setValue(spotLog).await()
        } catch (e: Exception) {
            throw SpotLogRepositoryException("Failed to update spot log", e)
        }
    }

    suspend fun delete(spotLog: SpotLog) {
        try {
            // Delete from RoomDB
            spotLogDao.delete(spotLog)
            // Delete from Firebase
            spotLogsRef.child(spotLog.id.toString()).removeValue().await()
        } catch (e: Exception) {
            throw SpotLogRepositoryException("Failed to delete spot log", e)
        }
    }

    suspend fun getSpotLogById(spotLogId: Long): SpotLog? {
        return try {
            val dataSnapshot = spotLogsRef.child(spotLogId.toString()).get().await()
            dataSnapshot.getValue(SpotLog::class.java)
        } catch (e: Exception) {
            throw SpotLogRepositoryException("Failed to get spot log by id", e)
        }
    }

    suspend fun getLogsForSpot(spotId: Int): List<SpotLog> {
        return try {
            val query = spotLogsRef.orderByChild("spotId").equalTo(spotId.toString()).get().await()
            query.children.mapNotNull { it.getValue(SpotLog::class.java) }
        } catch (e: Exception) {
            throw SpotLogRepositoryException("Failed to get logs for spot", e)
        }
    }

    // Custom exception for repository errors
    class SpotLogRepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)
}

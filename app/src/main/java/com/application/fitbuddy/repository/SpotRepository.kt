package com.application.fitbuddy.repository

import com.application.fitbuddy.dao.SpotDao
import com.application.fitbuddy.models.Spot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class SpotRepository(
    private val spotDao: SpotDao,
    private val database: FirebaseDatabase
) {

    private val spotsRef: DatabaseReference = database.getReference("spots")

    suspend fun insert(spot: Spot): Long {
        return try {
            // Insert into RoomDB
            val spotId = spotDao.insert(spot)
            // Insert into Firebase
            spotsRef.child(spot.id.toString()).setValue(spot).await()
            spotId
        } catch (e: Exception) {
            throw SpotRepositoryException("Failed to insert spot", e)
        }
    }

    suspend fun update(spot: Spot) {
        try {
            // Update in RoomDB
            spotDao.update(spot)
            // Update in Firebase
            spotsRef.child(spot.id.toString()).setValue(spot).await()
        } catch (e: Exception) {
            throw SpotRepositoryException("Failed to update spot", e)
        }
    }

    suspend fun delete(spot: Spot) {
        try {
            // Delete from RoomDB
            spotDao.delete(spot)
            // Delete from Firebase
            spotsRef.child(spot.id.toString()).removeValue().await()
        } catch (e: Exception) {
            throw SpotRepositoryException("Failed to delete spot", e)
        }
    }

    suspend fun getSpotById(spotId: Long): Spot? {
        return try {
            val dataSnapshot = spotsRef.child(spotId.toString()).get().await()
            dataSnapshot.getValue(Spot::class.java)
        } catch (e: Exception) {
            throw SpotRepositoryException("Failed to get spot by id", e)
        }
    }

    suspend fun getSpotsForUser(userUsername: String): List<Spot> {
        return try {
            val query = spotsRef.orderByChild("userUsername").equalTo(userUsername).get().await()
            query.children.mapNotNull { it.getValue(Spot::class.java) }
        } catch (e: Exception) {
            throw SpotRepositoryException("Failed to get spots for user", e)
        }
    }

    // Custom exception for repository errors
    class SpotRepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)
}

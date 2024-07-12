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
            val spotId = spotDao.insert(spot)
            spot.id = spotId.toInt()
            spotsRef.child(spot.id.toString()).setValue(spot).await()
            spotId
        } catch (e: Exception) {
            throw SpotRepositoryException("Failed to insert spot", e)
        }
    }

    suspend fun update(spot: Spot) {
        try {
            spotDao.update(spot)
            spotsRef.child(spot.id.toString()).setValue(spot).await()
        } catch (e: Exception) {
            throw SpotRepositoryException("Failed to update spot", e)
        }
    }

    suspend fun delete(spot: Spot) {
        try {
            spotDao.delete(spot)
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
            val spotsSnapshot = spotsRef.get().await()
            if (spotsSnapshot.exists() && spotsSnapshot.hasChildren()) {
                val query = spotsRef.orderByChild("userUsername").equalTo(userUsername).get().await()
                query.children.mapNotNull { it.getValue(Spot::class.java) }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            throw SpotRepositoryException("Failed to get spots for user", e)
        }
    }

    class SpotRepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)
}

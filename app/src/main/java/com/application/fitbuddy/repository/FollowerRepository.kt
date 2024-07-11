package com.application.fitbuddy.repository

import com.application.fitbuddy.dao.FollowerDao
import com.application.fitbuddy.models.Follower
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FollowerRepository(
    private val followerDao: FollowerDao,
    private val database: FirebaseDatabase
) {

    private val followersRef: DatabaseReference = database.getReference("followers")

    suspend fun insert(follower: Follower) {
        return try {
            // Insert into RoomDB
            val followerId = followerDao.insert(follower)
            // Insert into Firebase
            followersRef.child(follower.id.toString()).setValue(follower).await()
            followerId
        } catch (e: Exception) {
            throw FollowerRepositoryException("Failed to insert follower", e)
        }
    }

    suspend fun update(follower: Follower) {
        try {
            // Update in RoomDB
            followerDao.update(follower)
            // Update in Firebase
            followersRef.child(follower.id.toString()).setValue(follower).await()
        } catch (e: Exception) {
            throw FollowerRepositoryException("Failed to update follower", e)
        }
    }

    suspend fun delete(follower: Follower) {
        try {
            // Delete from RoomDB
            followerDao.delete(follower)
            // Delete from Firebase
            followersRef.child(follower.id.toString()).removeValue().await()
        } catch (e: Exception) {
            throw FollowerRepositoryException("Failed to delete follower", e)
        }
    }

    suspend fun removeFollowing(userFK: String, followerFK: String) {
        try {
            // Remove from RoomDB
            followerDao.removeFollowing(userFK, followerFK)
            // Remove from Firebase
            followersRef.child("${userFK}_${followerFK}").removeValue().await()
        } catch (e: Exception) {
            throw FollowerRepositoryException("Failed to remove following", e)
        }
    }

    suspend fun getFollowerById(followerId: Long): Follower? {
        return try {
            val dataSnapshot = followersRef.child(followerId.toString()).get().await()
            dataSnapshot.getValue(Follower::class.java)
        } catch (e: Exception) {
            throw FollowerRepositoryException("Failed to get follower by id", e)
        }
    }

    suspend fun getFollowersForUser(userFK: String): List<String> {
        return try {
            val query = followersRef.orderByChild("userFK").equalTo(userFK).get().await()
            query.children.mapNotNull { it.key }
        } catch (e: Exception) {
            throw FollowerRepositoryException("Failed to get followers for user", e)
        }
    }

    suspend fun getFollowingForUser(followerFK: String): List<String> {
        return try {
            val query = followersRef.orderByChild("followerFK").equalTo(followerFK).get().await()
            query.children.mapNotNull { it.key }
        } catch (e: Exception) {
            throw FollowerRepositoryException("Failed to get following for user", e)
        }
    }

    // Custom exception for repository errors
    class FollowerRepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)
}

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

    suspend fun insert(follower: Follower) : Long {
        return try {
            val followerId = followerDao.insert(follower)
            follower.id = followerId.toInt()
            followersRef.child(follower.id.toString()).setValue(follower).await()
            followerId
        } catch (e: Exception) {
            throw FollowerRepositoryException("Failed to insert follower--> $e", e.cause)
        }
    }

    suspend fun update(follower: Follower) {
        try {
            followerDao.update(follower)
            followersRef.child(follower.id.toString()).setValue(follower).await()
        } catch (e: Exception) {
            throw FollowerRepositoryException("Failed to update follower--> $e", e.cause)
        }
    }

    suspend fun delete(follower: Follower) {
        try {
            followerDao.delete(follower)
            followersRef.child(follower.id.toString()).removeValue().await()
        } catch (e: Exception) {
            throw FollowerRepositoryException("Failed to delete follower--> $e", e.cause)
        }
    }

    suspend fun removeFollowing(userFK: String, followerFK: String) {
        try {
            followerDao.removeFollowing(userFK, followerFK)
            followersRef.child("${userFK}_${followerFK}").removeValue().await()
        } catch (e: Exception) {
            throw FollowerRepositoryException("Failed to remove following--> $e", e.cause)
        }
    }

    suspend fun getFollowerById(followerId: Long): Follower? {
        return try {
            val dataSnapshot = followersRef.child(followerId.toString()).get().await()
            dataSnapshot.getValue(Follower::class.java)
        } catch (e: Exception) {
            throw FollowerRepositoryException("Failed to get follower by id--> $e", e.cause)
        }
    }

    suspend fun getFollowersForUser(userFK: String): List<String> {
        return try {
            val followersSnapshot = followersRef.get().await()
            if (followersSnapshot.exists() && followersSnapshot.hasChildren()) {
                val query = followersRef.orderByChild("userFK").equalTo(userFK).get().await()
                query.children.mapNotNull { it.getValue(Follower::class.java)?.followerFK }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            throw FollowerRepositoryException("Failed to get followers for user --> $e", e.cause)
        }
    }

    suspend fun getFollowingForUser(followerFK: String): List<String> {
        return try {
            val followersSnapshot = followersRef.get().await()
            if (followersSnapshot.exists() && followersSnapshot.hasChildren()) {
                val query = followersRef.orderByChild("followerFK").equalTo(followerFK).get().await()
                query.children.mapNotNull { it.getValue(Follower::class.java)?.userFK }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            throw FollowerRepositoryException("Failed to get following for user--> $e", e.cause)
        }
    }

    class FollowerRepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)
}

package com.application.fitbuddy.repository

import com.application.fitbuddy.dao.UserDao
import com.application.fitbuddy.models.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val userDao: UserDao,
    private val database: FirebaseDatabase
) {

    private val usersRef: DatabaseReference = database.getReference("users")

    suspend fun insert(user: User) {
        return try {
            val userId = userDao.insert(user)
            usersRef.child(user.username).setValue(user).await()
            userId
        } catch (e: Exception) {
            throw UserRepositoryException("Failed to insert user--> $e", e.cause)
        }
    }

    suspend fun update(user: User) {
        try {
            userDao.update(user)
            usersRef.child(user.username).setValue(user).await()
        } catch (e: Exception) {
            throw UserRepositoryException("Failed to update user--> $e", e.cause)
        }
    }

    suspend fun delete(user: User) {
        try {
            userDao.delete(user)
            usersRef.child(user.username).removeValue().await()
        } catch (e: Exception) {
            throw UserRepositoryException("Failed to delete user--> $e", e.cause)
        }
    }

    suspend fun getUserByUsername(username: String): User? {
        return try {
            val dataSnapshot = usersRef.child(username).get().await()
            dataSnapshot.getValue(User::class.java)
        } catch (e: Exception) {
            throw UserRepositoryException("Failed to get user by username--> $e", e.cause)
        }
    }

    suspend fun getUserWithPassword(username: String, password: String): User? {
        return try {
            val dataSnapshot = usersRef.child(username).get().await()
            val user = dataSnapshot.getValue(User::class.java)
            if (user?.password == password) {
                user
            } else {
                null
            }
        } catch (e: Exception) {
            throw UserRepositoryException("Failed to get user with password--> $e", e.cause)
        }
    }

    suspend fun searchUsers(query: String): List<String> {
        return try {
            val usersSnapshot = usersRef.get().await()
            if (usersSnapshot.exists() && usersSnapshot.hasChildren()) {
                val snapshot = usersRef.orderByKey().startAt(query).endAt(query + "\uf8ff").get().await()
                snapshot.children.mapNotNull { it.key }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            throw UserRepositoryException("Failed to search users--> $e", e.cause)
        }
    }

    suspend fun getAllUsers(): List<String> {
        return try {
            val usersSnapshot = usersRef.get().await()
            if (usersSnapshot.exists() && usersSnapshot.hasChildren()) {
                val snapshot = usersRef.get().await()
                snapshot.children.mapNotNull { it.key }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            throw UserRepositoryException("Failed to get all users--> $e", e.cause)
        }
    }

    class UserRepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)
}

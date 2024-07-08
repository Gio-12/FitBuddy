package com.example.fitbuddy.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fitbuddy.models.User

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    suspend fun getUserWithPassword(username: String, password: String): User?

    @Query("SELECT username FROM users WHERE username LIKE '%' || :query || '%'")
    suspend fun searchUsers(query: String): List<String>

    @Query("SELECT username FROM users")
    suspend fun getAllUsers(): List<String>

    @Delete
    suspend fun delete(user: User)
}

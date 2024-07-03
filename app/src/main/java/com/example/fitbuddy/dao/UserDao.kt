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
    fun getUserWithPassword(username: String, password: String): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): List<User>

    @Delete
    fun delete(user: User)
}

package com.example.fitbuddy.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.fitbuddy.dao.ActionDao
import com.example.fitbuddy.dao.FollowerDao
import com.example.fitbuddy.dao.SpotDao
import com.example.fitbuddy.dao.SpotLogDao
import com.example.fitbuddy.dao.UserDao
import com.example.fitbuddy.models.User
import com.example.fitbuddy.models.Action
import com.example.fitbuddy.models.Follower
import com.example.fitbuddy.models.Spot
import com.example.fitbuddy.models.SpotLog

@Database(entities = [User::class, Action::class, Follower::class, Spot::class, SpotLog::class], version = 1, exportSchema = false)
abstract class FitBuddyDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun actionDao(): ActionDao
    abstract fun followerDao(): FollowerDao
    abstract fun spotDao(): SpotDao
    abstract fun spotLogDao(): SpotLogDao
}
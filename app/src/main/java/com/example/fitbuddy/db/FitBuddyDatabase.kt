package com.example.fitbuddy.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
import com.example.fitbuddy.utils.DateConverter

@Database(entities = [User::class, Action::class, Follower::class, Spot::class, SpotLog::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class FitBuddyDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun actionDao(): ActionDao
    abstract fun followerDao(): FollowerDao
    abstract fun spotDao(): SpotDao
    abstract fun spotLogDao(): SpotLogDao


    companion object {
        @Volatile
        private var INSTANCE: FitBuddyDatabase? = null

        fun getDatabase(context: Context): FitBuddyDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitBuddyDatabase::class.java,
                    "fitbuddy_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
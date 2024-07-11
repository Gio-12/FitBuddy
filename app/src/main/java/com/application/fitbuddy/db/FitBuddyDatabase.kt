package com.application.fitbuddy.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.application.fitbuddy.dao.ActionDao
import com.application.fitbuddy.dao.FollowerDao
import com.application.fitbuddy.dao.SpotDao
import com.application.fitbuddy.dao.SpotLogDao
import com.application.fitbuddy.dao.UserDao
import com.application.fitbuddy.models.Action
import com.application.fitbuddy.models.Follower
import com.application.fitbuddy.models.Spot
import com.application.fitbuddy.models.SpotLog
import com.application.fitbuddy.models.User
import com.application.fitbuddy.utils.DateConverter

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
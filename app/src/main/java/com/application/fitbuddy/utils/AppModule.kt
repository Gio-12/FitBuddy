package com.application.fitbuddy.utils

import android.content.Context
import androidx.room.Room
import com.application.fitbuddy.db.FitBuddyDatabase
import com.application.fitbuddy.dao.*
import com.application.fitbuddy.repository.FitBuddyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FitBuddyDatabase {
        return Room.databaseBuilder(
            context,
            FitBuddyDatabase::class.java,
            "fitbuddy_database"
        ).build()
    }

    @Provides
    fun provideActionDao(db: FitBuddyDatabase): ActionDao = db.actionDao()

    @Provides
    fun provideFollowerDao(db: FitBuddyDatabase): FollowerDao = db.followerDao()

    @Provides
    fun provideSpotDao(db: FitBuddyDatabase): SpotDao = db.spotDao()

    @Provides
    fun provideSpotLogDao(db: FitBuddyDatabase): SpotLogDao = db.spotLogDao()

    @Provides
    fun provideUserDao(db: FitBuddyDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideRepository(
        actionDao: ActionDao,
        followerDao: FollowerDao,
        spotDao: SpotDao,
        spotLogDao: SpotLogDao,
        userDao: UserDao
    ): FitBuddyRepository {
        return FitBuddyRepository(actionDao, followerDao, spotDao, spotLogDao, userDao)
    }
}

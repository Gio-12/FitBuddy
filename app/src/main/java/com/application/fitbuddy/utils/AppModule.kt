package com.application.fitbuddy.utils

import android.content.Context
import androidx.room.Room
import com.application.fitbuddy.dao.*
import com.application.fitbuddy.db.FitBuddyDatabase
import com.application.fitbuddy.repository.*
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideActionRepository(actionDao: ActionDao, database: FirebaseDatabase): ActionRepository {
        return ActionRepository(actionDao, database)
    }

    @Provides
    @Singleton
    fun provideFollowerRepository(followerDao: FollowerDao, database: FirebaseDatabase): FollowerRepository {
        return FollowerRepository(followerDao, database)
    }

    @Provides
    @Singleton
    fun provideSpotRepository(spotDao: SpotDao, database: FirebaseDatabase): SpotRepository {
        return SpotRepository(spotDao, database)
    }

    @Provides
    @Singleton
    fun provideSpotLogRepository(spotLogDao: SpotLogDao, database: FirebaseDatabase): SpotLogRepository {
        return SpotLogRepository(spotLogDao, database)
    }

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao, database: FirebaseDatabase): UserRepository {
        return UserRepository(userDao, database)
    }
}

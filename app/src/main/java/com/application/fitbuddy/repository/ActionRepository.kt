package com.application.fitbuddy.repository

import com.application.fitbuddy.dao.ActionDao
import com.application.fitbuddy.models.Action
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class ActionRepository(
    private val actionDao: ActionDao,
    private val database: FirebaseDatabase
) {

    private val actionsRef: DatabaseReference = database.getReference("actions")

    suspend fun insert(action: Action): Long {
        return try {
            val actionId = actionDao.insert(action)
            action.id = actionId.toInt()
            actionsRef.child(action.id.toString()).setValue(action).await()
            actionId
        } catch (e: Exception) {
            throw ActionRepositoryException("Failed to insert action", e)
        }
    }

    suspend fun update(action: Action) {
        try {
            actionDao.update(action)
            actionsRef.child(action.id.toString()).setValue(action).await()
        } catch (e: Exception) {
            throw ActionRepositoryException("Failed to update action", e)
        }
    }

    suspend fun delete(action: Action) {
        try {
            actionDao.delete(action)
            actionsRef.child(action.id.toString()).removeValue().await()
        } catch (e: Exception) {
            throw ActionRepositoryException("Failed to delete action", e)
        }
    }

    suspend fun getActionById(actionId: Long): Action? {
        return try {
            val dataSnapshot = actionsRef.child(actionId.toString()).get().await()
            dataSnapshot.getValue(Action::class.java)
        } catch (e: Exception) {
            throw ActionRepositoryException("Failed to get action by id", e)
        }
    }

    suspend fun getActionsForUser(userUsername: String): List<Action> {
        return try {
            val actionsSnapshot = actionsRef.get().await()
            if (actionsSnapshot.exists() && actionsSnapshot.hasChildren()) {
                val query = actionsRef.orderByChild("userUsername").equalTo(userUsername).get().await()
                query.children.mapNotNull { it.getValue(Action::class.java) }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            throw ActionRepositoryException("Failed to get actions for user", e)
        }
    }

    suspend fun getActionsForPeriod(username: String, startTime: Long, endTime: Long): List<Action> {
        return try {
            val actionsSnapshot = actionsRef.get().await()
            if (actionsSnapshot.exists() && actionsSnapshot.hasChildren()) {
                val query = actionsRef.orderByChild("userUsername").equalTo(username).get().await()
                query.children.mapNotNull { it.getValue(Action::class.java) }
                    .filter { it.startTime >= startTime && it.endTime <= endTime }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            throw ActionRepositoryException("Failed to get actions for period", e)
        }
    }

    class ActionRepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)
}

package com.application.fitbuddy.geofence

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.application.fitbuddy.R
import com.application.fitbuddy.db.FitBuddyDatabase
import com.application.fitbuddy.models.SpotLog
import com.application.fitbuddy.repository.SpotLogRepository
import com.application.fitbuddy.utils.CHANNEL_ID
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.MainScope

class GeofenceReceiver : BroadcastReceiver() {

    private lateinit var repository: SpotLogRepository
    private val tag = "GeofenceReceiver"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(tag, "onReceive called")
        val geofencingEvent = intent.let { GeofencingEvent.fromIntent(it) }
        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
                Log.e(tag, "Geofencing error: $errorMessage")
                return
            }
            Log.d(tag, "geofencingEvent != Null")
        } else {
            Log.e(tag, "geofencingEvent IS NULL")
        }

        val geofenceTransition = geofencingEvent?.geofenceTransition
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            if (triggeringGeofences != null) {
                for (geofence in triggeringGeofences) {
                    val spotId = geofence.requestId.toInt()
                    Log.d(tag, "Geofence transition detected: spotId: $spotId, transition type: $geofenceTransition")
                    sendNotification(context, spotId, geofenceTransition)
                    saveSpotLog(context, spotId, geofenceTransition)
                }
            } else {
                Log.d(tag, "No triggering geofences found.")
            }
        } else {
            Log.d(tag, "Invalid transition type: $geofenceTransition")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(context: Context, spotId: Int, transitionType: Int) {
        val transition = if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) "entered" else "exited"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = CHANNEL_ID
        val title = "Geofence $transition"

        val notification = Notification.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText("You have $transition geofence with location ID: $spotId")
            .setSmallIcon(R.drawable.ic_alarm)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(spotId, notification)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun saveSpotLog(context: Context, spotId: Int, transitionType: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            repository = SpotLogRepository(
                FitBuddyDatabase.getDatabase(context).spotLogDao(),
                FirebaseDatabase.getInstance()
            )
            val entry = transitionType == Geofence.GEOFENCE_TRANSITION_ENTER
            val spotLog = SpotLog(spotId = spotId, date = System.currentTimeMillis(), entry = entry)
            try {
                repository.insert(spotLog)
                Log.d(tag, "SpotLog recorded: $spotLog")
            } catch (e: Exception) {
                Log.e(tag, "Error inserting SpotLog: ${e.message}")
            }
        }
    }
}

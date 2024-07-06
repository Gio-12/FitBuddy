package com.example.fitbuddy.geofence

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.fitbuddy.R
import com.example.fitbuddy.activities.GeofenceActivity
import com.example.fitbuddy.db.FitBuddyDatabase
import com.example.fitbuddy.models.SpotLog
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class GeofenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("GeofenceReceiver", "onReceive called")
        if (context != null && intent != null) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            if (geofencingEvent != null) {
                if (geofencingEvent.hasError()) {
                    val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
                    Log.e("GeofenceReceiver", "Geofencing error: $errorMessage")
                    return
                }
            }

            val geofenceTransition = geofencingEvent?.geofenceTransition
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                val triggeringGeofences = geofencingEvent.triggeringGeofences
                if (triggeringGeofences != null) {
                    for (geofence in triggeringGeofences) {
                        val spotId = geofence.requestId.toInt()
                        Log.d("GeofenceReceiver", "Geofence transition detected: spotId: $spotId, transition type: $geofenceTransition")
                        sendNotification(context, spotId, geofenceTransition)
                        saveSpotLog(context, spotId, geofenceTransition)
                    }
                } else {
                    Log.w("GeofenceReceiver", "No triggering geofences found.")
                }
            } else {
                Log.w("GeofenceReceiver", "Invalid transition type: $geofenceTransition")
            }
        } else {
            Log.e("GeofenceReceiver", "Context or Intent is null")
        }
    }

    private fun sendNotification(context: Context, locationId: Int, transitionType: Int) {
        val transition = if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) "entered" else "exited"
        val notificationId = (System.currentTimeMillis() % 10000).toInt()
        val builder = NotificationCompat.Builder(context, "GEOFENCE_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("Geofence Transition")
            .setContentText("You have $transition geofence with location ID: $locationId")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(notificationId, builder.build())
        } else {
            Log.e("GeofenceReceiver", "Notification permission not granted")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun saveSpotLog(context: Context, spotId: Int, transitionType: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                val spotLogDao = FitBuddyDatabase.getDatabase(context).spotLogDao()
                val entry = transitionType == Geofence.GEOFENCE_TRANSITION_ENTER
                val spotLog = SpotLog(spotId = spotId, date = System.currentTimeMillis(), entry = entry)
                try {
                    spotLogDao.insert(spotLog)
                    withContext(Dispatchers.Main) {
                        Log.d("GeofenceReceiver", "SpotLog recorded: $spotLog")
                    }
                } catch (e: Exception) {
                    Log.e("GeofenceReceiver", "Error inserting SpotLog: ${e.message}")
                }
            } else {
                Log.e("GeofenceReceiver", "Write permission not granted")
            }
        }
    }
}

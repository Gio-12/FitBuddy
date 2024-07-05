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
import com.example.fitbuddy.R
import com.example.fitbuddy.activities.GeofenceActivity
import com.example.fitbuddy.dao.SpotLogDao
import com.example.fitbuddy.models.SpotLog
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


class GeofenceReceiver : BroadcastReceiver() {


    lateinit var spotLogDao: SpotLogDao

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            val geofencingEvent = intent?.let { GeofencingEvent.fromIntent(it) }
            if (geofencingEvent != null) {
                if (geofencingEvent.hasError()) {
                    Log.e("GeofenceReceiver", "Error: ${geofencingEvent.errorCode}")
                    return
                }

                val geofenceTransition = geofencingEvent.geofenceTransition
                if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    val triggeringGeofences = geofencingEvent.triggeringGeofences
                    if (triggeringGeofences != null) {
                        for (geofence in triggeringGeofences) {
                            val spotId = geofence.requestId.toInt()
                            sendNotification(context, spotId, geofenceTransition)
                            saveSpotLog(spotId, geofenceTransition)
                        }
                    }
                }
            }
        }
    }

    private fun sendNotification(context: Context, locationId: Int, transitionType: Int) {
        val transition = if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) "entered" else "exited"
        val notificationId = (System.currentTimeMillis() % 10000).toInt()
        val builder = NotificationCompat.Builder(context, GeofenceActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("Geofence Alert")
            .setContentText("Location ID: $locationId $transition")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notify(notificationId, builder.build())
            } else {
                Log.e("GeofenceReceiver", "Notification permission not granted")
            }
        }
    }

    private fun saveSpotLog(spotId: Int, transitionType: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val entry = transitionType == Geofence.GEOFENCE_TRANSITION_ENTER
            val spotLog = SpotLog(spotId = spotId, date = System.currentTimeMillis(), entry = entry)
            try {
                spotLogDao.insert(spotLog)
                Log.d("GeofenceReceiver", "SpotLog recorded: $spotLog")
            } catch (e: Exception) {
                Log.e("GeofenceReceiver", "Error inserting SpotLog: ${e.message}")
            }
        }
    }
}

package com.example.fitbuddy.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import com.example.fitbuddy.R
import com.example.fitbuddy.geofence.GeofenceReceiver
import com.example.fitbuddy.repository.FitBuddyRepository
import com.example.fitbuddy.utils.KEY_USERNAME
import com.example.fitbuddy.utils.SHARED_PREFS_NAME
import com.example.fitbuddy.viewmodel.FitBuddyViewModel
import com.example.fitbuddy.viewmodel.FitBuddyViewModelFactory
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GeofenceService : Service() {

    private val TAG = "GeofenceServiceActivity"

    private lateinit var geofencingClient: GeofencingClient
    private lateinit var viewModel: FitBuddyViewModel
    private val viewModelStore = ViewModelStore()

    @Inject
    lateinit var repository: FitBuddyRepository

    override fun onCreate() {
        Log.e(TAG, "OnCreateService")
        super.onCreate()
        geofencingClient = LocationServices.getGeofencingClient(this)

        val factory = FitBuddyViewModelFactory(repository)
        viewModel = ViewModelProvider(viewModelStore, factory)[FitBuddyViewModel::class.java]

//        monitorGeofences()
        startForegroundService()
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "GEOFENCE_CHANNEL_ID"
            val channel = NotificationChannel(
                channelId,
                "Geofence Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, "GEOFENCE_CHANNEL_ID")
            .setContentTitle("Geofence Service")
            .setContentText("Monitoring geofences")
            .setSmallIcon(R.drawable.ic_alarm)
            .build()

        startForeground(1, notification)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun monitorGeofences() {
        GlobalScope.launch(Dispatchers.IO)  {
            val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            val username = sharedPreferences.getString(KEY_USERNAME, "") ?: ""
            val spots = viewModel.getSpotsForUser(username)

            // Clear existing geofences to avoid duplicates
//            geofencingClient.removeGeofences(getGeofencePendingIntent()).run {
//                addOnSuccessListener {
//                    println("Existing geofences removed")
//                    // Add new geofences
                    for (spot in spots) {
                        createGeofence(spot.id, LatLng(spot.latitude, spot.longitude))
                    }
//                }
//                addOnFailureListener { e ->
//                    println("Failed to remove existing geofences: ${e.message}")
//                }
//            }
        }
    }

    private fun createGeofence(spotId: Int, spot: LatLng) {
        val geofence = Geofence.Builder()
            .setRequestId(spotId.toString())
            .setCircularRegion(spot.latitude, spot.longitude, 100f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val pendingIntent = getGeofencePendingIntent()

        // Check for necessary permissions before adding geofences
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            geofencingClient.addGeofences(geofenceRequest, pendingIntent).run {
                addOnSuccessListener { println("Geofence added for spotId: $spotId") }
                addOnFailureListener { e -> println("Geofence addition failed for spotId: $spotId with error: ${e.message}") }
            }
        } else {
            println("Required permissions are not granted for geofences.")
        }
    }


    private fun getGeofencePendingIntent(): PendingIntent {
        val intent = Intent(this, GeofenceReceiver::class.java)
        return PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "OnStartService") // Call monitorGeofences() here to handle service restarts
        monitorGeofences(); // Ensures geofences are monitored on service start
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.e(TAG, "OnDestroyService")
        super.onDestroy()
        stopForeground(true)
        viewModelStore.clear()
    }
}

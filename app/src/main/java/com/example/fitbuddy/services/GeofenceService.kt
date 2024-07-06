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
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.example.fitbuddy.R
import com.example.fitbuddy.geofence.GeofenceReceiver
import com.example.fitbuddy.models.Spot
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
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class GeofenceService : Service() {

    private val TAG = "GeofenceService"

    @Inject
    lateinit var repository: FitBuddyRepository

    private lateinit var geofencingClient: GeofencingClient
    private lateinit var viewModel: FitBuddyViewModel
    private val viewModelStore = ViewModelStore()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")

        geofencingClient = LocationServices.getGeofencingClient(this)

        // Initialize ViewModel
        val factory = FitBuddyViewModelFactory(repository)
        viewModel = ViewModelProvider(viewModelStore, factory)[FitBuddyViewModel::class.java]

        // Start foreground service
        startForegroundService()

        monitorGeofences()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnspecifiedImmutableFlag", "WrongConstant")
    private fun startForegroundService() {
        val channelId = "GeofenceChannelId"
        val channelName = "Geofence Service Channel"
        val importance = NotificationManager.IMPORTANCE_LOW

        val channel = NotificationChannel(channelId, channelName, importance)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Geofence Service")
            .setContentText("Monitoring geofences")
            .setSmallIcon(R.drawable.ic_alarm)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return START_STICKY
    }

    private fun monitorGeofences() {
        Log.d(TAG, "monitorGeofences")
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val username = sharedPreferences.getString(KEY_USERNAME, "") ?: ""

        // Use CoroutineScope to launch coroutine for background processing
        CoroutineScope(Dispatchers.IO).launch {
            val spots = viewModel.getSpotsForUser(username)
            // Create geofences
            for (spot in spots) {
                createGeofence(spot.id, LatLng(spot.latitude, spot.longitude))
            }
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            geofencingClient.addGeofences(geofenceRequest, pendingIntent).run {
                addOnSuccessListener {
                    Log.d(TAG, "Geofence added for spotId: $spotId")
                }
                addOnFailureListener {
                    Log.e(TAG, "Geofence addition failed for spotId: $spotId with error: ${it.message}")
                }
            }
        } else {
            Log.e(TAG, "Permissions not granted")
        }
    }

    private fun getGeofencePendingIntent(): PendingIntent {
        val intent = Intent(this, GeofenceReceiver::class.java).apply {
            action = "com.example.ACTION_RECEIVE_GEOFENCE"
        }
        return PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        stopForeground(true)
        viewModelStore.clear()
    }
}
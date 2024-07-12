package com.application.fitbuddy.services

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.application.fitbuddy.R
import com.application.fitbuddy.geofence.GeofenceReceiver
import com.application.fitbuddy.repository.SpotRepository
import com.application.fitbuddy.utils.GEOFENCE_DWELL_DELAY
import com.application.fitbuddy.utils.KEY_USERNAME
import com.application.fitbuddy.utils.SHARED_PREFS_NAME
import com.application.fitbuddy.viewmodel.SpotViewModel
import com.application.fitbuddy.viewmodel.SpotViewModelFactory
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GeofenceService : Service() {

    private val tag = "GeofenceService"

    @Inject
    lateinit var repository: SpotRepository

    private lateinit var geofencingClient: GeofencingClient
    private lateinit var viewModel: SpotViewModel
    private val viewModelStore = ViewModelStore()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "onCreate")

        geofencingClient = LocationServices.getGeofencingClient(this)

        val factory = SpotViewModelFactory(repository)
        viewModel = ViewModelProvider(viewModelStore, factory)[SpotViewModel::class.java]

        startForegroundService()
        monitorGeofences()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        val channelId = "GeofenceChannelId"
        val channelName = "Geofence Service Channel"
        val importance = NotificationManager.IMPORTANCE_HIGH

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
        Log.d(tag, "onStartCommand")
//        monitorGeofences()
        return START_STICKY
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun monitorGeofences() {
        GlobalScope.launch(Dispatchers.IO) {
            Log.d(tag, "monitorGeofences")
            val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            val username = sharedPreferences.getString(KEY_USERNAME, "") ?: ""
            viewModel.getSpotsForUser(username,
                onSuccess = { spots ->
                    for (spot in spots) {
                        createGeofence(spot.id, LatLng(spot.latitude, spot.longitude))
                    }
                },
                onFailure = { errorMessage ->
                    Log.e(tag, "Failed to get spots: $errorMessage")
                }
            )
        }
    }

    private fun createGeofence(spotId: Int, spot: LatLng) {
        val geofence = Geofence.Builder()
            .setRequestId(spotId.toString())
            .setCircularRegion(spot.latitude, spot.longitude, 100f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setLoiteringDelay(GEOFENCE_DWELL_DELAY)
            .build()

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(this, GeofenceReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            geofencingClient.addGeofences(geofenceRequest, pendingIntent).run {
                addOnSuccessListener {
                    Log.d(tag, "Geofence added for spotId: $spotId")
                }
                addOnFailureListener {
                    Log.e(tag, "Geofence addition failed for spotId: $spotId with error: ${it.message}")
                }
            }
        } else {
            Log.e(tag, "Permissions not granted")
        }
    }

//    private fun getGeofencePendingIntent(): PendingIntent {
//        val intent = Intent(this, GeofenceReceiver::class.java)
//        return PendingIntent.getBroadcast(
//            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
//        )
//    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy")
        stopForeground(true)
        viewModelStore.clear()
    }
}

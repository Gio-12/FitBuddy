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
import com.application.fitbuddy.R
import com.google.android.gms.location.*

class LocationUpdateService : Service() {

    private val tag = "LocationUpdateService"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "onCreate")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L
        ).apply {
            setWaitForAccurateLocation(true)
            setMinUpdateIntervalMillis(1000L)
            setMaxUpdateDelayMillis(3000L)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach { location ->
                    Log.d(tag, "Location: ${location.latitude}, ${location.longitude}")
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService()
        } else {
            startForegroundServiceCompat()
        }
        requestLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            Log.e(tag, "Location permission not granted")
        }
    }

    @SuppressLint("ForegroundServiceType")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundService() {
        val channelId = "LocationUpdateChannelId"
        val channelName = "Location Update Service Channel"
        val importance = NotificationManager.IMPORTANCE_LOW

        val channel = NotificationChannel(channelId, channelName, importance)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Update Service")
            .setContentText("Requesting location updates")
            .setSmallIcon(R.drawable.ic_alarm)
            .build()

        startForeground(1, notification)
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundServiceCompat() {
        val channelId = "LocationUpdateChannelId"
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Update Service")
            .setContentText("Requesting location updates")
            .setSmallIcon(R.drawable.ic_alarm)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

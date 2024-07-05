package com.example.fitbuddy.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.fitbuddy.R

class ActionService : Service(), SensorEventListener {

    private val binder = LocalBinder()
    private lateinit var notificationManager: NotificationManager
    private lateinit var sensorManager: SensorManager
    private var steps: Int = 0

    inner class LocalBinder : Binder() {
        fun getService(): ActionService = this@ActionService
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val selectedActivityType = intent?.getStringExtra("selectedActionType") ?: "WALKING"

        if (selectedActivityType == "WALKING") {
            trackSteps()
        }

        startForegroundService()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, "action_channel")
            .setContentTitle("Action Tracker")
            .setContentText("Monitoring your action")
            .setSmallIcon(R.drawable.ic_action)
            .build()

        startForeground(1, notification)
    }

    private fun trackSteps() {
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)?.also { stepCounter ->
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            steps = event.values[0].toInt()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun getStepCount(): Int {
        return steps
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "action_channel",
            "Action Monitoring",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }
}

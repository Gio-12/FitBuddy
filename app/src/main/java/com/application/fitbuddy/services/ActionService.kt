package com.application.fitbuddy.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.application.fitbuddy.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActionService : Service(), SensorEventListener {

    private val tag = "ActionService"
    private val binder = LocalBinder()
    private lateinit var notificationManager: NotificationManager
    private var initialSteps: Int = -1
    private var currentSteps: Int = 0

    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("action_service_prefs", Context.MODE_PRIVATE)
    }

    private val sensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val sensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    private val sensorEventChannel = Channel<Unit>(capacity = Channel.CONFLATED)

    inner class LocalBinder : Binder() {
        fun getService(): ActionService = this@ActionService
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        initialSteps = sharedPreferences.getInt("initial_steps", -1)
        if (sensor == null) {
            Log.e(tag, "Step counter sensor is not present on this device")
        }
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val selectedActivityType = intent?.getStringExtra("selectedActionType") ?: "WALKING"

        if (selectedActivityType == "WALKING") {
            CoroutineScope(Dispatchers.IO).launch {
                trackSteps()
            }
        }

        startForegroundService()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        sharedPreferences.edit().putInt("initial_steps", initialSteps).apply()
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

    private suspend fun trackSteps() {
        withContext(Dispatchers.Main) {
            Log.d(tag, "Registering sensor listener...")

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        val stepsSinceLastReboot = it.values[0].toInt()
                        if (initialSteps == -1) {
                            initialSteps = stepsSinceLastReboot
                        }
                        currentSteps = stepsSinceLastReboot - initialSteps
                        Log.d(tag, "Steps in session: $currentSteps")
                        sensorEventChannel.trySend(Unit)
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    Log.d(tag, "Accuracy changed to: $accuracy")
                }
            }

            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)

            sensorManager.unregisterListener(listener)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {}

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun getStepCount(): Int {
        return currentSteps
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

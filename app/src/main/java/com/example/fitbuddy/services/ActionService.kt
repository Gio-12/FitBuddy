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
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.fitbuddy.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class ActionService : Service(), SensorEventListener {

    var TAG = "ActionService"

    private val binder = LocalBinder()
    private lateinit var notificationManager: NotificationManager
    private var steps: Int = 0

    private val sensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    private val sensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) }

    inner class LocalBinder : Binder() {
        fun getService(): ActionService = this@ActionService
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (sensor == null) {
            Log.e(TAG,"Step counter sensor is not present on this device")
        }
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

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
            suspendCancellableCoroutine { continuation ->
                Log.d(TAG, "Registering sensor listener... ")

                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        event?.let {
                            val stepsSinceLastReboot = it.values[0].toInt()
                            Log.d(TAG, "Steps since last reboot: $stepsSinceLastReboot")
                            steps = stepsSinceLastReboot
                            continuation.resume(Unit)
                        }
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                        Log.d(TAG, "Accuracy changed to: $accuracy")
                    }
                }
                sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
                continuation.invokeOnCancellation {
                    sensorManager.unregisterListener(listener)
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {}

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

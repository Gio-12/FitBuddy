package com.application.fitbuddy.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.application.fitbuddy.R
import com.application.fitbuddy.models.Action
import com.application.fitbuddy.services.ActionService
import com.application.fitbuddy.utils.KEY_DEFAULT_TOGGLE
import com.application.fitbuddy.utils.KEY_USERNAME
import com.application.fitbuddy.utils.SHARED_PREFS_NAME
import com.application.fitbuddy.utils.isBackgroundActivityEnabled
import com.application.fitbuddy.viewmodel.ActionViewModel
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : MenuActivity() {

    private val tag = "MainActivity"

    // Views
    private lateinit var imageActivity: ImageView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var radioGroupActivities: RadioGroup
    private lateinit var chronometer: TextView

    // Action values
    private var selectedActionType: String = "WALKING"
    private var isActionStarted = false

    // Service
    private var actionService: ActionService? = null
    private var isBound = false

    // Timer
    private var startTime: Long = 0
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    // ViewModel
    private val actionViewModel: ActionViewModel by viewModels()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as ActionService.LocalBinder
            actionService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
            permissions[Manifest.permission.ACTIVITY_RECOGNITION] == true) {
            Log.d(tag, "All required permissions granted")
        } else {
            Log.d(tag, "Permission denied")
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (getLoggedUser() == null) {
            redirectToLogin()
            return
        }

        setContentView(R.layout.main_activity)
        setSupportActionBar(findViewById(R.id.toolbar))

        imageActivity = findViewById(R.id.image_activity)
        btnStart = findViewById(R.id.button_start)
        btnStop = findViewById(R.id.button_stop)
        radioGroupActivities = findViewById(R.id.radioGroup_activities)
        chronometer = findViewById(R.id.chronometer)

        btnStart.setOnClickListener { startActivityRecognition() }
        btnStop.setOnClickListener { stopActivityRecognition() }

        // Retrieve default activity type from shared preferences
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        selectedActionType = sharedPreferences.getString(KEY_DEFAULT_TOGGLE, "WALKING") ?: "WALKING"

        // Update the radio button selection
        when (selectedActionType) {
            "STILL" -> {
                radioGroupActivities.check(R.id.radioButton_still)
                setActionImage(R.drawable.still_image)
            }
            "WALKING" -> {
                radioGroupActivities.check(R.id.radioButton_walking)
                setActionImage(R.drawable.walking_image)
            }
            "DRIVING" -> {
                radioGroupActivities.check(R.id.radioButton_driving)
                setActionImage(R.drawable.driving_image)
            }
        }

        radioGroupActivities.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButton_still -> {
                    setActionImage(R.drawable.still_image)
                    selectedActionType = "STILL"
                    saveDefaultActionTypeToPreferences(selectedActionType)
                }
                R.id.radioButton_walking -> {
                    setActionImage(R.drawable.walking_image)
                    selectedActionType = "WALKING"
                    saveDefaultActionTypeToPreferences(selectedActionType)
                }
                R.id.radioButton_driving -> {
                    setActionImage(R.drawable.driving_image)
                    selectedActionType = "DRIVING"
                    saveDefaultActionTypeToPreferences(selectedActionType)
                }
            }
        }

        checkPermissions()
    }

    private fun getLoggedUser(): String? {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_USERNAME, "")
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        // Bind to the service
        Intent(this, ActionService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        // Unbind from the service
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isActionStarted) {
            Log.d(tag, "App is closing, ending the ongoing activity")
            val endTime = System.currentTimeMillis()
            val actionId = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE).getLong("currentActivityId", -1)
            val steps = actionService?.getStepCount() ?: 0
            saveEndAction(actionId, endTime, steps)
            isActionStarted = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startActivityRecognition() {
        Log.d(tag, "Starting Activity Recognition")
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED) {
            val action = Action(
                userUsername = getUsernameFromPreferences(),
                actionType = selectedActionType,
                steps = 0,
                startTime = System.currentTimeMillis(),
                endTime = 0,
            )
            lifecycleScope.launch {
                val actionId = saveAction(action)
                if (actionId == -1L) {
                    // Handle insertion failure
                    Log.e(tag, "Failed to insert action into database")
                } else {
                    // Handle successful insertion
                    Log.d(tag, "Action inserted successfully with ID: $actionId")
                }
                val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
                sharedPreferences.edit().putLong("currentActionId", actionId).apply()

                isActionStarted = true
                btnStart.isEnabled = false
                btnStop.isEnabled = true
                setRadioButtonsEnabled(false)

                // Start the chronometer
                startTime = System.currentTimeMillis()
                runnable = object : Runnable {
                    @SuppressLint("DefaultLocale")
                    override fun run() {
                        val elapsedMillis = System.currentTimeMillis() - startTime
                        val seconds = (elapsedMillis / 1000) % 60
                        val minutes = (elapsedMillis / (1000 * 60)) % 60
                        val hours = (elapsedMillis / (1000 * 60 * 60)) % 24
                        chronometer.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                        handler.postDelayed(this, 1000)
                    }
                }
                handler.post(runnable!!)

                // Start the ActivityMonitoringService with the action ID
                val serviceIntent = Intent(this@MainActivity, ActionService::class.java)
                serviceIntent.putExtra("actionId", actionId)
                serviceIntent.putExtra("selectedActionType", selectedActionType)
                ContextCompat.startForegroundService(this@MainActivity, serviceIntent)
            }
        } else {
            checkPermissions()
        }
    }

    private fun stopActivityRecognition() {
        if (!isActionStarted) {
            showStartActionNotify()
            return
        }
        Log.d(tag, "Stopping Activity Recognition")
        val endTime = System.currentTimeMillis()

        // Retrieve the correct action ID
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val actionId = sharedPreferences.getLong("currentActionId", -1)

        // Get step count from the service
        val steps = actionService?.getStepCount() ?: 0

        saveEndAction(actionId, endTime, steps)

        showStartActionEndingNotify()

        isActionStarted = false
        btnStart.isEnabled = true
        btnStop.isEnabled = false
        setRadioButtonsEnabled(true)

        // Stop the chronometer
        handler.removeCallbacks(runnable!!)

        // Stop the ActivityMonitoringService
        val serviceIntent = Intent(this, ActionService::class.java)
        stopService(serviceIntent)

        if (isBackgroundActivityEnabled) {
            Log.d(tag, "Background activity recognition is enabled. Starting background service.")
        }
    }


    private suspend fun saveAction(action: Action): Long {
        var actionId: Long = -1 // default value

        actionViewModel.insert(action,
            onSuccess = { id ->
                actionId = id
                Log.d(tag, "Action inserted successfully with ID: $actionId")
            },
            onFailure = { errorMessage ->
                showError(errorMessage)
            }
        )
        return actionId
    }

    private fun saveEndAction(actionId: Long, endTime: Long, steps: Int) {
        Log.d(tag, "Attempting to save end time to database for action ID $actionId at $endTime")
        actionViewModel.getActionById(actionId,
            onSuccess = { action ->
                if (action != null) {
                    action.endTime = endTime
                    if (action.actionType == "WALKING") {
                        action.steps = steps
                    }
                    actionViewModel.update(action,
                        onSuccess = {
                            Log.d(tag, "Action updated successfully")
                        },
                        onFailure = { errorMessage ->
                            showError(errorMessage)
                        }
                    )
                } else {
                    Log.e(tag, "Action not found for ID: $actionId")
                }
            },
            onFailure = { errorMessage ->
                showError(errorMessage)
            }
        )
    }

    private fun showStartActionNotify() {
        AlertDialog.Builder(this)
            .setTitle("Notify")
            .setMessage("Please start the activity first.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showStartActionEndingNotify() {
        AlertDialog.Builder(this)
            .setTitle("Notify")
            .setMessage("You just finished an activity!")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun setActionImage(resourceId: Int) {
        Glide.with(this)
            .load(resourceId)
            .into(imageActivity)
    }

    private fun setRadioButtonsEnabled(enabled: Boolean) {
        for (i in 0 until radioGroupActivities.childCount) {
            radioGroupActivities.getChildAt(i).isEnabled = enabled
        }
    }

    private fun getUsernameFromPreferences(): String {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_USERNAME, "") ?: ""
    }

    private fun saveDefaultActionTypeToPreferences(actionType: String) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(KEY_DEFAULT_TOGGLE, actionType).apply()
    }

    private fun showError(errorMessage: String) {
        Log.e(tag, errorMessage)
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }
}
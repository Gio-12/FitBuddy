package com.example.fitbuddy.activities


class MainActivity : MenuActivity() {

}
//import android.Manifest
//import android.content.*
//import android.content.pm.PackageManager
//import android.os.*
//import android.util.Log
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.RadioGroup
//import android.widget.TextView
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.activity.viewModels
//import androidx.annotation.RequiresApi
//import androidx.appcompat.app.AlertDialog
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.lifecycleScope
//import com.bumptech.glide.Glide
//import com.example.fitbuddy.R
//import com.example.fitbuddy.models.Action
//import com.example.fitbuddy.services.ActionService
//import com.example.fitbuddy.utils.KEY_DEFAULT_TOGGLE
//import com.example.fitbuddy.utils.KEY_USERNAME
//import com.example.fitbuddy.utils.SHARED_PREFS_NAME
//import com.example.fitbuddy.utils.isBackgroundActivityEnabled
//import com.example.fitbuddy.viewmodel.FitBuddyViewModel
//import kotlinx.coroutines.*
//
//class MainActivity : MenuActivity() {
//
//    private val TAG = "MainActivity"
//
//    private lateinit var imageActivity: ImageView
//    private lateinit var btnStart: Button
//    private lateinit var btnStop: Button
//    private lateinit var radioGroupActivities: RadioGroup
//    private lateinit var chronometer: TextView
//
//    private var selectedActionType: String = "WALKING"
//    private var isActivityStarted = false
//    private var isBound = false
//    private var service: ActionService? = null
//    private var startTime: Long = 0
//    private var handler: Handler = Handler(Looper.getMainLooper())
//    private var runnable: Runnable? = null
//
//    // ViewModel
//    private val viewModel: FitBuddyViewModel by viewModels()
//
//    private val requestPermissionLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions()
//    ) { permissions ->
//        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
//            permissions[Manifest.permission.ACTIVITY_RECOGNITION] == true) {
//            Log.d(TAG, "All required permissions granted")
//        } else {
//            Log.d(TAG, "Permission denied")
//        }
//    }
//
//    private val connection = object : ServiceConnection {
//        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
//            val binder = binder as ActionService.LocalBinder
//            service = binder.getService()
//            isBound = true
//        }
//
//        override fun onServiceDisconnected(arg0: ComponentName) {
//            isBound = false
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.Q)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        if (!isUserLoggedIn()) {
//            redirectToLogin()
//            return
//        }
//
//        setContentView(R.layout.main_activity)
//
//        imageActivity = findViewById(R.id.image_activity)
//        btnStart = findViewById(R.id.button_start)
//        btnStop = findViewById(R.id.button_stop)
//        radioGroupActivities = findViewById(R.id.radioGroup_activities)
//        chronometer = findViewById(R.id.chronometer)
//
//        btnStart.setOnClickListener { startActivityRecognition() }
//        btnStop.setOnClickListener { stopActivityRecognition() }
//
//        // Retrieve default activity type from shared preferences
//        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
//        val defaultActionType = sharedPreferences.getString(KEY_DEFAULT_TOGGLE, "WALKING") ?: "WALKING"
//
//        // Update the selected activity type based on the default value
//        selectedActionType = defaultActionType
//
//        // Update the radio button selection
//        when (defaultActionType) {
//            "STILL" -> {
//                radioGroupActivities.check(R.id.radioButton_still)
//                setActivityImage(R.drawable.still_image)
//            }
//            "WALKING" -> {
//                radioGroupActivities.check(R.id.radioButton_walking)
//                setActivityImage(R.drawable.walking_image)
//            }
//            "DRIVING" -> {
//                radioGroupActivities.check(R.id.radioButton_driving)
//                setActivityImage(R.drawable.driving_image)
//            }
//        }
//
//        radioGroupActivities.setOnCheckedChangeListener { _, checkedId ->
//            when (checkedId) {
//                R.id.radioButton_still -> {
//                    setActivityImage(R.drawable.still_image)
//                    selectedActionType = "STILL"
//                    saveDefaultActivityTypeToPreferences(selectedActionType)
//                }
//                R.id.radioButton_walking -> {
//                    setActivityImage(R.drawable.walking_image)
//                    selectedActionType = "WALKING"
//                    saveDefaultActivityTypeToPreferences(selectedActionType)
//                }
//                R.id.radioButton_driving -> {
//                    setActivityImage(R.drawable.driving_image)
//                    selectedActionType = "DRIVING"
//                    saveDefaultActivityTypeToPreferences(selectedActionType)
//                }
//            }
//        }
//
//        checkPermissions()
//    }
//
//    private fun isUserLoggedIn(): Boolean {
//        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
//        val username = sharedPreferences.getString(KEY_USERNAME, "")
//        return !username.isNullOrEmpty()
//    }
//
//    private fun redirectToLogin() {
//        val intent = Intent(this, LoginActivity::class.java)
//        startActivity(intent)
//        finish()
//    }
//
//    override fun onStart() {
//        super.onStart()
//        // Bind to the service
//        Intent(this, ActionService::class.java).also { intent ->
//            bindService(intent, connection, Context.BIND_AUTO_CREATE)
//        }
//    }
//
//    override fun onStop() {
//        super.onStop()
//        // Unbind from the service
//        if (isBound) {
//            unbindService(connection)
//            isBound = false
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        if (isActivityStarted) {
//            Log.d(TAG, "App is closing, ending the ongoing activity")
//            val endTime = System.currentTimeMillis()
//            val activityId = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE).getLong("currentActivityId", -1)
//            val steps = service?.getStepCount() ?: 0
//            saveEndActivityToDatabase(activityId, endTime, selectedActionType, steps)
//            isActivityStarted = false
//        }
//    }
//
//    @OptIn(DelicateCoroutinesApi::class)
//    @RequiresApi(Build.VERSION_CODES.Q)
//    private fun startActivityRecognition() {
//        Log.d(TAG, "Starting Activity Recognition")
//        if (ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED &&
//            ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACTIVITY_RECOGNITION
//            ) == PackageManager.PERMISSION_GRANTED) {
//            val action = Action(
//                userUsername = getUsernameFromPreferences(),
//                actionType = selectedActionType,
//                steps = 0,
//                startTime = System.currentTimeMillis(),
//                endTime = 0,
//            )
//            lifecycleScope.launch {
//                val actionId = viewModel.insertAction(action)
//                withContext(Dispatchers.Main) {
//                    val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
//                    sharedPreferences.edit().putLong("currentActionId", actionId).apply()
//                    isActivityStarted = true
//                    btnStart.isEnabled = false
//                    btnStop.isEnabled = true
//                    setRadioButtonsEnabled(false)
//
//                    // start the chronometer
//                    startTime = System.currentTimeMillis()
//                    runnable = object : Runnable {
//                        override fun run() {
//                            val elapsedMillis = System.currentTimeMillis() - startTime
//                            val seconds = (elapsedMillis / 1000) % 60
//                            val minutes = (elapsedMillis / (1000 * 60)) % 60
//                            val hours = (elapsedMillis / (1000 * 60 * 60)) % 24
//                            chronometer.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
//                            handler.postDelayed(this, 1000)
//                        }
//                    }
//                    handler.post(runnable!!)
//
//                    // Start the ActivityMonitoringService with the activity ID
//                    val serviceIntent = Intent(this@MainActivity, ActionService::class.java)
//                    serviceIntent.putExtra("actionId", actionId)
//                    serviceIntent.putExtra("selectedActionType", selectedActionType)
//                    ContextCompat.startForegroundService(this@MainActivity, serviceIntent)
//                }
//            }
//        } else {
//            checkPermissions()
//        }
//    }
//
//    private fun stopActivityRecognition() {
//        if (!isActivityStarted) {
//            showStartActivityReminder()
//            return
//        }
//        Log.d(TAG, "Stopping Activity Recognition")
//        val endTime = System.currentTimeMillis()
//
//        // Retrieve the correct activity ID
//        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
//        val activityId = sharedPreferences.getLong("currentActivityId", -1)
//
//        // Get step count from the service
//        val steps = service?.getStepCount() ?: 0
//
//        saveEndActivityToDatabase(activityId, endTime, selectedActionType, steps)
//        showStartActivityEndingNotify()
//        isActivityStarted = false
//        btnStart.isEnabled = true
//        btnStop.isEnabled = false
//        setRadioButtonsEnabled(true)
//
//        // stop the chronometer
//        handler.removeCallbacks(runnable!!)
//
//        // Stop the ActivityMonitoringService
//        val serviceIntent = Intent(this, ActionService::class.java)
//        stopService(serviceIntent)
//
//        if (isBackgroundActivityEnabled) {
//            Log.d(TAG, "Background activity recognition is enabled. Starting background service.")
//        }
//    }
//
//    private fun saveEndActivityToDatabase(activityId: Long, endTime: Long, activityType: String, steps: Int) {
//        Log.d(TAG, "Attempting to save end time to database for activity ID $activityId at $endTime")
//        lifecycleScope.launch {
//            try {
//                viewModel.updateAction(activityId, endTime, activityType, steps)
//                Log.d(TAG, "End time and steps updated for activity ID: $activityId")
//            } catch (e: Exception) {
//                Log.e(TAG, "Error saving end activity to database", e)
//            }
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.Q)
//    private fun checkPermissions() {
//        val permissionsToRequest = mutableListOf<String>()
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
//        }
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
//            permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
//        }
//        if (permissionsToRequest.isNotEmpty()) {
//            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
//        }
//    }
//
//    private fun showStartActivityReminder() {
//        AlertDialog.Builder(this)
//            .setTitle("Reminder")
//            .setMessage("Please start the activity first.")
//            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
//            .show()
//    }
//
//    private fun showStartActivityEndingNotify() {
//        AlertDialog.Builder(this)
//            .setTitle("Notify")
//            .setMessage("You just finished an activity!")
//            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
//            .show()
//    }
//
//    private fun setActivityImage(resourceId: Int) {
//        Glide.with(this)
//            .load(resourceId)
//            .into(imageActivity)
//    }
//
//    private fun getUsernameFromPreferences(): String {
//        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
//        return sharedPreferences.getString(KEY_USERNAME, "") ?: ""
//    }
//
//    private fun setRadioButtonsEnabled(enabled: Boolean) {
//        for (i in 0 until radioGroupActivities.childCount) {
//            radioGroupActivities.getChildAt(i).isEnabled = enabled
//        }
//    }
//
//    private fun saveDefaultActivityTypeToPreferences(activityType: String) {
//        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
//        sharedPreferences.edit().putString(KEY_DEFAULT_TOGGLE, activityType).apply()
//    }
//
//    @Deprecated("Deprecated in Java", ReplaceWith("moveTaskToBack(true)"))
//    override fun onBackPressed() {
//        super.onBackPressed()
//        moveTaskToBack(true)
//    }
//}

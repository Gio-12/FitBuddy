package com.application.fitbuddy.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.application.fitbuddy.R
import com.application.fitbuddy.geofence.GeofenceReceiver
import com.application.fitbuddy.models.Spot
import com.application.fitbuddy.services.GeofenceService
import com.application.fitbuddy.utils.CHANNEL_ID
import com.application.fitbuddy.utils.GEOFENCE_DWELL_DELAY
import com.application.fitbuddy.utils.GEOFENCE_RADIUS
import com.application.fitbuddy.utils.KEY_USERNAME
import com.application.fitbuddy.utils.LOCATION_REQUEST_CODE
import com.application.fitbuddy.utils.SHARED_PREFS_NAME
import com.application.fitbuddy.utils.WRITE_EXTERNAL_STORAGE_REQUEST_CODE
import com.application.fitbuddy.viewmodel.SpotViewModel
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.Geofence.NEVER_EXPIRE
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class GeofenceActivity : MenuActivity(), OnMapReadyCallback {

    private val tag = "GeofenceActivity"
    private lateinit var map: GoogleMap

    // VIEWMODELS
    private val spotViewModel: SpotViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.geofence_activity)
        setSupportActionBar(findViewById(R.id.toolbar))

        createNotificationChannel()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        checkAndRequestPermissions()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

        if (!isLocationPermissionGranted()) {
            requestLocationPermissions()
        } else {
            enableUserLocation()
        }

        setMapLongClick(map)
        loadSavedGeofence() // Pass a flag to control re-registration
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            getLocationName(latLng) { locationName ->
                addMarkerAndCircle(latLng, locationName)
                saveSpotAndCreateGeofence(latLng, locationName)
            }
        }
    }

    private fun addMarkerAndCircle(latLng: LatLng, locationName: String?) {
        map.addMarker(
            MarkerOptions().position(latLng).title(locationName ?: "Geofence Location")
        )?.showInfoWindow()
        map.addCircle(
            CircleOptions()
                .center(latLng)
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(70, 150, 150, 150))
                .radius(GEOFENCE_RADIUS.toDouble())
        )
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    private fun saveSpotAndCreateGeofence(latLng: LatLng, locationName: String?) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val username = sharedPreferences.getString(KEY_USERNAME, "") ?: ""

        lifecycleScope.launch {
            val spot = Spot(username, locationName ?: "", latLng.latitude, latLng.longitude)
            spotViewModel.insert(spot, onSuccess = { spotId ->
                createGeofence(spotId.toInt(), latLng)
            }, onFailure = { errorMessage ->
                showError(errorMessage)
            })
        }
    }

    private fun createGeofence(locationId: Int, latLng: LatLng) {
        val geofence = Geofence.Builder()
            .setRequestId(locationId.toString())
            .setCircularRegion(latLng.latitude, latLng.longitude, GEOFENCE_RADIUS)
            .setExpirationDuration(NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setLoiteringDelay(GEOFENCE_DWELL_DELAY)
            .build()

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .addGeofence(geofence)
            .build()

        val geofencePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(this, GeofenceReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        LocationServices.getGeofencingClient(this).addGeofences(geofenceRequest, geofencePendingIntent)
    }

    private fun loadSavedGeofence() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val userUsername = sharedPreferences.getString(KEY_USERNAME, "") ?: ""
        lifecycleScope.launch {
            try {
                spotViewModel.getSpotsForUser(userUsername, onSuccess = { spots ->
                    spots.forEach { spot ->
                        val latLng = LatLng(spot.latitude, spot.longitude)
                        addMarkerAndCircle(latLng, spot.name)
                        createGeofence(spot.id, latLng)
                    }
                }, onFailure = { errorMessage ->
                    showError(errorMessage)
                })
            } catch (e: Exception) {
                Log.e(tag, "Error loading saved geofences", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun isLocationPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestLocationPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
        ActivityCompat.requestPermissions(this, permissions, LOCATION_REQUEST_CODE)
    }

    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        }
    }

    override fun onPause() {
        super.onPause()
        Intent(this, GeofenceService::class.java).also { intent ->
            startService(intent)
        }
    }

    override fun onRestart() {
        super.onRestart()
        Intent(this, GeofenceService::class.java).also { intent ->
            stopService(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Intent(this, GeofenceService::class.java).also { intent ->
            stopService(intent)
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("moveTaskToBack(true)"))
    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )

        val permissionsToRequest = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(tag, "${permissions[i]} permission granted")
                } else {
                    Log.e(tag, "${permissions[i]} permission denied")
                }
            }
        }
    }

    private fun getLocationName(latLng: LatLng, callback: (String?) -> Unit) {
        val geocoder = Geocoder(this, Locale.getDefault())
        lifecycleScope.launch {
            try {
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (addresses != null) {
                    if (addresses.isNotEmpty()) {
                        callback(addresses[0]?.getAddressLine(0))
                    } else {
                        callback(null)
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Geocoding failed", e)
                callback(null)
            }
        }
    }

    private fun showError(errorMessage: String) {
        Log.e(tag, errorMessage)
    }
}
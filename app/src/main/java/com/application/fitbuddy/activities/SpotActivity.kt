package com.application.fitbuddy.activities

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.application.fitbuddy.R
import com.application.fitbuddy.adapters.SpotLogsAdapter
import com.application.fitbuddy.utils.KEY_USERNAME
import com.application.fitbuddy.utils.LOCATION_PERMISSION_REQUEST_CODE
import com.application.fitbuddy.utils.SHARED_PREFS_NAME
import com.application.fitbuddy.viewmodel.SpotLogViewModel
import com.application.fitbuddy.viewmodel.SpotViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SpotActivity : MenuActivity(), OnMapReadyCallback {

    private val tag = "SpotActivity"

    private val spotViewModel: SpotViewModel by viewModels()
    private val spotLogViewModel: SpotLogViewModel by viewModels()

    private lateinit var googleMap: GoogleMap
    private lateinit var spotLogsRecyclerView: RecyclerView
    private lateinit var panelHeader: TextView
    private lateinit var expandedPanel: View
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.spot_activity)
        setSupportActionBar(findViewById(R.id.toolbar))

        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val defaultUsername = sharedPreferences.getString(KEY_USERNAME, "") ?: ""
        username = intent.getStringExtra("username") ?: defaultUsername

        spotLogsRecyclerView = findViewById(R.id.spot_logs_recycler_view)
        panelHeader = findViewById(R.id.panel_header)
        expandedPanel = findViewById(R.id.expanded_panel)
        drawerLayout = findViewById(R.id.drawer_layout)

        spotLogsRecyclerView.layoutManager = LinearLayoutManager(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = true

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
        loadUserSpots(username)
    }

    private fun loadUserSpots(username: String) {
        lifecycleScope.launch {
            try {
                spotViewModel.getSpotsForUser(username,
                    onSuccess = { spots ->
                        for (spot in spots) {
                            val spotLocation = LatLng(spot.latitude, spot.longitude)
                            val marker = googleMap.addMarker(MarkerOptions().position(spotLocation).title(spot.name))
                            marker?.tag = spot.id
                            googleMap.setOnMarkerClickListener { marker ->
                                marker?.let {
                                    showSpotLogs(marker.tag as Int)
                                }
                                true
                            }
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(spotLocation, 10f))
                        }
                    },
                    onFailure = { errorMessage ->
                        showError(errorMessage)
                    })
            } catch (e: Exception) {
                Log.e(tag, "Error loading spots: ${e.message}")
            }
        }
    }

    private fun showSpotLogs(spotId: Int) {
        expandedPanel.visibility = View.VISIBLE
        drawerLayout.openDrawer(expandedPanel)

        lifecycleScope.launch {
            try {
                spotLogViewModel.getLogsForSpot(spotId,
                    onSuccess = { spotLogs ->
                        spotLogsRecyclerView.adapter = SpotLogsAdapter(spotLogs)
                    },
                    onFailure = { errorMessage ->
                        showError(errorMessage)
                    })
            } catch (e: Exception) {
                Log.e(tag, "Error loading logs: ${e.message}")
            }
        }
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

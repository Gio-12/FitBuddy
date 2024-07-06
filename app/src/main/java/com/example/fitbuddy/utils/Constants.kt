package com.example.fitbuddy.utils

const val CUSTOM_INTENT_GEOFENCE = "GEOFENCE-TRANSITION-INTENT-ACTION"
const val CUSTOM_REQUEST_CODE_GEOFENCE = 1001
const val GEOFENCE_RADIUS = 100f
const val SHARED_PREFS_NAME = "FitBuddy_prefs"
const val KEY_USERNAME = "key_username"
const val KEY_DEFAULT_TOGGLE = "key_default_toggle"
const val KEY_NOTIFICATION_INTERVAL = "key_notification_interval"
const val GEOFENCE_EXPIRATION = 10 * 24 * 60 * 60 * 1000L // 10 days
const val GEOFENCE_DWELL_DELAY = 10 * 1000 // 10 secs
const val LOCATION_REQUEST_CODE = 123
const val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 100
var isBackgroundActivityEnabled: Boolean = false
package com.example.fitbuddy.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbuddy.R
import com.example.fitbuddy.activities.MainActivity

open class MenuActivity : AppCompatActivity() {

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_navbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_mn_home -> {
                navigateTo(MainActivity::class.java)
                true
            }
            R.id.action_mn_geofence -> {
                navigateTo(GeofenceActivity::class.java)
                true
            }
            R.id.action_mn_spots -> {
                navigateTo(SpotActivity::class.java)
                true
            }
            R.id.action_mn_graphs -> {
                navigateTo(GraphActivity::class.java)
                true
            }
            R.id.action_mn_profile -> {
                navigateTo(ProfileActivity::class.java)
                true
            }
            R.id.action_mn_search -> {
                navigateTo(SearchActivity::class.java)
                true
            }
            R.id.action_logout -> {
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                navigateTo(LoginActivity::class.java)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateTo(targetClass: Class<*>) {
        val intent = Intent(this, targetClass)
        startActivity(intent)
        finish()
    }
}

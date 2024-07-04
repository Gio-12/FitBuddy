package com.example.fitbuddy.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fitbuddy.R
import com.example.fitbuddy.utils.KEY_USERNAME
import com.example.fitbuddy.utils.SHARED_PREFS_NAME
import com.example.fitbuddy.viewmodel.FitBuddyViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"

    private lateinit var textUsername: EditText
    private lateinit var textPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoToSignup: Button

    // ViewModel
    private val viewModel: FitBuddyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        textUsername = findViewById(R.id.login_username)
        textPassword = findViewById(R.id.login_password)
        btnLogin = findViewById(R.id.btn_login)
        btnGoToSignup = findViewById(R.id.btn_go_to_signup)

        btnLogin.setOnClickListener {
            val username = textUsername.text.toString()
            val password = textPassword.text.toString()

            Log.d(TAG, "Login button clicked with username: $username")

            if (username.isNotEmpty() && password.isNotEmpty()) {
                loginWithRoomDB(username, password)
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Empty username or password")
            }
        }

        btnGoToSignup.setOnClickListener {
            Log.d(TAG, "Go to register button clicked")
            val intent = Intent(applicationContext, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    //TODO ROOMDB
    private fun loginWithRoomDB(username: String, password: String) {
        lifecycleScope.launch {
            Log.d(TAG, "Checking user credentials in Room for username: $username")
            val user = viewModel.getUserWithPassword(username, password)
            if (user != null) {
                saveUsernameToPreferences(username)
                Log.d(TAG, "Login successful for username: $username")
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Log.d(TAG, "Invalid username or password for username: $username")
            }
        }
    }

    private fun saveUsernameToPreferences(username: String) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(KEY_USERNAME, username)
            apply()
        }
    }
}
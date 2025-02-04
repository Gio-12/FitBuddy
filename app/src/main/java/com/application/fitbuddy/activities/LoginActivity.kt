package com.application.fitbuddy.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.application.fitbuddy.R
import com.application.fitbuddy.utils.KEY_USERNAME
import com.application.fitbuddy.utils.SHARED_PREFS_NAME
import com.application.fitbuddy.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val tag = "LoginActivity"

    private lateinit var textUsername: EditText
    private lateinit var textPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoToSignup: Button

    // ViewModel
    private val userViewModel: UserViewModel by viewModels()

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

            Log.d(tag, "Login button clicked with username: $username")

            if (username.isNotEmpty() && password.isNotEmpty()) {
                login(username, password)
            } else {
                Log.d(tag, "Empty username or password")
                showAlert("Login Error", "Username or password cannot be empty")
            }
        }

        btnGoToSignup.setOnClickListener {
            Log.d(tag, "Go to register button clicked")
            val intent = Intent(applicationContext, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun login(username: String, password: String) {
        lifecycleScope.launch {
            Log.d(tag, "Checking user credentials in Room for username: $username")
            userViewModel.getUserWithPassword(
                username,
                password,
                onSuccess = { user ->
                    if (user != null) {
                        saveUsernameToPreferences(username)
                        Log.d(tag, "Login successful for username: $username")
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.d(tag, "Invalid username or password for username: $username")
                        showAlert("Login Error", "Invalid username or password")
                    }
                },
                onFailure = { errorMessage ->
                    showError(errorMessage)
                    showAlert("Login Error", "An error occurred: $errorMessage")
                }
            )
        }
    }

    private fun saveUsernameToPreferences(username: String) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(KEY_USERNAME, username)
            apply()
        }
    }

    private fun showError(errorMessage: String) {
        Log.e(tag, errorMessage)
    }

    private fun showAlert(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }
}

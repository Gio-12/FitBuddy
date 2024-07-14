package com.application.fitbuddy.activities

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
import com.application.fitbuddy.models.User
import com.application.fitbuddy.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignupActivity : AppCompatActivity() {

    private val tag = "SignupActivity"

    private lateinit var signupUsername: EditText
    private lateinit var signupPassword: EditText
    private lateinit var btnSignup: Button
    private lateinit var btnGoToLogin: Button

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_activity)

        signupUsername = findViewById(R.id.signup_username)
        signupPassword = findViewById(R.id.signup_password)
        btnSignup = findViewById(R.id.btn_signup)
        btnGoToLogin = findViewById(R.id.btn_go_to_login)

        btnSignup.setOnClickListener {
            val username = signupUsername.text.toString()
            val password = signupPassword.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                signup(username, password)
            } else {
                Log.e(tag, "Please enter username and password")
                showAlert("Signup Error", "Please enter username and password")
            }
        }

        btnGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun signup(username: String, password: String) {
        lifecycleScope.launch {
            userViewModel.getUserByUsername(
                username,
                onSuccess = { checkUser ->
                    if (checkUser != null) {
                        Log.d(tag, "Username already taken: $username")
                        showAlert("Signup Error", "Username already taken")
                    } else {
                        signupNewUser(username, password)
                    }
                },
                onFailure = { errorMessage ->
                    showError(errorMessage)
                    showAlert("Signup Error", "An error occurred: $errorMessage")
                }
            )
        }
    }

    private fun signupNewUser(username: String, password: String) {
        lifecycleScope.launch {
            val newUser = User(username, password)
            userViewModel.insert(
                newUser,
                onSuccess = {
                    Log.d(tag, "Registration Successful in RoomDB")
                    startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                    finish()
                },
                onFailure = { errorMessage ->
                    showError(errorMessage)
                    showAlert("Signup Error", "An error occurred: $errorMessage")
                }
            )
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

package com.example.fitbuddy.activities

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
import com.example.fitbuddy.models.User
import com.example.fitbuddy.viewmodel.FitBuddyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignupActivity : AppCompatActivity() {

    private val tag = "SignupActivity"

    private lateinit var signupUsername: EditText
    private lateinit var signupPassword: EditText
    private lateinit var btnSignup: Button
    private lateinit var btnGoToLogin: Button

    private val viewModel: FitBuddyViewModel by viewModels()

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
                signupWithRoomDB(username, password)
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }

        btnGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    //TODO ROOMDB
    private fun signupWithRoomDB(username: String, password: String) {
        lifecycleScope.launch {
            val checkUser = viewModel.getUserByUsername(username)
            if (checkUser != null) {
                Log.d(tag, "Username already taken: $username")
                Toast.makeText(this@SignupActivity, "Username already taken", Toast.LENGTH_SHORT).show()
            } else {
                signupNewUserInRoomDB(username, password)
            }
        }
    }

    private fun signupNewUserInRoomDB(username: String, password: String) {
        lifecycleScope.launch {
            val newUser = User(username, password)
            viewModel.insertUser(newUser)
            Log.d(tag, "Registration Successful in RoomDB")
            Toast.makeText(this@SignupActivity, "Registration Successful in RoomDB", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
            finish()
        }
    }
}
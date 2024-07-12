package com.application.fitbuddy.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.application.fitbuddy.R
import com.application.fitbuddy.models.Follower
import com.application.fitbuddy.utils.KEY_USERNAME
import com.application.fitbuddy.utils.SHARED_PREFS_NAME
import com.application.fitbuddy.viewmodel.FollowerViewModel
import com.application.fitbuddy.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ProfileActivity : MenuActivity() {

    private val tag = "ProfileActivity"

    private val userViewModel: UserViewModel by viewModels()
    private val followerViewModel: FollowerViewModel by viewModels()

    private lateinit var profileImageView: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var followButton: Button

    private lateinit var loggedUsername: String
    private lateinit var profileUsername: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)
        setSupportActionBar(findViewById(R.id.toolbar))

        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        loggedUsername = sharedPreferences.getString(KEY_USERNAME, "") ?: ""

        profileUsername = intent.getStringExtra("username") ?: loggedUsername

        profileImageView = findViewById(R.id.profile_image)
        usernameTextView = findViewById(R.id.username_text_view)
        followButton = findViewById(R.id.follow_button)

        findViewById<ImageView>(R.id.spot_icon).setOnClickListener {
            navigateToSpotActivity()
        }

        findViewById<ImageView>(R.id.chart_icon).setOnClickListener {
            navigateToChartActivity()
        }

        loadProfileData()

        if (profileUsername == loggedUsername) {
            followButton.visibility = Button.GONE
        } else {
            checkIfFollowing()
        }

        followButton.setOnClickListener {
            followUser()
        }
    }

    private fun loadProfileData() {
        userViewModel.getUserByUsername(profileUsername,
            onSuccess = { user ->
                lifecycleScope.launch(Dispatchers.Main) {
                    usernameTextView.text = user?.username
                }
            },
            onFailure = { error ->
                showError(error)
            }
        )
    }

    private fun checkIfFollowing() {
        followButton.visibility = Button.GONE

        followerViewModel.getFollowingForUser(loggedUsername,
            onSuccess = { followingList ->
                lifecycleScope.launch(Dispatchers.Main) {
                    if (followingList.contains(profileUsername)) {
                        followButton.isEnabled = false
                        followButton.text = getString(R.string.following)
                    } else {
                        followButton.isEnabled = true
                        followButton.text = getString(R.string.follow)
                    }
                    followButton.visibility = Button.VISIBLE
                }
            },
            onFailure = { error ->
                showError(error)
                followButton.visibility = Button.VISIBLE
            }
        )
    }

    private fun followUser() {
        lifecycleScope.launch {
            val follower = Follower(
                userFK = profileUsername,
                followerFK = loggedUsername,
                followedDate = System.currentTimeMillis()
            )
            followerViewModel.insert(follower,
                onSuccess = {
                    lifecycleScope.launch(Dispatchers.Main) {
                        followButton.isEnabled = false
                        followButton.text = getString(R.string.following)
                    }
                },
                onFailure = { error ->
                    showError(error)
                }
            )
        }
    }

    private fun navigateToSpotActivity() {
        val intent = Intent(this, SpotActivity::class.java)
        intent.putExtra("username", profileUsername)
        startActivity(intent)
    }

    private fun navigateToChartActivity() {
        val intent = Intent(this, ChartActivity::class.java)
        intent.putExtra("username", profileUsername)
        startActivity(intent)
    }

    private fun showError(errorMessage: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Log.e(tag, errorMessage)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }
}

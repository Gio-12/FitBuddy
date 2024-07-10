package com.application.fitbuddy.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.application.fitbuddy.R
import com.application.fitbuddy.models.Follower
import com.application.fitbuddy.repository.FitBuddyRepository
import com.application.fitbuddy.utils.KEY_USERNAME
import com.application.fitbuddy.utils.SHARED_PREFS_NAME
import com.application.fitbuddy.viewmodel.FitBuddyViewModel
import com.application.fitbuddy.viewmodel.FitBuddyViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileActivity : MenuActivity() {

    private val tag = "ProfileActivity"

    @Inject
    lateinit var repository: FitBuddyRepository
    private lateinit var viewModel: FitBuddyViewModel

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

        val factory = FitBuddyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[FitBuddyViewModel::class.java]

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
        lifecycleScope.launch {
            val user = viewModel.getUserByUsername(profileUsername)
            usernameTextView.text = user?.username
            // Set profile picture if available, otherwise, it will show the placeholder
        }
    }

    private fun checkIfFollowing() {
        lifecycleScope.launch {
            val followingList = viewModel.getFollowingForUser(loggedUsername)
            if (followingList.contains(profileUsername)) {
                followButton.isEnabled = false
                followButton.text = getString(R.string.following)
            } else {
                followButton.isEnabled = true
                followButton.text = getString(R.string.follow)
            }
        }
    }

    private fun followUser() {
        lifecycleScope.launch {
            val follower = Follower(userFK = profileUsername, followerFK = loggedUsername, followedDate = System.currentTimeMillis())
            viewModel.insertFollower(follower)
            followButton.isEnabled = false
            followButton.text = getString(R.string.following)
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }
}

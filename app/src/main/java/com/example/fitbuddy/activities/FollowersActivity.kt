package com.example.fitbuddy.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbuddy.R
import com.example.fitbuddy.adapters.FollowersAdapter
import com.example.fitbuddy.adapters.FollowingAdapter
import com.example.fitbuddy.models.Follower
import com.example.fitbuddy.repository.FitBuddyRepository
import com.example.fitbuddy.utils.KEY_USERNAME
import com.example.fitbuddy.utils.SHARED_PREFS_NAME
import com.example.fitbuddy.viewmodel.FitBuddyViewModel
import com.example.fitbuddy.viewmodel.FitBuddyViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FollowersActivity : MenuActivity() {

    private val tag = "FolowersActivity"

    @Inject
    lateinit var repository: FitBuddyRepository
    private lateinit var viewModel: FitBuddyViewModel

    private lateinit var followersRecyclerView: RecyclerView
    private lateinit var followersAdapter: FollowersAdapter
    private lateinit var followingAdapter: FollowingAdapter

    private lateinit var username: String
    private var showingFollowers = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.followers_activity)
        setSupportActionBar(findViewById(R.id.toolbar))

        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val defaultUsername = sharedPreferences.getString(KEY_USERNAME, "") ?: ""
        username = intent.getStringExtra("username") ?: defaultUsername

        val factory = FitBuddyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[FitBuddyViewModel::class.java]

        followersRecyclerView = findViewById(R.id.recycler_view)
        followersRecyclerView.layoutManager = LinearLayoutManager(this)

        followersAdapter = FollowersAdapter(
            onProfileClick = { followerUsername ->
                navigateToProfile(followerUsername)
            },
            onFollowClick = { followerUsername ->
                addFollowing(followerUsername)
            }
        )

        followingAdapter = FollowingAdapter(
            onProfileClick = { followingUsername ->
                navigateToProfile(followingUsername)
            },
            onUnfollowClick = { followingUsername ->
                removeFollowing(followingUsername)
            }
        )

        findViewById<SearchView>(R.id.search_view).setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { search(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { search(it) }
                return true
            }
        })

        findViewById<LinearLayout>(R.id.followers_container).setOnClickListener {
            showingFollowers = true
            loadFollowers(username)
        }

        findViewById<LinearLayout>(R.id.following_container).setOnClickListener {
            showingFollowers = false
            loadFollowing(username)
        }

        loadUserDetails(username)
        loadFollowers(username)
    }

    private fun loadUserDetails(username: String) {
        lifecycleScope.launch {
            val user = viewModel.getUserByUsername(username)
            findViewById<TextView>(R.id.username_text_view).text = user?.username
            findViewById<TextView>(R.id.followers_count).text = viewModel.getFollowersForUser(username).size.toString()
            findViewById<TextView>(R.id.following_count).text = viewModel.getFollowingForUser(username).size.toString()
        }
    }

    private fun loadFollowers(username: String) {
        lifecycleScope.launch {
            val followers = viewModel.getFollowersForUser(username)
            followersAdapter.submitList(followers)
            followersRecyclerView.adapter = followersAdapter
        }
    }

    private fun loadFollowing(username: String) {
        lifecycleScope.launch {
            val following = viewModel.getFollowingForUser(username)
            followingAdapter.submitList(following)
            followersRecyclerView.adapter = followingAdapter
        }
    }

    private fun search(query: String) {
        lifecycleScope.launch {
            if (showingFollowers) {
                val followers = viewModel.getFollowersForUser(username)
                followersAdapter.submitList(followers)
            } else {
                val filteredFollowing = viewModel.getFollowingForUser(username)
                followingAdapter.submitList(filteredFollowing)
            }
        }
    }

    private fun navigateToProfile(username: String) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
    }

    private fun addFollowing(followerUsername: String) {
        lifecycleScope.launch {
            val follower = Follower(
                userFK = followerUsername,
                followerFK = username,
                followedDate = System.currentTimeMillis()
            )
            viewModel.insertFollower(follower)
            loadFollowing(this@FollowersActivity.username)
        }
    }

    private fun removeFollowing(followingUsername: String) {
        lifecycleScope.launch {
            viewModel.removeFollowing(followingUsername, username)
            loadFollowing(this@FollowersActivity.username)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }
}

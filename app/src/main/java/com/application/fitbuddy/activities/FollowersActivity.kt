package com.application.fitbuddy.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.application.fitbuddy.R
import com.application.fitbuddy.adapters.FollowersAdapter
import com.application.fitbuddy.adapters.FollowingAdapter
import com.application.fitbuddy.models.Follower
import com.application.fitbuddy.utils.KEY_USERNAME
import com.application.fitbuddy.utils.SHARED_PREFS_NAME
import com.application.fitbuddy.viewmodel.FollowerViewModel
import com.application.fitbuddy.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FollowersActivity : MenuActivity() {

    private val tag = "FollowersActivity"

    private val followerViewModel: FollowerViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

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

        followersRecyclerView = findViewById(R.id.recycler_view)
        followersRecyclerView.layoutManager = LinearLayoutManager(this)

        followersAdapter = FollowersAdapter(
            onProfileClick = { followerUsername ->
                navigateToProfile(followerUsername)
            },
            onFollowClick = { followerUsername ->
                addFollowing(followerUsername)
            },
            onUnfollowClick = { followerUsername ->
                removeFollowing(followerUsername)
            },
            isFollowingUser = { followerUsername, onSuccess, onFailure ->
                followerViewModel.isFollowing(followerUsername, username, onSuccess, onFailure)
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
    }

    private fun loadUserDetails(username: String) {
        userViewModel.getUserByUsername(
            username,
            onSuccess = { user ->
                findViewById<TextView>(R.id.username_text_view).text = user?.username

                followerViewModel.getFollowersForUser(
                    username,
                    onSuccess = { followers ->
                        findViewById<TextView>(R.id.followers_count).text = followers.size.toString()
                    },
                    onFailure = { errorMessage ->
                        showError(errorMessage)
                    }
                )

                followerViewModel.getFollowingForUser(
                    username,
                    onSuccess = { following ->
                        findViewById<TextView>(R.id.following_count).text = following.size.toString()
                    },
                    onFailure = { errorMessage ->
                        showError(errorMessage)
                    }
                )
            },
            onFailure = { errorMessage ->
                showError(errorMessage)
            }
        )
    }

    private fun loadFollowers(username: String) {
        followerViewModel.getFollowersForUser(
            username,
            onSuccess = { followers ->
                followersAdapter.submitList(followers)
                followersRecyclerView.adapter = followersAdapter
            },
            onFailure = { errorMessage ->
                showError(errorMessage)
            }
        )
    }

    private fun loadFollowing(username: String) {
        followerViewModel.getFollowingForUser(
            username,
            onSuccess = { following ->
                followingAdapter.submitList(following)
                followersRecyclerView.adapter = followingAdapter
            },
            onFailure = { errorMessage ->
                showError(errorMessage)
            }
        )
    }

    private fun search(query: String) {
        if (showingFollowers) {
            followerViewModel.getFollowersForUser(
                username,
                onSuccess = { followers ->
                    followersAdapter.submitList(followers.filter { it.contains(query, true) })
                },
                onFailure = { errorMessage ->
                    showError(errorMessage)
                }
            )
        } else {
            followerViewModel.getFollowingForUser(
                username,
                onSuccess = { following ->
                    followingAdapter.submitList(following.filter { it.contains(query, true) })
                },
                onFailure = { errorMessage ->
                    showError(errorMessage)
                }
            )
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
            followerViewModel.insert(
                follower,
                onSuccess = {
                    loadFollowing(username)
                    updateFollowerState(followerUsername, true)
                },
                onFailure = { errorMessage ->
                    showError(errorMessage)
                }
            )
        }
    }

    private fun updateFollowerState(followerUsername: String, isFollowed: Boolean) {
        val followersList = followersAdapter.currentList.toMutableList()
        val index = followersList.indexOf(followerUsername)
        if (index != -1) {
            followersList[index] = if (isFollowed) "Following" else "NotFollowing"
            followersAdapter.submitList(followersList)
        }
    }

    private fun removeFollowing(followingUsername: String) {
        followerViewModel.removeFollowing(
            followingUsername,
            username,
            onSuccess = {
                loadFollowing(username)
                updateFollowerState(followingUsername, false)
            },
            onFailure = { errorMessage ->
                showError(errorMessage)
            }
        )
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
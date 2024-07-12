package com.application.fitbuddy.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.application.fitbuddy.R

class FollowersAdapter(
    private val onProfileClick: (String) -> Unit,
    private val onFollowClick: (String) -> Unit,
    private val onUnfollowClick: (String) -> Unit,
    private val isFollowingUser: (String, (Boolean) -> Unit, (String) -> Unit) -> Unit
) : ListAdapter<String, FollowersAdapter.FollowerViewHolder>(FollowerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_follower, parent, false)
        return FollowerViewHolder(view, onProfileClick, onFollowClick, onUnfollowClick, isFollowingUser)
    }

    override fun onBindViewHolder(holder: FollowerViewHolder, position: Int) {
        val followerUsername = getItem(position)
        holder.bind(followerUsername)
    }

    class FollowerViewHolder(
        itemView: View,
        private val onProfileClick: (String) -> Unit,
        private val onFollowClick: (String) -> Unit,
        private val onUnfollowClick: (String) -> Unit,
        private val isFollowingUser: (String, (Boolean) -> Unit, (String) -> Unit) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.username_text_view)
        private val profileButton: Button = itemView.findViewById(R.id.profile_button)
        private val followButton: Button = itemView.findViewById(R.id.follow_button)
        private val unfollowButton: Button = itemView.findViewById(R.id.unfollow_button)
        private var currentFollowerUsername: String? = null

        init {
            profileButton.setOnClickListener {
                currentFollowerUsername?.let { followerUsername ->
                    onProfileClick(followerUsername)
                }
            }
            followButton.setOnClickListener {
                currentFollowerUsername?.let { followerUsername ->
                    onFollowClick(followerUsername)
                }
            }
            unfollowButton.setOnClickListener {
                currentFollowerUsername?.let { followerUsername ->
                    onUnfollowClick(followerUsername)
                }
            }
        }

        fun bind(followerUsername: String) {
            currentFollowerUsername = followerUsername
            usernameTextView.text = followerUsername
            isFollowingUser(followerUsername, { isFollowing ->
                if (isFollowing) {
                    followButton.visibility = View.GONE
                    unfollowButton.visibility = View.VISIBLE
                } else {
                    followButton.visibility = View.VISIBLE
                    unfollowButton.visibility = View.GONE
                }
            }, { errorMessage ->
                followButton.visibility = View.VISIBLE
                unfollowButton.visibility = View.GONE
                Log.e("FollowersAdapterError", errorMessage)
            })
        }
    }

    class FollowerDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}

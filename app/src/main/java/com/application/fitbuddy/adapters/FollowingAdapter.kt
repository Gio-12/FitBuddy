package com.application.fitbuddy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.application.fitbuddy.R

class FollowingAdapter(
    private val onProfileClick: (String) -> Unit,
    private val onUnfollowClick: (String) -> Unit
) :
    ListAdapter<String, FollowingAdapter.FollowingViewHolder>(FollowingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_following, parent, false)
        return FollowingViewHolder(view, onProfileClick, onUnfollowClick)
    }

    override fun onBindViewHolder(holder: FollowingViewHolder, position: Int) {
        val followingUsername = getItem(position)
        holder.bind(followingUsername)
    }

    class FollowingViewHolder(
        itemView: View,
        private val onProfileClick: (String) -> Unit,
        private val onUnfollowClick: (String) -> Unit
    ) :
        RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.username_text_view)
        private val profileButton: Button = itemView.findViewById(R.id.profile_button)
        private val unfollowButton: Button = itemView.findViewById(R.id.unfollow_button)
        private var currentFollowingUsername: String? = null

        init {
            profileButton.setOnClickListener {
                currentFollowingUsername?.let { followingUsername ->
                    onProfileClick(followingUsername)
                }
            }
            unfollowButton.setOnClickListener {
                currentFollowingUsername?.let { followingUsername ->
                    onUnfollowClick(followingUsername)
                }
            }
        }

        fun bind(followingUsername: String) {
            currentFollowingUsername = followingUsername
            usernameTextView.text = followingUsername
        }
    }

    class FollowingDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}

package com.example.fitbuddy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbuddy.R

class FollowersAdapter(private val onClick: (String) -> Unit) :
    ListAdapter<String, FollowersAdapter.FollowerViewHolder>(FollowerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_follower, parent, false)
        return FollowerViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: FollowerViewHolder, position: Int) {
        val followerUsername = getItem(position)
        holder.bind(followerUsername)
    }

    class FollowerViewHolder(itemView: View, val onClick: (String) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.username_text_view)
        private var currentFollowerUsername: String? = null

        init {
            itemView.setOnClickListener {
                currentFollowerUsername?.let { onClick(it) }
            }
        }

        fun bind(followerUsername: String) {
            currentFollowerUsername = followerUsername
            usernameTextView.text = followerUsername
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


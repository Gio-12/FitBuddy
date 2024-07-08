package com.example.fitbuddy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbuddy.R

class FollowingAdapter(private val onClick: (String) -> Unit) :
    ListAdapter<String, FollowingAdapter.FollowingViewHolder>(FollowingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_follower, parent, false)
        return FollowingViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: FollowingViewHolder, position: Int) {
        val followingUsername = getItem(position)
        holder.bind(followingUsername)
    }

    class FollowingViewHolder(itemView: View, val onClick: (String) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.username_text_view)
        private var currentFollowingUsername: String? = null

        init {
            itemView.setOnClickListener {
                currentFollowingUsername?.let { onClick(it) }
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

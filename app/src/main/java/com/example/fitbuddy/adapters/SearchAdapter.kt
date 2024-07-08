package com.example.fitbuddy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbuddy.R

class SearchAdapter(private val onClick: (String) -> Unit) :
    ListAdapter<String, SearchAdapter.SearchViewHolder>(SearchDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search, parent, false)
        return SearchViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val username = getItem(position)
        holder.bind(username)
    }

    class SearchViewHolder(itemView: View, val onClick: (String) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.username_text_view)
        private var currentUsername: String? = null

        init {
            itemView.setOnClickListener {
                currentUsername?.let { onClick(it) }
            }
        }

        fun bind(username: String) {
            currentUsername = username
            usernameTextView.text = username
        }
    }

    class SearchDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}

package com.application.fitbuddy.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.application.fitbuddy.R
import com.application.fitbuddy.adapters.SearchAdapter
import com.application.fitbuddy.repository.FitBuddyRepository
import com.application.fitbuddy.viewmodel.FitBuddyViewModel
import com.application.fitbuddy.viewmodel.FitBuddyViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchActivity : MenuActivity() {

    private val tag = "SearchActivity"

    @Inject
    lateinit var repository: FitBuddyRepository
    private lateinit var viewModel: FitBuddyViewModel

    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var searchAdapter: SearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_activity)
        setSupportActionBar(findViewById(R.id.toolbar))

        val factory = FitBuddyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[FitBuddyViewModel::class.java]

        // Initialize RecyclerView
        searchRecyclerView = findViewById(R.id.recycler_view)
        searchRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize SearchView
        val searchView = findViewById<SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchUsers(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchUsers(it) }
                return true
            }
        })

        // Initialize SearchAdapter for RecyclerView
        searchAdapter = SearchAdapter { username ->
            navigateToProfile(username)
        }
        searchRecyclerView.adapter = searchAdapter
    }

    private fun searchUsers(query: String) {
        lifecycleScope.launch {
            val users = viewModel.searchUsers(query)
            searchAdapter.submitList(users)
        }
    }

    private fun navigateToProfile(username: String) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }
}

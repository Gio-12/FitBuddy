package com.example.fitbuddy.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.SearchView // Import correct SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.fitbuddy.R
import com.example.fitbuddy.models.Action
import com.example.fitbuddy.repository.FitBuddyRepository
import com.example.fitbuddy.viewmodel.FitBuddyViewModel
import com.example.fitbuddy.viewmodel.FitBuddyViewModelFactory
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ChartActivity : MenuActivity() {

    @Inject
    lateinit var repository: FitBuddyRepository
    private lateinit var viewModel: FitBuddyViewModel

    private lateinit var timePeriodSpinner: Spinner
    private lateinit var activityPieChart: PieChart
    private lateinit var stepsLineChart: LineChart
    private lateinit var totalStepsTextView: TextView
    private lateinit var totalSpotsTextView: TextView

    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chart_activity)
        setSupportActionBar(findViewById(R.id.toolbar))

        username = intent.getStringExtra("username") ?: ""

        val factory = FitBuddyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[FitBuddyViewModel::class.java]

        timePeriodSpinner = findViewById(R.id.time_period_spinner)
        activityPieChart = findViewById(R.id.activity_pie_chart)
        stepsLineChart = findViewById(R.id.steps_line_chart)
        totalStepsTextView = findViewById(R.id.total_steps_text_view)
        totalSpotsTextView = findViewById(R.id.total_spots_text_view)

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.time_periods,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timePeriodSpinner.adapter = adapter

        timePeriodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedPeriod = parent.getItemAtPosition(position).toString()
                updateCharts(selectedPeriod)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Initially load data for the first time period (e.g., Day)
        updateCharts(timePeriodSpinner.selectedItem.toString())
    }

    private fun updateCharts(period: String) {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        val startTime = when (period) {
            "Day" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                calendar.timeInMillis
            }
            "Week" -> {
                calendar.add(Calendar.WEEK_OF_YEAR, -1)
                calendar.timeInMillis
            }
            "Month" -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.timeInMillis
            }
            else -> endTime
        }

        lifecycleScope.launch {
            val actions = viewModel.getActionsForPeriod(username, startTime, endTime)
            updateActivityPieChart(actions)
            updateStepsLineChart(actions)
            updateTotals(actions)
        }
    }

    private fun updateActivityPieChart(actions: List<Action>) {
        // Group actions by activity type and prepare PieChart entries
        val activityMap = actions.groupBy { it.actionType }
        val entries = activityMap.map { (type, actions) ->
            PieEntry(actions.size.toFloat(), type)
        }

        val dataSet = PieDataSet(entries, "Activity Distribution")
        val data = PieData(dataSet)
        activityPieChart.data = data
        activityPieChart.invalidate() // Refresh the chart
    }

    private fun updateStepsLineChart(actions: List<Action>) {
        // Group actions by day and prepare LineChart entries
        val stepsMap = actions.groupBy { action ->
            val calendar = Calendar.getInstance().apply { timeInMillis = action.startTime }
            calendar.get(Calendar.DAY_OF_YEAR)
        }.mapValues { entry ->
            entry.value.sumBy { it.steps }
        }

        val entries = stepsMap.map { (day, steps) ->
            Entry(day.toFloat(), steps.toFloat())
        }

        val dataSet = LineDataSet(entries, "Daily Steps")
        val data = LineData(dataSet)
        stepsLineChart.data = data
        stepsLineChart.invalidate() // Refresh the chart
    }

    private fun updateTotals(actions: List<Action>) {
        val totalSteps = actions.sumBy { it.steps }
        val totalSpots = actions.size
        totalStepsTextView.text = getString(R.string.total_steps, totalSteps)
        totalSpotsTextView.text = getString(R.string.total_spots, totalSpots)
    }
}

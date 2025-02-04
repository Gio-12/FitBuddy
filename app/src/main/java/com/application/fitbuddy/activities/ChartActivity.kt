package com.application.fitbuddy.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.application.fitbuddy.R
import com.application.fitbuddy.models.Action
import com.application.fitbuddy.utils.KEY_USERNAME
import com.application.fitbuddy.utils.SHARED_PREFS_NAME
import com.application.fitbuddy.viewmodel.ActionViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.components.XAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class ChartActivity : MenuActivity() {

    private val tag = "ChartActivity"

    private val actionViewModel: ActionViewModel by viewModels()

    private lateinit var timePeriodSpinner: Spinner
    private lateinit var activityPieChart: PieChart
    private lateinit var stepsLineChart: LineChart
    private lateinit var totalStepsTextView: TextView
    private lateinit var totalActionsTextView: TextView

    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chart_activity)
        setSupportActionBar(findViewById(R.id.toolbar))

        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val defaultUsername = sharedPreferences.getString(KEY_USERNAME, "") ?: ""

        username = intent.getStringExtra("username") ?: defaultUsername

        timePeriodSpinner = findViewById(R.id.time_period_spinner)
        activityPieChart = findViewById(R.id.activity_pie_chart)
        stepsLineChart = findViewById(R.id.steps_line_chart)
        totalStepsTextView = findViewById(R.id.total_steps_text_view)
        totalActionsTextView = findViewById(R.id.total_actions_text_view)

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
            actionViewModel.getActionsForPeriod(username, startTime, endTime, { actions ->
                updateActivityPieChart(actions)
                updateStepsLineChart(actions)
                updateTotals(actions)
            }, { error ->
                // Handle error
            })
        }
    }

    private fun updateActivityPieChart(actions: List<Action>) {
        val activityMap = actions.groupBy { it.actionType }
        val entries = activityMap.map { (type, actions) ->
            PieEntry(actions.size.toFloat(), type)
        }

        val dataSet = PieDataSet(entries, "Activity Distribution")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.asList()
        dataSet.valueTextSize = 14f
        dataSet.valueFormatter = PercentFormatter(activityPieChart)

        val data = PieData(dataSet)
        activityPieChart.data = data
        activityPieChart.setEntryLabelTextSize(14f)
        activityPieChart.invalidate()
    }

    private fun updateStepsLineChart(actions: List<Action>) {
        val stepsMap = actions.groupBy { action ->
            val calendar = Calendar.getInstance().apply { timeInMillis = action.startTime }
            calendar.get(Calendar.DAY_OF_YEAR)
        }.mapValues { entry ->
            entry.value.sumBy { it.steps.coerceAtLeast(0) }
        }

        val entries = stepsMap.map { (day, steps) ->
            Entry(day.toFloat(), steps.toFloat())
        }.sortedBy { it.x }

        val dataSet = LineDataSet(entries, "Daily Steps")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.asList()
        dataSet.valueTextSize = 14f

        val lineData = LineData(dataSet)
        stepsLineChart.data = lineData
        stepsLineChart.invalidate()

        val xAxis = stepsLineChart.xAxis
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(entries.map { it.x.toInt().toString() })
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        val yAxisLeft = stepsLineChart.axisLeft
        yAxisLeft.axisMinimum = 0f

        val yAxisRight = stepsLineChart.axisRight
        yAxisRight.isEnabled = false
    }

    private fun updateTotals(actions: List<Action>) {
        val totalSteps = actions.sumBy { it.steps.coerceAtLeast(0) }
        val totalActions = actions.size
        totalStepsTextView.text = getString(R.string.total_steps, totalSteps)
        totalActionsTextView.text = getString(R.string.total_actions, totalActions)
    }
}

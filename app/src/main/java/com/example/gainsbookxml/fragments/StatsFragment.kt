package com.example.gainsbookxml.fragments

import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.gainsbookxml.R
import com.example.gainsbookxml.databinding.FragmentStatsBinding
import com.example.gainsbookxml.utils.*
import com.example.gainsbookxml.viewmodels.*
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * This fragment is used to display statistics of a variable based on selected type, month and year
 */
class StatsFragment : Fragment() {
    private lateinit var binding: FragmentStatsBinding

    // Used to handle changing the month and year
    private val supportViewModel: SupportViewModel by viewModels {
        SupportViewModelFactory(requireContext())
    }

    // used to add/delete and get workouts
    private val statsViewModel: StatsViewModel by viewModels {
        StatsViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatsBinding.inflate(layoutInflater)

        initUI()
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun initUI() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)

        // View model gets all saved years from database and stores them in a StateFlow variable
        supportViewModel.getYears()

        // Initializes month and year to be current month and year
        lifecycleScope.launch(Dispatchers.IO) {
            supportViewModel.setCurrentYear(currentYear)
            supportViewModel.setCurrentMonth(currentMonth + 1)
        }

        // Click listener for "+ add new year" -button
        // Displays a popup with an edit text in which user inputs a new year
        binding.AddNewYearButton.setOnClickListener {
            newYearPopup(
                supportViewModel = supportViewModel,
                context = requireContext()
            )
        }

        // Click listener for "+ new lift" -button
        // Displays a popup with an edit text in which user inputs a new lift
        binding.AddNewVariableButton.setOnClickListener {
            newVariablePopup(
                statsViewModel = statsViewModel,
                context = requireContext()
            )
        }

        lifecycleScope.launch {
            binding.graphView.gridLabelRenderer.gridStyle = GridLabelRenderer.GridStyle.BOTH
            binding.graphView.viewport.setMaxX(31.0)
            binding.graphView.gridLabelRenderer.setHumanRounding(true)
            binding.graphView.viewport.isXAxisBoundsManual = true
            binding.graphView.viewport.isYAxisBoundsManual = true
            binding.graphView.gridLabelRenderer.gridColor = ContextCompat.getColor(
                requireContext(),
                R.color.secondary
            )
            statsViewModel.statistics.collect {
                // Map the statistics, create a datapoint with x value of day and y value of value and then convert it to array.
                val list: Array<DataPoint> =
                    it.map { statistic -> DataPoint(statistic.day.toDouble(), statistic.value) }
                        .toTypedArray()
                val series = LineGraphSeries(list)
                val linePaint = Paint()
                linePaint.color = ContextCompat.getColor(
                    requireContext(),
                    R.color.secondary
                )
                linePaint.strokeWidth = 6F
                series.setCustomPaint(linePaint)

                val size = it.size
                binding.graphView.removeAllSeries()
                binding.graphView.gridLabelRenderer.numHorizontalLabels = size
                binding.graphView.viewport.setMaxY(series.highestValueY)
                binding.graphView.viewport.setMinY(series.lowestValueY)
                binding.graphView.addSeries(series)
            }
        }

        // For selecting month
        monthSpinner(
            spinner = binding.monthSpinner,
            supportViewModel = supportViewModel,
            mainViewModel = statsViewModel,
            context = requireContext(),
            lifecycleScope = lifecycleScope,
        )

        // For selecting year
        yearSpinner(
            spinner = binding.yearSpinner,
            supportViewModel = supportViewModel,
            mainViewModel = statsViewModel,
            context = requireContext(),
            lifecycleScope = lifecycleScope,
        )

        // For selecting variable
        variableSpinner(
            spinner = binding.variableSpinner,
            supportViewModel = supportViewModel,
            mainViewModel = statsViewModel,
            context = requireContext(),
            lifecycleScope = lifecycleScope,
        )

        // For selecting type
        typeSpinner(
            spinner = binding.typeSpinner,
            supportViewModel = supportViewModel,
            mainViewModel = statsViewModel,
            context = requireContext(),
            lifecycleScope = lifecycleScope,
        )

        // Click listener for floating action button
        binding.fab.setOnClickListener {
            // navigate to NewStatisticFragment
            val direction = StatsFragmentDirections.actionStatsFragmentToNewStatisticFragment()
            findNavController().navigate(direction)
        }
    }
}
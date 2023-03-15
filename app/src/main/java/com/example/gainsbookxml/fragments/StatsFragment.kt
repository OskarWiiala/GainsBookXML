package com.example.gainsbookxml.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.gainsbookxml.R
import com.example.gainsbookxml.databinding.FragmentStatsBinding
import com.example.gainsbookxml.utils.*
import com.example.gainsbookxml.viewmodels.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class StatsFragment : Fragment() {
    val TAG = "StatsFragment"
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
        Log.d(TAG, "Just before binding init")
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
            statsViewModel.statistics.collect {
                Log.d(TAG, "Collecting statistics")
                // Do graph updates here
            }
        }

        monthSpinner(
            spinner = binding.monthSpinner,
            supportViewModel = supportViewModel,
            mainViewModel = statsViewModel,
            context = requireContext(),
            lifecycleScope = lifecycleScope,
        )

        yearSpinner(
            spinner = binding.yearSpinner,
            supportViewModel = supportViewModel,
            mainViewModel = statsViewModel,
            context = requireContext(),
            lifecycleScope = lifecycleScope,
        )

        variableSpinner(
            spinner = binding.variableSpinner,
            supportViewModel = supportViewModel,
            mainViewModel = statsViewModel,
            context = requireContext(),
            lifecycleScope = lifecycleScope,
        )

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
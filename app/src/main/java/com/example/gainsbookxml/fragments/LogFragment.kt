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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gainsbookxml.R
import com.example.gainsbookxml.WorkoutListAdapter
import com.example.gainsbookxml.databinding.FragmentLogBinding
import com.example.gainsbookxml.utils.WorkoutClickListener
import com.example.gainsbookxml.utils.deletePopup
import com.example.gainsbookxml.utils.newYearPopup
import com.example.gainsbookxml.viewmodels.LogViewModel
import com.example.gainsbookxml.viewmodels.LogViewModelFactory
import com.example.gainsbookxml.viewmodels.SupportViewModel
import com.example.gainsbookxml.viewmodels.SupportViewModelFactory
import kotlinx.coroutines.launch
import java.util.*

/**
 * The main screen of the app. It displays a list of workouts by month and year.
 * A new year can also be added to the database here.
 */
class LogFragment : Fragment(), WorkoutClickListener {
    private lateinit var binding: FragmentLogBinding

    // Used to handle changing the month and year
    private val supportViewModel: SupportViewModel by viewModels {
        SupportViewModelFactory(requireContext())
    }

    // used to add/delete and get workouts
    private val logViewModel: LogViewModel by viewModels {
        LogViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLogBinding.inflate(layoutInflater)

        initUI()

        // Inflate the layout for this fragment
        return binding.root
    }

    // Initializes the UI for the fragment
    private fun initUI() {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        // get workouts by this year and month
        logViewModel.getWorkoutsByYearMonth(year = currentYear, month = currentMonth + 1)

        // Click listener for the + fab
        binding.fab.setOnClickListener {
            val direction = LogFragmentDirections.actionLogFragmentToNewWorkoutFragment()
            findNavController().navigate(direction)
        }

        // Click listener for + add new year -button
        // Displays a popup with an edit text in which user inputs a new year
        binding.AddNewYearButton.setOnClickListener {
            newYearPopup(
                supportViewModel = supportViewModel,
                context = requireContext()
            )
        }

        // list of months for monthSpinner
        val months = listOf(
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"
        )

        var selectedMonth = currentMonth + 1
        var selectedYear = currentYear

        // View model gets all saved years from database and stores them in a StateFlow variable
        supportViewModel.getYears()

        val monthSpinnerAdapter = ArrayAdapter(
            requireContext().applicationContext,
            R.layout.custom_spinner_item,
            months
        )

        binding.monthSpinner.adapter = monthSpinnerAdapter
        binding.monthSpinner.setSelection(currentMonth)

        binding.monthSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    // When selecting a month in the spinner list
                    selectedMonth = p2 + 1
                    // Updates the workouts into the view model based on selection of month and year
                    logViewModel.getWorkoutsByYearMonth(year = selectedYear, month = selectedMonth)

                }

                // Unused, here to prevent member implementation error
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }

        lifecycleScope.launch {
            // has to collect years to reconstruct an new list of years everytime a new year is added
            supportViewModel.years.collect {
                Log.d("collecting", "collecting years")
                // Inserts current year to database is view model does not have any years
                // Inserting an already existing year just replaces it so no worries if the view model updates a little late
                if (supportViewModel.years.value.isEmpty()) {
                    supportViewModel.insertYear(currentYear)
                }

                // Converts years from type Year() to type Int
                val yearsConverted = mutableListOf<Int>()
                supportViewModel.years.value.forEach { yearsConverted.add(it.year) }

                val yearSpinnerAdapter = ArrayAdapter(
                    requireContext().applicationContext,
                    R.layout.custom_spinner_item,
                    yearsConverted
                )

                binding.yearSpinner.adapter = yearSpinnerAdapter

                if (yearsConverted.contains(currentYear)) {
                    binding.yearSpinner.setSelection(yearsConverted.indexOf(currentYear))
                } else binding.yearSpinner.setSelection(0)

                binding.yearSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            p2: Int,
                            p3: Long
                        ) {
                            // Gets the correct year from view model based on index p2
                            selectedYear = supportViewModel.years.value[p2].year
                            // Updates the workouts into the view model based on selection of month and year
                            logViewModel.getWorkoutsByYearMonth(
                                year = selectedYear,
                                month = selectedMonth
                            )
                        }

                        // Unused, here to prevent member implementation error
                        override fun onNothingSelected(p0: AdapterView<*>?) {
                        }
                    }
            }
        }

        // Applies the layout manager and adapter for the workout list recycler view
        // that lists all the workouts of this month and year
        binding.WorkoutList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter =
                WorkoutListAdapter(logViewModel = logViewModel, clickListener = this@LogFragment)

            // Updates the list whenever any changes are made to the list
            // of workouts in the view model
            lifecycleScope.launch {
                logViewModel.workouts.collect {
                    (adapter as WorkoutListAdapter).notifyDataSetChanged()
                }
            }
        }
    }

    // Is called whenever a workout card is clicked
    // Navigates to ViewWorkoutFragment
    override fun onViewClick(workoutId: Int) {
        // navigate to ViewWorkoutFragment with workout id
        val direction = LogFragmentDirections.actionLogFragmentToViewWorkoutFragment(workoutId)
        findNavController().navigate(direction)
    }

    // Is called whenever the edit-ImageButton is clicked on a workout card.
    // Navigates to EditWorkoutFragment
    override fun onEditClick(workoutId: Int) {
        // navigate to ViewWorkoutFragment with workout id
        val direction = LogFragmentDirections.actionLogFragmentToEditWorkoutFragment(workoutId)
        findNavController().navigate(direction)
    }

    // Is called whenever the delete-ImageButton is clicked on a workout card.
    // Displays a popup which is used to confirm deletion of a selected workout
    override fun onDeleteClick(workoutId: Int, year: Int, month: Int) {
        // Delete workout by id
        deletePopup(
            supportViewModel = supportViewModel,
            logViewModel = logViewModel,
            context = requireContext(),
            workoutId = workoutId,
            year = year,
            month = month,
            type = "workout"
        )
    }
}
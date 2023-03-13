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
import kotlinx.coroutines.selects.select
import java.util.*

class LogFragment : Fragment(), WorkoutClickListener {
    private lateinit var binding: FragmentLogBinding

    private val supportViewModel: SupportViewModel by viewModels {
        SupportViewModelFactory(requireContext())
    }
    private val logViewModel: LogViewModel by viewModels {
        LogViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLogBinding.inflate(layoutInflater)

        // Get workouts from database to view model
        initUI()

        // Just for testing purposes
        /*val listOfExercises = listOf(
            ExerciseWithIndex(
                description = "pull up",
                index = 0
            )
        )
        supportViewModel.addWorkout(exercises = listOfExercises, day = 9, month = 3, year = 2023)*/

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun initUI() {
        // get workouts by selection of year and month
        logViewModel.getWorkoutsByYearMonth(year = 2023, month = 3)

        binding.fab.setOnClickListener {
            val direction = LogFragmentDirections.actionLogFragmentToNewWorkoutFragment()
            findNavController().navigate(direction)
        }


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

        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        var selectedMonth = currentMonth + 1
        var selectedYear = currentYear

        // get years for yearSpinner
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
                    selectedMonth = p2 + 1
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


        binding.WorkoutList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter =
                WorkoutListAdapter(logViewModel = logViewModel, clickListener = this@LogFragment)
            lifecycleScope.launch {
                logViewModel.workouts.collect {
                    (adapter as WorkoutListAdapter).notifyDataSetChanged()
                }
            }
        }
    }

    override fun onViewClick(workoutId: Int) {
        // navigate to ViewWorkoutFragment with workout id
        val direction = LogFragmentDirections.actionLogFragmentToViewWorkoutFragment(workoutId)
        findNavController().navigate(direction)
    }

    override fun onEditClick(workoutId: Int) {
        // navigate to ViewWorkoutFragment with workout id
        val direction = LogFragmentDirections.actionLogFragmentToEditWorkoutFragment(workoutId)
        findNavController().navigate(direction)
    }

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
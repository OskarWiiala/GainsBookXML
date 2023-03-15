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
import com.example.gainsbookxml.utils.newVariablePopup
import com.example.gainsbookxml.utils.newYearPopup
import com.example.gainsbookxml.viewmodels.StatsViewModel
import com.example.gainsbookxml.viewmodels.StatsViewModelFactory

import com.example.gainsbookxml.viewmodels.SupportViewModel
import com.example.gainsbookxml.viewmodels.SupportViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

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

        lifecycleScope.launch(Dispatchers.IO) {
            statsViewModel.statistics.collect {
                // Do graph updates here
            }
        }

        initMonthSpinner()
        initYearSpinner(currentYear = currentYear)
        initVariableSpinner()
        initTypeSpinner()

        // Click listener for floating action button
        binding.fab.setOnClickListener {
            // navigate to NewStatisticFragment
            val direction = StatsFragmentDirections.actionStatsFragmentToNewStatisticFragment()
            findNavController().navigate(direction)
        }
    }

    private fun initMonthSpinner() {
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

        val monthSpinnerAdapter = ArrayAdapter(
            requireContext().applicationContext,
            R.layout.custom_spinner_item,
            months
        )

        val currentMonth = supportViewModel.currentMonth.value

        binding.monthSpinner.adapter = monthSpinnerAdapter
        binding.monthSpinner.setSelection(currentMonth)

        binding.monthSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    // When selecting a month in the spinner list
                    // get current variableId and type from view model
                    lifecycleScope.launch(Dispatchers.IO) {
                        supportViewModel.setCurrentMonth(month = p2 + 1)
                    }

                    val variableId = statsViewModel.variable.value.variableID
                    val type = statsViewModel.type.value
                    val selectedYear = supportViewModel.currentYear.value

                    // Update the new statistics to view model
                    statsViewModel.getStatisticsBySelection(
                        variableID = variableId,
                        type = type,
                        month = p2 + 1,
                        year = selectedYear
                    )
                }

                // Unused, here to prevent member implementation error
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
    }

    private fun initYearSpinner(currentYear: Int) {
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
                            val selectedYear = supportViewModel.years.value[p2].year

                            // Set selection as current year in view model
                            lifecycleScope.launch(Dispatchers.IO) {
                                supportViewModel.setCurrentYear(year = selectedYear)
                            }

                            val variableId = statsViewModel.variable.value.variableID
                            val type = statsViewModel.type.value
                            val month = supportViewModel.currentMonth.value
                            // Updates the workouts into the view model based on selection of month and year
                            statsViewModel.getStatisticsBySelection(
                                variableID = variableId,
                                type = type,
                                year = selectedYear,
                                month = month
                            )
                        }

                        // Unused, here to prevent member implementation error
                        override fun onNothingSelected(p0: AdapterView<*>?) {
                        }
                    }
            }
        }
    }

    private fun initVariableSpinner() {
        lifecycleScope.launch {
            // has to collect variables to reconstruct an new list of variables everytime a new variable is added
            statsViewModel.variables.collect {
                // Converts variables from type Variable() to type String,
                // which is the name of the variable
                val variablesConverted = mutableListOf<String>()
                statsViewModel.variables.value.forEach { variablesConverted.add(it.variableName) }

                val variableSpinnerAdapter = ArrayAdapter(
                    requireContext().applicationContext,
                    R.layout.custom_spinner_item,
                    variablesConverted
                )

                binding.variableSpinner.adapter = variableSpinnerAdapter

                // Set the first element of variables list as the selected variable
                binding.variableSpinner.setSelection(0)

                binding.variableSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            p2: Int,
                            p3: Long
                        ) {
                            // Gets the correct year from view model based on index p2
                            val year = supportViewModel.currentYear.value
                            val variables = statsViewModel.variables.value
                            val variableId = variables[p2].variableID
                            val type = statsViewModel.type.value
                            val month = supportViewModel.currentMonth.value

                            // Change the variable in view model as the selected variable
                            lifecycleScope.launch(Dispatchers.IO) {
                                statsViewModel.changeVariable(variables[p2])
                            }

                            // Updates the workouts into the view model based on selection of month and year
                            statsViewModel.getStatisticsBySelection(
                                variableID = variableId,
                                type = type,
                                year = year,
                                month = month
                            )
                        }

                        // Unused, here to prevent member implementation error
                        override fun onNothingSelected(p0: AdapterView<*>?) {
                        }
                    }
            }
        }
    }

    private fun initTypeSpinner() {
        val typeSpinnerAdapter = ArrayAdapter(
            requireContext().applicationContext,
            R.layout.custom_spinner_item,
            statsViewModel.getTypes()
        )

        binding.typeSpinner.adapter = typeSpinnerAdapter

        // Set the selection as the first type
        binding.typeSpinner.setSelection(0)

        binding.typeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    p2: Int,
                    p3: Long
                ) {
                    // Gets the correct year from view model based on index p2
                    val year = supportViewModel.currentYear.value
                    val variableId = statsViewModel.variable.value.variableID
                    val selectedType = statsViewModel.getTypes()[p2]
                    val month = supportViewModel.currentMonth.value

                    // Change the variable in view model as the selected variable
                    lifecycleScope.launch(Dispatchers.IO) {
                        statsViewModel.changeType(selectedType)
                    }

                    // Updates the workouts into the view model based on selection of month and year
                    statsViewModel.getStatisticsBySelection(
                        variableID = variableId,
                        type = selectedType,
                        year = year,
                        month = month
                    )
                }

                // Unused, here to prevent member implementation error
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
    }
}
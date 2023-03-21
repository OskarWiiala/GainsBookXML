package com.example.gainsbookxml.utils

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModel
import com.example.gainsbookxml.R
import com.example.gainsbookxml.viewmodels.LogViewModel
import com.example.gainsbookxml.viewmodels.StatsViewModel
import com.example.gainsbookxml.viewmodels.SupportViewModel
import com.example.gainsbookxml.viewmodels.TimerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.InvalidParameterException

/**
 * Dropdown spinner for selecting a month
 * @param spinner a reference to the AppCompatSpinner to be used
 * @param supportViewModel used to get and set the current month. Also used to get the year from the view model
 * @param mainViewModel has to be a LogViewModel or StatsViewModel. Since this spinner is reusable, it is used in multiple fragments
 * @param context needed for creating the spinner adapter
 * @param lifecycleScope needed for coroutine operations
 * @author Oskar Wiiala
 */
fun <T : ViewModel> monthSpinner(
    spinner: AppCompatSpinner,
    supportViewModel: SupportViewModel,
    mainViewModel: T,
    context: Context,
    lifecycleScope: LifecycleCoroutineScope,
) {
    // Enforces proper mainViewModel types
    if (mainViewModel !is StatsViewModel && mainViewModel !is LogViewModel) throw InvalidParameterException()

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
        context.applicationContext,
        R.layout.custom_spinner_item,
        months
    )

    val currentMonth = supportViewModel.currentMonth.value

    spinner.adapter = monthSpinnerAdapter
    spinner.setSelection(currentMonth - 1)

    spinner.onItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedMonth = p2 + 1
                val year = supportViewModel.currentYear.value

                lifecycleScope.launch(Dispatchers.IO) {
                    supportViewModel.setCurrentMonth(month = selectedMonth)
                }

                when (mainViewModel) {
                    is LogViewModel -> {
                        // Updates list of workouts to view model
                        mainViewModel.getWorkoutsByYearMonth(
                            year = year,
                            month = selectedMonth
                        )
                    }
                    is StatsViewModel -> {
                        val variableId = mainViewModel.variable.value.variableID
                        val type = mainViewModel.type.value
                        // Update the new statistics to view model
                        mainViewModel.getStatisticsBySelection(
                            variableID = variableId,
                            type = type,
                            month = selectedMonth,
                            year = year
                        )
                    }
                    else -> throw InvalidParameterException()
                }
            }

            // Unused, here to prevent member implementation error
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
}

/**
 * Dropdown spinner for selecting a year
 * @param spinner a reference to the AppCompatSpinner to be used
 * @param supportViewModel used to get and set the current year. Also used to get the month from the view model
 * @param mainViewModel has to be a LogViewModel or StatsViewModel. Since this spinner is reusable, it is used in multiple fragments
 * @param context needed for creating the spinner adapter
 * @param lifecycleScope needed for coroutine operations
 * @author Oskar Wiiala
 */
fun <T : ViewModel> yearSpinner(
    spinner: AppCompatSpinner,
    supportViewModel: SupportViewModel,
    mainViewModel: T,
    context: Context,
    lifecycleScope: LifecycleCoroutineScope,
) {
    if (mainViewModel !is StatsViewModel && mainViewModel !is LogViewModel) throw InvalidParameterException()

    val currentYear = supportViewModel.currentYear.value

    lifecycleScope.launch {
        // has to collect years to reconstruct an new list of years everytime a new year is added
        supportViewModel.years.collect {
            // Inserts current year to database is view model does not have any years
            // Inserting an already existing year just replaces it so no worries if the view model updates a little late
            if (supportViewModel.years.value.isEmpty()) {
                supportViewModel.insertYear(currentYear)
            }

            // Converts years from type Year() to type Int
            val yearsConverted = mutableListOf<Int>()
            supportViewModel.years.value.forEach { yearsConverted.add(it.year) }

            val yearSpinnerAdapter = ArrayAdapter(
                context.applicationContext,
                R.layout.custom_spinner_item,
                yearsConverted
            )

            spinner.adapter = yearSpinnerAdapter

            // Sets the selected year as current year, if current year exists
            if (yearsConverted.contains(currentYear)) {
                spinner.setSelection(yearsConverted.indexOf(currentYear))
            } else spinner.setSelection(0)

            spinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        p0: AdapterView<*>?,
                        p1: View?,
                        p2: Int,
                        p3: Long
                    ) {
                        val selectedYear = supportViewModel.years.value[p2].year
                        val month = supportViewModel.currentMonth.value

                        lifecycleScope.launch(Dispatchers.IO) {
                            supportViewModel.setCurrentYear(year = selectedYear)
                        }

                        when (mainViewModel) {
                            is LogViewModel -> {
                                // Updates the workouts into the view model based on selection of month and year
                                mainViewModel.getWorkoutsByYearMonth(
                                    year = selectedYear,
                                    month = month
                                )
                            }
                            is StatsViewModel -> {
                                val variableId = mainViewModel.variable.value.variableID
                                val type = mainViewModel.type.value

                                mainViewModel.getStatisticsBySelection(
                                    variableID = variableId,
                                    type = type,
                                    year = selectedYear,
                                    month = month
                                )
                            }
                            else -> throw InvalidParameterException()
                        }
                    }

                    // Unused, here to prevent member implementation error
                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }
                }
        }
    }
}

/**
 * Dropdown spinner for selecting a variable
 * @param spinner a reference to the AppCompatSpinner to be used
 * @param supportViewModel used to get year and month
 * @param mainViewModel
 * @param context needed for creating the spinner adapter
 * @param lifecycleScope needed for coroutine operations
 * @author Oskar Wiiala
 */
fun variableSpinner(
    spinner: AppCompatSpinner,
    supportViewModel: SupportViewModel,
    mainViewModel: StatsViewModel,
    context: Context,
    lifecycleScope: LifecycleCoroutineScope,
) {
    lifecycleScope.launch {
        // has to collect variables to reconstruct an new list of variables everytime a new variable is added
        mainViewModel.variables.collect {
            // Converts variables from type Variable() to type String,
            // which is the name of the variable
            val variablesConverted = mutableListOf<String>()
            mainViewModel.variables.value.forEach { variablesConverted.add(it.variableName) }

            val variableSpinnerAdapter = ArrayAdapter(
                context.applicationContext,
                R.layout.custom_spinner_item,
                variablesConverted
            )

            spinner.adapter = variableSpinnerAdapter

            // Set the first element of variables list as the selected variable
            spinner.setSelection(0)

            spinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        p0: AdapterView<*>?,
                        p1: View?,
                        p2: Int,
                        p3: Long
                    ) {
                        val month = supportViewModel.currentMonth.value
                        val year = supportViewModel.currentYear.value

                        val variables = mainViewModel.variables.value
                        val selectedVariable = variables[p2]
                        val type = mainViewModel.type.value

                        // Change the variable in view model as the selected variable
                        lifecycleScope.launch(Dispatchers.IO) {
                            mainViewModel.changeVariable(selectedVariable)
                        }

                        // Updates the workouts into the view model based on selection of month and year
                        mainViewModel.getStatisticsBySelection(
                            variableID = selectedVariable.variableID,
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

/**
 * Dropdown spinner for selecting a type
 * @param spinner a reference to the AppCompatSpinner to be used
 * @param supportViewModel used to get the current month and year
 * @param mainViewModel
 * @param context needed for creating the spinner adapter
 * @param lifecycleScope needed for coroutine operations
 * @author Oskar Wiiala
 */
fun typeSpinner(
    spinner: AppCompatSpinner,
    supportViewModel: SupportViewModel,
    mainViewModel: StatsViewModel,
    context: Context,
    lifecycleScope: LifecycleCoroutineScope,
) {
    val typeSpinnerAdapter = ArrayAdapter(
        context.applicationContext,
        R.layout.custom_spinner_item,
        mainViewModel.getTypes()
    )

    spinner.adapter = typeSpinnerAdapter

    // Set the selection as the first type
    spinner.setSelection(0)

    spinner.onItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                p0: AdapterView<*>?,
                p1: View?,
                p2: Int,
                p3: Long
            ) {
                val month = supportViewModel.currentMonth.value
                val year = supportViewModel.currentYear.value

                val variableId = mainViewModel.variable.value.variableID
                val selectedType = mainViewModel.getTypes()[p2]

                // Change the variable in view model as the selected variable
                lifecycleScope.launch(Dispatchers.IO) {
                    mainViewModel.changeType(selectedType)
                }

                // Updates the workouts into the view model based on selection of month and year
                mainViewModel.getStatisticsBySelection(
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

/**
 * Dropdown spinner for selecting a time
 * @param spinner a reference to the AppCompatSpinner to be used
 * @param mainViewModel
 * @param context needed for creating the spinner adapter
 * @param lifecycleScope needed for coroutine operations
 * @author Oskar Wiiala
 */
fun timeSpinner(
    spinner: AppCompatSpinner,
    mainViewModel: TimerViewModel,
    context: Context,
    lifecycleScope: LifecycleCoroutineScope,
) {
    // Converts time types from type CustomTimeType to type String,
    // which is the time in text
    val timeTypesConverted = mutableListOf<String>()
    mainViewModel.customTimeTypes.forEach { timeTypesConverted.add(it.type) }

    val typeSpinnerAdapter = ArrayAdapter(
        context.applicationContext,
        R.layout.custom_spinner_item,
        timeTypesConverted
    )

    spinner.adapter = typeSpinnerAdapter

    // Set the selection as the first type
    spinner.setSelection(0)

    spinner.onItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                p0: AdapterView<*>?,
                p1: View?,
                p2: Int,
                p3: Long
            ) {
                // Change the timeType in view model as the selected time type
                lifecycleScope.launch(Dispatchers.IO) {
                    mainViewModel.setCustomTimeType(mainViewModel.customTimeTypes[p2])
                }
            }

            // Unused, here to prevent member implementation error
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
}


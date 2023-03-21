package com.example.gainsbookxml.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.gainsbookxml.databinding.FragmentNewStatisticBinding
import com.example.gainsbookxml.utils.WorkoutDate
import com.example.gainsbookxml.utils.pickDatePopup
import com.example.gainsbookxml.utils.typeSpinner
import com.example.gainsbookxml.utils.variableSpinner
import com.example.gainsbookxml.viewmodels.StatsViewModel
import com.example.gainsbookxml.viewmodels.StatsViewModelFactory
import com.example.gainsbookxml.viewmodels.SupportViewModel
import com.example.gainsbookxml.viewmodels.SupportViewModelFactory
import kotlinx.coroutines.launch
import java.util.*

/**
 * This fragment is used to add a new statistic to the database.
 * User selects the variable and type and then gives it a value.
 * Clicking OK adds the newly created statistic to database and navigates back to StatsFragment
 * @author Oskar Wiiala
 */
class NewStatisticFragment : Fragment() {
    private lateinit var binding: FragmentNewStatisticBinding

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
        binding = FragmentNewStatisticBinding.inflate(layoutInflater)

        initUI()

        return binding.root
    }

    private fun initUI() {
        val calendar = Calendar.getInstance()

        // Adds the date to view model based on current date
        supportViewModel.setDate(
            WorkoutDate(
                day = calendar.get(Calendar.DAY_OF_MONTH),
                month = calendar.get(Calendar.MONTH) + 1,
                year = calendar.get(Calendar.YEAR)
            )
        )

        // Updates the date in the UI whenever date changes in view model
        lifecycleScope.launch {
            supportViewModel.date.collect {
                binding.date = "${it.day}.${it.month}.${it.year}"
            }
        }

        // Calendar picker
        binding.pickDateButton.setOnClickListener {
            // Initiate custom datePickerDialog
            pickDatePopup(
                supportViewModel = supportViewModel,
                context = requireContext(),
            )
        }

        // For selecting the variable
        variableSpinner(
            spinner = binding.variableSpinner,
            supportViewModel = supportViewModel,
            mainViewModel = statsViewModel,
            context = requireContext(),
            lifecycleScope = lifecycleScope,
        )

        // For selecting the type
        typeSpinner(
            spinner = binding.typeSpinner,
            supportViewModel = supportViewModel,
            mainViewModel = statsViewModel,
            context = requireContext(),
            lifecycleScope = lifecycleScope,
        )

        // OK button, which adds a new workout with exercises to database via view model
        // And then navigates back to LogFragment
        binding.buttonOk.setOnClickListener {
            val userInput = binding.editText.text.toString()
            val safeInput = userInput.toDoubleOrNull()
            if (safeInput != null) {
                statsViewModel.insertStatistic(
                    variableName = statsViewModel.variable.value.variableName,
                    type = statsViewModel.type.value,
                    value = safeInput,
                    day = supportViewModel.date.value.day,
                    month = supportViewModel.date.value.month,
                    year = supportViewModel.date.value.year
                )
                val direction =
                    NewStatisticFragmentDirections.actionNewStatisticFragmentToStatsFragment()
                findNavController().navigate(direction)
            } else binding.editText.error = "Invalid value type. Value must contain numbers only."
        }

        // Cancel button, navigates back to LogFragment
        binding.buttonCancel.setOnClickListener {
            val direction =
                NewStatisticFragmentDirections.actionNewStatisticFragmentToStatsFragment()
            findNavController().navigate(direction)
        }
    }
}
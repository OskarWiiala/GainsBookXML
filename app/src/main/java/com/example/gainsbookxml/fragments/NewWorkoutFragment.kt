package com.example.gainsbookxml.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gainsbookxml.adapters.ExerciseListAdapter
import com.example.gainsbookxml.databinding.FragmentNewWorkoutBinding
import com.example.gainsbookxml.utils.*
import com.example.gainsbookxml.viewmodels.SupportViewModel
import com.example.gainsbookxml.viewmodels.SupportViewModelFactory
import kotlinx.coroutines.launch
import java.util.*

class NewWorkoutFragment : Fragment(), ExerciseClickListener {
    val TAG = "NewWorkoutFragment"

    private lateinit var binding: FragmentNewWorkoutBinding

    private val supportViewModel: SupportViewModel by viewModels {
        SupportViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewWorkoutBinding.inflate(layoutInflater)

        initUI()
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun initUI() {
        val calendar = Calendar.getInstance()

        supportViewModel.setDate(
            WorkoutDate(
                day = calendar.get(Calendar.DAY_OF_MONTH),
                month = calendar.get(Calendar.MONTH) + 1,
                year = calendar.get(Calendar.YEAR)
            )
        )

        lifecycleScope.launch {
            // Updates date in UI whenever date in view model changes
            supportViewModel.date.collect {
                binding.date = "${it.day}.${it.month}.${it.year}"
            }
        }

        // Calendar picker
        binding.pickDateButton.setOnClickListener {
            // Initiate datePickerDialog here
            pickDatePopup(
                supportViewModel = supportViewModel,
                context = requireContext(),
            )
        }
        // List of exercises
        binding.ExerciseList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter =
                ExerciseListAdapter(
                    supportViewModel = supportViewModel,
                    clickListener = this@NewWorkoutFragment,
                )
            lifecycleScope.launch {
                supportViewModel.exercises.collect {
                    // quick and dirty
                    (adapter as ExerciseListAdapter).notifyDataSetChanged()
                }
            }

        }

        // Add new exercise button
        binding.buttonAddNewExercise.setOnClickListener {
            newExercisePopup(
                supportViewModel = supportViewModel,
                context = requireContext(),
                type = "new",
                description = "",
                exerciseIndex = 0
            )
        }

        // Ok/Cancel
        binding.buttonOk.setOnClickListener {
            Log.d(TAG, "clicked OK")
            supportViewModel.addWorkout(
                exercises = supportViewModel.exercises.value,
                day = supportViewModel.date.value.day,
                month = supportViewModel.date.value.month,
                year = supportViewModel.date.value.year
            )
            val direction = NewWorkoutFragmentDirections.actionNewWorkoutFragmentToLogFragment()
            findNavController().navigate(direction)
        }

        binding.buttonCancel.setOnClickListener {
            Log.d(TAG, "clicked cancel")
            val direction = NewWorkoutFragmentDirections.actionNewWorkoutFragmentToLogFragment()
            findNavController().navigate(direction)
        }

    }

    override fun onEditClick(description: String, exerciseIndex: Int) {
        newExercisePopup(
            supportViewModel = supportViewModel,
            context = requireContext(),
            type = "edit",
            description = description,
            exerciseIndex = exerciseIndex
        )
    }

    override fun onDeleteClick(description: String, exerciseIndex: Int) {
        deletePopup(
            supportViewModel = supportViewModel,
            context = requireContext(),
            description = description,
            exerciseIndex = exerciseIndex
        )
    }
}
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

/**
 * This fragment is used to create a new workout.
 * @author Oskar Wiiala
 */
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

        // Applies the layout manager and adapter for the recycler view
        // that lists all the exercises of this workout
        binding.ExerciseList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter =
                ExerciseListAdapter(
                    supportViewModel = supportViewModel,
                    clickListener = this@NewWorkoutFragment,
                    type = "card"
                )

            // Updates the list whenever any changes are made to the list
            // of exercises in the view model
            lifecycleScope.launch {
                supportViewModel.exercises.collect {
                    // quick and dirty
                    (adapter as ExerciseListAdapter).notifyDataSetChanged()
                }
            }
        }

        // Displays a popup whenever the + new exercise button is clicked.
        // The popup consists of an edit text, where the user can add a new exercise
        binding.buttonAddNewExercise.setOnClickListener {
            newExercisePopup(
                supportViewModel = supportViewModel,
                context = requireContext(),
                type = "new",
                description = "",
                exerciseIndex = 0
            )
        }

        // OK button, which adds a new workout with exercises to database via view model
        // And then navigates back to LogFragment
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

        // Cancel button, navigates back to LogFragment
        binding.buttonCancel.setOnClickListener {
            Log.d(TAG, "clicked cancel")
            val direction = NewWorkoutFragmentDirections.actionNewWorkoutFragmentToLogFragment()
            findNavController().navigate(direction)
        }
    }

    // Is called whenever the edit-ImageButton is clicked on an exercise card.
    // Displays a popup which is used to edit a selected exercise
    override fun onEditClick(description: String, exerciseIndex: Int) {
        newExercisePopup(
            supportViewModel = supportViewModel,
            context = requireContext(),
            type = "edit",
            description = description,
            exerciseIndex = exerciseIndex
        )
    }

    // Is called whenever the delete-ImageButton is clicked on an exercise card.
    // Displays a popup which is used to confirm deletion of a selected exercise
    override fun onDeleteClick(description: String, exerciseIndex: Int) {
        deletePopup(
            supportViewModel = supportViewModel,
            context = requireContext(),
            description = description,
            exerciseIndex = exerciseIndex
        )
    }
}
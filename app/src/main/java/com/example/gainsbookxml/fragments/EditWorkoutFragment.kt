package com.example.gainsbookxml.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gainsbookxml.adapters.ExerciseListAdapter
import com.example.gainsbookxml.databinding.FragmentEditWorkoutBinding
import com.example.gainsbookxml.utils.ExerciseClickListener
import com.example.gainsbookxml.utils.deletePopup
import com.example.gainsbookxml.utils.newExercisePopup
import com.example.gainsbookxml.utils.pickDatePopup
import com.example.gainsbookxml.viewmodels.SupportViewModel
import com.example.gainsbookxml.viewmodels.SupportViewModelFactory
import kotlinx.coroutines.launch

/**
 * This fragment is used to edit the date and exercises of a selected workout.
 * @author Oskar Wiiala
 */
class EditWorkoutFragment : Fragment(), ExerciseClickListener {
    // Arguments that were passed when navigating here
    // Should only have workoutId
    private val args: EditWorkoutFragmentArgs by navArgs()

    private lateinit var binding: FragmentEditWorkoutBinding

    private val supportViewModel: SupportViewModel by viewModels {
        SupportViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditWorkoutBinding.inflate(layoutInflater)

        initUI()
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun initUI() {
        // Adds the selected workout to view model based on workout id
        supportViewModel.getWorkoutByID(workoutID = args.workoutId)

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
                context = requireContext()
            )
        }

        // Applies the layout manager and adapter for the recycler view
        // that lists all the exercises of this workout
        binding.ExerciseList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter =
                ExerciseListAdapter(
                    viewModel = supportViewModel,
                    clickListener = this@EditWorkoutFragment,
                    type = "card"
                )

            // Updates the list whenever any changes are made to the list
            // of exercises in the view model
            lifecycleScope.launch {
                supportViewModel.exercises.collect {
                    // quick and dirty
                    (adapter as ExerciseListAdapter<*>).notifyDataSetChanged()
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
            supportViewModel.addWorkout(
                workoutID = args.workoutId,
                exercises = supportViewModel.exercises.value,
                day = supportViewModel.date.value.day,
                month = supportViewModel.date.value.month,
                year = supportViewModel.date.value.year,
                type = "delete"
            )
            // Give the view model some time to update the state of workouts before accessing it
            Thread.sleep(10L)
            val direction = EditWorkoutFragmentDirections.actionEditWorkoutFragmentToLogFragment()
            findNavController().navigate(direction)
        }

        // Cancel button, navigates back to LogFragment
        binding.buttonCancel.setOnClickListener {
            val direction = EditWorkoutFragmentDirections.actionEditWorkoutFragmentToLogFragment()
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
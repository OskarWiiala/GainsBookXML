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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gainsbookxml.R
import com.example.gainsbookxml.adapters.ExerciseListAdapter
import com.example.gainsbookxml.databinding.FragmentEditWorkoutBinding
import com.example.gainsbookxml.databinding.FragmentNewWorkoutBinding
import com.example.gainsbookxml.utils.ExerciseClickListener
import com.example.gainsbookxml.utils.deletePopup
import com.example.gainsbookxml.utils.newExercisePopup
import com.example.gainsbookxml.utils.pickDatePopup
import com.example.gainsbookxml.viewmodels.SupportViewModel
import com.example.gainsbookxml.viewmodels.SupportViewModelFactory
import kotlinx.coroutines.launch

class EditWorkoutFragment : Fragment(), ExerciseClickListener {
    val TAG = "EditWorkoutFragment"

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

        lifecycleScope.launch {
            supportViewModel.date.collect {
                binding.date = "${it.day}.${it.month}.${it.year}"
            }
        }

        // Calendar picker
        binding.pickDateButton.setOnClickListener {
            // Initiate datePickerDialog here
            pickDatePopup(
                supportViewModel = supportViewModel,
                context = requireContext()
            )
        }

        // List of exercises
        binding.ExerciseList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter =
                ExerciseListAdapter(
                    supportViewModel = supportViewModel,
                    clickListener = this@EditWorkoutFragment,
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
                workoutID = args.workoutId,
                exercises = supportViewModel.exercises.value,
                day = supportViewModel.date.value.day,
                month = supportViewModel.date.value.month,
                year = supportViewModel.date.value.year,
                type = "delete"
            )
            val direction = EditWorkoutFragmentDirections.actionEditWorkoutFragmentToLogFragment()
            findNavController().navigate(direction)
        }

        binding.buttonCancel.setOnClickListener {
            Log.d(TAG, "clicked cancel")
            val direction = EditWorkoutFragmentDirections.actionEditWorkoutFragmentToLogFragment()
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
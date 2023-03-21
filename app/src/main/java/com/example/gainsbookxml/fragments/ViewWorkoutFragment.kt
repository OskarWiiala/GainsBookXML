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
import com.example.gainsbookxml.databinding.FragmentViewWorkoutBinding
import com.example.gainsbookxml.viewmodels.ViewWorkoutViewModel
import com.example.gainsbookxml.viewmodels.ViewWorkoutViewModelFactory
import kotlinx.coroutines.launch

/**
 * This fragment is used to view a selected workout.
 * @author Oskar Wiiala
 */
class ViewWorkoutFragment : Fragment() {
    private val args: ViewWorkoutFragmentArgs by navArgs()

    private lateinit var binding: FragmentViewWorkoutBinding

    private val viewWorkoutViewModel: ViewWorkoutViewModel by viewModels {
        ViewWorkoutViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewWorkoutBinding.inflate(layoutInflater)
        initUI()
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun initUI() {
        // Adds the selected workout to view model based on workout id
        viewWorkoutViewModel.getWorkout(workoutID = args.workoutId)

        // Updates the date in UI whenever the date changes in view model
        lifecycleScope.launch {
            viewWorkoutViewModel.workout.collect {
                binding.date =
                    "${it.firstOrNull()?.workout?.day}.${it.firstOrNull()?.workout?.month}.${it.firstOrNull()?.workout?.year}"
            }
        }

        // Applies the layout manager and adapter for the recycler view
        // that lists all the exercises of this workout
        binding.ExerciseList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter =
                ExerciseListAdapter(
                    viewModel = viewWorkoutViewModel,
                    clickListener = null,
                    type = "item"
                )
        }

        // Navigate back to LogFragment when clicking "Back"-button
        binding.buttonBack.setOnClickListener {
            val direction = ViewWorkoutFragmentDirections.actionViewWorkoutFragmentToLogFragment()
            findNavController().navigate(direction)
        }
    }
}
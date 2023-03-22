package com.example.gainsbookxml.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gainsbookxml.databinding.ExerciseCardBinding
import com.example.gainsbookxml.databinding.ExerciseItemBinding
import com.example.gainsbookxml.utils.*
import com.example.gainsbookxml.viewmodels.SupportViewModel
import com.example.gainsbookxml.viewmodels.ViewWorkoutViewModel
import java.security.InvalidParameterException

/**
 * Custom list adapter for exercises.
 * Used by NewWorkoutFragment, EditWorkoutFragment and ViewWorkoutFragment.
 * Uses two types of ViewHolders, ItemViewHolder and CardViewHolder.
 * ItemViewHolder displays a very basic text view with the exercise.
 * CardViewHolder displays a fancy card with a red border and edit/delete buttons
 * @param viewModel Used for handling dates and exercises
 * @param clickListener used to call an interface function for edit/delete functions
 * @param type pass item or card. With item, ItemViewHolder is loaded, with card, CardViewHolder is loaded.
 * @author Oskar Wiiala
 */
class ExerciseListAdapter<T: ViewModel>(
    private val viewModel: T,
    private val clickListener: ExerciseClickListener?,
    private val type: String
) : ListAdapter<ExerciseItem, RecyclerView.ViewHolder>(ExerciseItemCallBack()) {
    // View holder for ViewWorkoutFragment
    // View holder holds a simple text view
    class ItemViewHolder(private val binding: ExerciseItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    ExerciseItemBinding.inflate(layoutInflater, parent, false)
                return ItemViewHolder(binding)
            }
        }

        // Handle whatever UI elements you want
        fun bind(
            exercise: String,
        ) {
            // binding.exercise is a data variable used to display the exercise as text
            binding.exercise = exercise
        }
    }


    // View holder for fragments NewWorkoutFragment and EditWorkoutFragment
    // View holder holds a card with an edit and delete button
    class CardViewHolder(private val binding: ExerciseCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): CardViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    ExerciseCardBinding.inflate(layoutInflater, parent, false)
                return CardViewHolder(binding)
            }
        }

        // Handle whatever UI elements you want
        fun bind(
            item: ExerciseWithIndex,
            clickListener: ExerciseClickListener?
        ) {
            // binding.exercise is a data variable used to display the exercise as text
            binding.exercise = item.description

            // Click listener for clicking edit button
            binding.editExerciseButton.setOnClickListener {
                // Calls interface function to edit click functionality
                clickListener?.onEditClick(
                    description = item.description,
                    exerciseIndex = item.index
                )
            }

            // Click listener for clicking delete button
            binding.deleteExerciseButton.setOnClickListener {
                // Calls interface function to delete click functionality
                clickListener?.onDeleteClick(
                    description = item.description,
                    exerciseIndex = item.index
                )

            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (type) {
            // Basic text view holder
            "item" -> ItemViewHolder.from(parent)
            // Fancy card view holder
            "card" -> CardViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CardViewHolder -> {
                if(viewModel !is SupportViewModel) throw InvalidParameterException()
                // The specific exercise with index item based on position in list
                val item = viewModel.exercises.value[position]
                holder.bind(
                    item = item,
                    clickListener = clickListener
                )
            }
            is ItemViewHolder -> {
                if(viewModel !is ViewWorkoutViewModel) throw InvalidParameterException()
                // Same as in CardViewHolder but with just the exercise description
                val exercise = viewModel.workout.value.first().exercises[position].description
                holder.bind(exercise = exercise)
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        // Give the view model some time to load
        Thread.sleep(10L)
        return if (viewModel is SupportViewModel) viewModel.exercises.value.size
        else if(viewModel is ViewWorkoutViewModel) viewModel.workout.value.first().exercises.size
        else throw InvalidParameterException()
    }
}

class ExerciseItemCallBack : DiffUtil.ItemCallback<ExerciseItem>() {
    override fun areItemsTheSame(oldItem: ExerciseItem, newItem: ExerciseItem): Boolean {
        return oldItem.exerciseIndex == newItem.exerciseIndex
    }

    override fun areContentsTheSame(oldItem: ExerciseItem, newItem: ExerciseItem): Boolean {
        return oldItem == newItem
    }
}
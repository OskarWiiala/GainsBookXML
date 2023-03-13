package com.example.gainsbookxml

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gainsbookxml.database.relations.WorkoutWithExercises
import com.example.gainsbookxml.databinding.WorkoutCardBinding
import com.example.gainsbookxml.utils.WorkoutClickListener
import com.example.gainsbookxml.utils.WorkoutItem
import com.example.gainsbookxml.viewmodels.LogViewModel

/**
 * Custom list adapter for workouts.
 * Used by LogFragment.
 * @param logViewModel Used for handling workouts
 * @param clickListener used to call an interface function for view/edit/delete functions
 * @author Oskar Wiiala
 */
class WorkoutListAdapter(
    private val logViewModel: LogViewModel,
    private val clickListener: WorkoutClickListener
) : ListAdapter<WorkoutItem, RecyclerView.ViewHolder>(WorkoutItemCallBack()) {

    // View holder holds a card that displays the three
    // first exercises of the workout and edi/delete buttons
    class ViewHolder(private val binding: WorkoutCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    WorkoutCardBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(item: WorkoutWithExercises, clickListener: WorkoutClickListener) {
            val TAG = "onBindViewHolder"

            // Date to be displayed at the top of workout_card
            binding.date = "<u>${item.workout.day}.${item.workout.month}.${item.workout.year}</u>"
            val exercisePreviews = item.exercises.take(3)
            // Card displays first three exercises as previews
            binding.exercisePreview1 = exercisePreviews.getOrNull(0)?.description ?: ""
            binding.exercisePreview2 = exercisePreviews.getOrNull(1)?.description ?: ""
            binding.exercisePreview3 = exercisePreviews.getOrNull(2)?.description ?: ""

            // Click listener for clicking card
            binding.workoutCard.setOnClickListener {
                Log.d(TAG, "clicked on card with item: $item")
                clickListener.onViewClick(item.workout.workoutID)
            }
            // Click listener for clicking delete button
            binding.deleteWorkoutButton.setOnClickListener {
                Log.d(TAG, "clicked delete button with item: $item")
                clickListener.onDeleteClick(
                    workoutId = item.workout.workoutID,
                    year = item.workout.year,
                    month = item.workout.month
                )
            }

            // Click listener for clicking edit button
            binding.editWorkoutButton.setOnClickListener {
                Log.d(TAG, "clicked edit button with item: $item")
                clickListener.onEditClick(workoutId = item.workout.workoutID)
            }
        }
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return ViewHolder.from(parent)
    }


    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val TAG = "onBindViewHolder"

        when (holder) {
            is ViewHolder -> {
                Log.d(TAG, "is RecyclerView.ViewHolder, position: $position")
                val item = logViewModel.workouts.value[position]
                holder.bind(item = item, clickListener = clickListener)
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        Log.d("getItemCount", "size of workouts: ${logViewModel.workouts.value.size}")
        return logViewModel.workouts.value.size
    }
}

class WorkoutItemCallBack : DiffUtil.ItemCallback<WorkoutItem>() {
    override fun areItemsTheSame(oldItem: WorkoutItem, newItem: WorkoutItem): Boolean {
        return oldItem.workoutId == newItem.workoutId
    }

    override fun areContentsTheSame(oldItem: WorkoutItem, newItem: WorkoutItem): Boolean {
        return oldItem == newItem
    }
}
package com.example.gainsbookxml.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gainsbookxml.databinding.ExerciseCardBinding
import com.example.gainsbookxml.databinding.ExerciseItemBinding
import com.example.gainsbookxml.utils.*
import com.example.gainsbookxml.viewmodels.SupportViewModel

class ExerciseListAdapter(
    private val supportViewModel: SupportViewModel,
    private val clickListener: ExerciseClickListener?,
    private val type: String
) : ListAdapter<ExerciseItem, RecyclerView.ViewHolder>(ExerciseItemCallBack()) {
    val TAG = "ExerciseListAdapter"

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

        fun bind(
            exercise: String,
        ) {
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

        fun bind(
            item: ExerciseWithIndex,
            clickListener: ExerciseClickListener?
        ) {
            val TAG = "onBindViewHolder"
            binding.exercise = item.description

            // Click listener for clicking edit button
            binding.editExerciseButton.setOnClickListener {
                Log.d(TAG, "clicked edit button with item: $item")
                clickListener?.onEditClick(
                    description = item.description,
                    exerciseIndex = item.index
                )
            }

            // Click listener for clicking delete button
            binding.deleteExerciseButton.setOnClickListener {
                Log.d(TAG, "clicked delete button with item: $item")
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
        Log.d(TAG, "viewType: $viewType")
        return when (type) {
            "item" -> ItemViewHolder.from(parent)
            "card" -> CardViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val TAG = "onBindViewHolder"

        when (holder) {
            is CardViewHolder -> {
                Log.d(TAG, "is CardViewHolder, position: $position")
                val item = supportViewModel.exercises.value[position]
                holder.bind(
                    item = item,
                    clickListener = clickListener
                )
            }
            is ItemViewHolder -> {
                Log.d(TAG, "is ItemViewHolder, position: $position")
                val exercise = supportViewModel.exercises.value[position].description
                holder.bind(exercise = exercise)
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        Log.d("getItemCount", "size of exercises: ${supportViewModel.exercises.value.size}")
        return supportViewModel.exercises.value.size
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
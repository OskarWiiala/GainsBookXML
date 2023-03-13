package com.example.gainsbookxml.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gainsbookxml.databinding.ExerciseCardBinding
import com.example.gainsbookxml.utils.*
import com.example.gainsbookxml.viewmodels.SupportViewModel

class ExerciseListAdapter(
    private val supportViewModel: SupportViewModel,
    private val clickListener: ExerciseClickListener,
) : ListAdapter<ExerciseItem, RecyclerView.ViewHolder>(ExerciseItemCallBack()) {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(private val binding: ExerciseCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    ExerciseCardBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(
            item: ExerciseWithIndex,
            clickListener: ExerciseClickListener
        ) {
            val TAG = "onBindViewHolder"
            binding.exercise = item.description

            // Click listener for clicking edit button
            binding.editExerciseButton.setOnClickListener {
                Log.d(TAG, "clicked edit button with item: $item")
                clickListener.onEditClick(
                    description = item.description,
                    exerciseIndex = item.index
                )
            }

            // Click listener for clicking delete button
            binding.deleteExerciseButton.setOnClickListener {
                Log.d(TAG, "clicked delete button with item: $item")
                clickListener.onDeleteClick(description = item.description, exerciseIndex = item.index)

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
                val item = supportViewModel.exercises.value[position]
                holder.bind(
                    item = item,
                    clickListener = clickListener
                )
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
package com.example.gainsbookxml.utils

import com.example.gainsbookxml.database.relations.WorkoutWithExercises

/**
 * Completely unsued. I don't want to delete this because it would break my RecyclerviewAdapters. Too bad.
 */
sealed class WorkoutItem {
    abstract val workoutId: Int

    data class Item(val data: WorkoutWithExercises) : WorkoutItem() {
        override val workoutId = data.workout.workoutID
    }
}

/**
 * Completely unsued. I don't want to delete this because it would break my RecyclerviewAdapters. Too bad.
 */
sealed class ExerciseItem {
    abstract val exerciseIndex: Int

    data class Item(val data: ExerciseWithIndex) : ExerciseItem() {
        override val exerciseIndex = data.index
    }
}




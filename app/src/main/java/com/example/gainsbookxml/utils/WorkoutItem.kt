package com.example.gainsbookxml.utils

import com.example.gainsbookxml.database.relations.WorkoutWithExercises

sealed class WorkoutItem {
    abstract val workoutId: Int

    data class Item(val data: WorkoutWithExercises) : WorkoutItem() {
        override val workoutId = data.workout.workoutID
    }
}

sealed class ExerciseItem {
    abstract val exerciseIndex: Int

    data class Item(val data: ExerciseWithIndex) : ExerciseItem() {
        override val exerciseIndex = data.index
    }
}




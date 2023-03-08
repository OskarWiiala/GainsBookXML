package com.example.gainsbookxml.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gainsbookxml.database.entities.Exercise
import com.example.gainsbookxml.database.entities.Workout

data class WorkoutWithExercises(
    @Embedded val workout: Workout,
    @Relation(
        parentColumn = "workoutID",
        entityColumn = "workoutID"
    )
    val exercises: List<Exercise>
)
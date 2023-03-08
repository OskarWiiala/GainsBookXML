package com.example.gainsbookxml.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val workoutID: Int,
    val year: Int,
    val month: Int,
    val day: Int
)
package com.example.gainsbookxml.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val exerciseID: Int,
    val workoutID: Int,
    val description: String,
    val year: Int,
    val month: Int,
    val day: Int
)
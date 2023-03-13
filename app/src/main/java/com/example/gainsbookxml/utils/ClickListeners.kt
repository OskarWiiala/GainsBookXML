package com.example.gainsbookxml.utils

interface WorkoutClickListener {
    fun onViewClick(workoutId: Int)
    fun onEditClick(workoutId: Int)
    fun onDeleteClick(workoutId: Int, year: Int, month: Int)
}

interface ExerciseClickListener {
    fun onEditClick(description: String, exerciseIndex: Int)
    fun onDeleteClick(description: String, exerciseIndex: Int)
}
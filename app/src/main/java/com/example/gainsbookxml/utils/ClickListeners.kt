package com.example.gainsbookxml.utils

/**
 * Listens to clicks on a workout card
 * @author Oskar Wiiala
 */
interface WorkoutClickListener {
    fun onViewClick(workoutId: Int)
    fun onEditClick(workoutId: Int)
    fun onDeleteClick(workoutId: Int, year: Int, month: Int)
}

/**
 * Listens to clicks on an exercise card
 * @author Oskar Wiiala
 */
interface ExerciseClickListener {
    fun onEditClick(description: String, exerciseIndex: Int)
    fun onDeleteClick(description: String, exerciseIndex: Int)
}
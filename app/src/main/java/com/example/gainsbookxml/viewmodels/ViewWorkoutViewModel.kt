package com.example.gainsbookxml.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gainsbookxml.database.AppDatabase
import com.example.gainsbookxml.database.relations.WorkoutWithExercises
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * @author Oskar Wiiala
 * @param context
 * View model for ViewWorkoutScreeb
 */
class ViewWorkoutViewModel(context: Context) : ViewModel() {
    private val dao = AppDatabase.getInstance(context).appDao

    private val _workout = MutableStateFlow(listOf<WorkoutWithExercises>())
    val workout: StateFlow<List<WorkoutWithExercises>> get() = _workout

    // Gets the workout from database based on workout ID and updates _workout
    fun getWorkout(workoutID: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = dao.getWorkoutWithExercisesByID(workoutID = workoutID)
            _workout.emit(response)
        }
    }
}

inline fun <VM : ViewModel> viewExerciseViewModelFactory(crossinline f: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = f() as T
    }
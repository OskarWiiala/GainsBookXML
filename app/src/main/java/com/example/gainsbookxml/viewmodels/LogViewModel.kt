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
 * View model for LogScreen
 */
class LogViewModel(context: Context) : ViewModel() {
    private val dao = AppDatabase.getInstance(context).appDao

    private val _workouts = MutableStateFlow(listOf<WorkoutWithExercises>())
    val workouts: StateFlow<List<WorkoutWithExercises>> get() = _workouts

    // Gets workout from database and updates _workouts
    fun getWorkoutsByYearMonth(year: Int, month: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val listWorkoutWithExercises = dao.getWorkoutWithExercisesByYearMonth(
                year = year,
                month = month
            )
            _workouts.emit(listWorkoutWithExercises)
        }
    }

    // Deletes workout and exercises from database based on ID and updates view model
    fun deleteWorkoutByID(workoutID: Int, year: Int, month: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteWorkoutByID(workoutID = workoutID)
            dao.deleteExercisesByWorkoutID(workoutID = workoutID)
            getWorkoutsByYearMonth(year = year, month = month)
        }
    }
}

inline fun <VM : ViewModel> logViewModelFactory(crossinline f: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = f() as T
    }
package com.example.gainsbookxml.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gainsbookxml.ExerciseWithIndex
import com.example.gainsbookxml.WorkoutDate
import com.example.gainsbookxml.database.AppDatabase
import com.example.gainsbookxml.database.entities.Exercise
import com.example.gainsbookxml.database.entities.Workout
import com.example.gainsbookxml.database.entities.Year
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SupportViewModel(context: Context) : ViewModel() {
    private val dao = AppDatabase.getInstance(context).appDao

    private val _exercises = MutableStateFlow(listOf<ExerciseWithIndex>())
    val exercises: StateFlow<List<ExerciseWithIndex>> get() = _exercises

    private val _date = MutableStateFlow(WorkoutDate(0, 0, 0))
    val date: StateFlow<WorkoutDate> get() = _date

    private val _years = MutableStateFlow(listOf<Year>())
    val years: StateFlow<List<Year>> get() = _years

    private val _currentYear = MutableStateFlow(0)
    val currentYear: StateFlow<Int> get() = _currentYear

    private val _currentMonth = MutableStateFlow(0)
    val currentMonth: StateFlow<Int> get() = _currentMonth

    fun addExercises(exercises: List<ExerciseWithIndex>) {
        viewModelScope.launch {
            _exercises.emit(exercises)
        }
    }

    fun getWorkoutByID(workoutID: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            // Get workout based on workoutID, which will later be loaded in the UI
            val response = dao.getWorkoutWithExercisesByID(workoutID = workoutID)
            // Converts response from WorkoutWithExercises to ExerciseWithIndex
            val convertedList: MutableList<ExerciseWithIndex> = mutableListOf()
            if (response.isNotEmpty()) {
                var index = 1
                response.first().exercises.forEach { exercise ->
                    convertedList.add(
                        ExerciseWithIndex(
                            description = exercise.description,
                            index = index
                        )
                    )
                    index++
                }
                _exercises.emit(convertedList)
                val day = response.first().workout.day
                val month = response.first().workout.month
                val year = response.first().workout.year
                val date = WorkoutDate(day = day, month = month, year = year)
                // Date is used to display the date of the workout
                setDate(date)
            }
        }
    }

    // Deletes workout from database based on workoutID
    private suspend fun deleteWorkout(workoutID: Int) {
        dao.deleteWorkoutByID(workoutID = workoutID)
        deleteExercises(workoutID = workoutID)
    }

    // Deletes exercises from database based on workoutID
    private suspend fun deleteExercises(workoutID: Int) {
        dao.deleteExercisesByWorkoutID(workoutID = workoutID)
    }

    // Adds workout to database
    fun addWorkout(
        exercises: List<ExerciseWithIndex>,
        workoutID: Int = 0,
        day: Int,
        month: Int,
        year: Int,
        type: String = "normal"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (type == "delete") {
                deleteWorkout(workoutID)
            }
            // Create workout and then insert it to the database
            val workout = Workout(workoutID = workoutID, day = day, month = month, year = year)
            val response = dao.insertWorkout(workout = workout)

            // Converts a list of ExerciseWithIndex to Exercise
            val exercisesConverted: MutableList<Exercise> = mutableListOf()
            exercises.forEach { exerciseWithIndex ->
                exercisesConverted.add(
                    Exercise(
                        exerciseID = 0,
                        workoutID = response.toInt(),
                        description = exerciseWithIndex.description,
                        day = day,
                        month = month,
                        year = year
                    )
                )
            }
            // inserts converted exercises to database
            exercisesConverted.forEach { dao.insertExercise(exercise = it) }
        }
    }

    fun setDate(date: WorkoutDate) {
        viewModelScope.launch(Dispatchers.IO) {
            _date.emit(date)
        }
    }

    // Gets years from database and updates _years
    fun getYears() {
        viewModelScope.launch(Dispatchers.IO) {
            val years = dao.getYears()
            _years.emit(years)
        }
    }

    // Inserts a year into database, gets years from database and updates _years
    fun insertYear(year: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertYear(year = Year(year))
            getYears()
        }
    }

    suspend fun setCurrentYear(year: Int) {
        _currentYear.emit(year)
    }

    suspend fun setCurrentMonth(month: Int) {
        _currentMonth.emit(month)
    }
}

inline fun <VM : ViewModel> supportViewModelFactory(crossinline f: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = f() as T
    }
package com.example.gainsbookxml.database

import androidx.room.*
import com.example.gainsbookxml.database.entities.*
import com.example.gainsbookxml.database.relations.WorkoutWithExercises

@Dao
interface AppDao {
    // suspend because they are executed on a background thread
    // in order to not block the main thread
    // onConflict is when you try to insert a workout which already exists

    // Insertions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertYear(year: Year)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariable(variable: Variable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatistic(statistic: Statistic)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile)

    // Updates
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateProfile(profile: Profile)

    // GET Queries
    // @Transaction is required to prevent multithreading problems
    @Transaction
    @Query("SELECT * FROM workout WHERE workoutID = :workoutID")
    suspend fun getWorkoutWithExercisesByID(workoutID: Int): List<WorkoutWithExercises>

    @Transaction
    @Query("SELECT * FROM workout WHERE year = :year AND month = :month")
    suspend fun getWorkoutWithExercisesByYearMonth(year: Int, month: Int): List<WorkoutWithExercises>

    @Transaction
    @Query("SELECT * FROM year")
    suspend fun getYears(): List<Year>

    @Transaction
    @Query("SELECT * FROM variable")
    suspend fun getVariables(): List<Variable>

    @Transaction
    @Query("Select variableID FROM variable WHERE variableName = :variableName")
    suspend fun getVariableIDByName(variableName: String): Int

    @Transaction
    @Query("SELECT * FROM statistic WHERE variableID = :variableID AND type = :type AND month = :month AND year = :year")
    suspend fun getStatisticsBySelection(variableID: Int, type: String, month: Int, year: Int): List<Statistic>

    @Transaction
    @Query("SELECT * FROM profile")
    suspend fun getProfile(): List<Profile>

    // Deletions
    @Transaction
    @Query("DELETE FROM workout WHERE workoutID = :workoutID")
    suspend fun deleteWorkoutByID(workoutID: Int)

    @Transaction
    @Query("DELETE FROM exercise WHERE workoutID = :workoutID")
    suspend fun deleteExercisesByWorkoutID(workoutID: Int)
}
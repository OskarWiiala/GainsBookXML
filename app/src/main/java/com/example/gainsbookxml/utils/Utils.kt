package com.example.gainsbookxml.utils

/**
 * @author Oskar Wiiala
 * @param exercises a list of exercises with index
 * @param textFieldState the state of an editable text field
 * @return a mutable list of exercises with index
 * This function handles adding a new exercise to an existing list of exercises
 * (which can be empty) and returns the new list
 */
fun newExercise(
    exercises: MutableList<ExerciseWithIndex>,
    textFieldState: String
): MutableList<ExerciseWithIndex> {
    var indexOfLast = 1

    // Creates a list of indexes based on the exercise index
    if (exercises.isNotEmpty()) {
        val indexList: MutableList<Int> = mutableListOf()
        exercises.forEach { exerciseWithIndex ->
            indexList.add(exerciseWithIndex.index)
        }
        indexOfLast = indexList.max() + 1
    }

    // Creates a new exercise with an index one higher than the previous
    exercises.add(
        ExerciseWithIndex(
            description = textFieldState,
            index = indexOfLast
        )
    )
    return exercises
}

/**
 * @author Oskar Wiiala
 * @param exercises a list of exercises with index
 * @param description the description of an individual exercise
 * @param exerciseIndex the index of an individual exercise
 * @param textFieldState the state of an editable text field
 * @return a mutable list of exercises with index
 * This function handles editing an individual exercise in an existing list of exercises,
 * keeps the index of the exercise and returns the new list
 */
fun editExercise(
    exercises: MutableList<ExerciseWithIndex>,
    description: String,
    exerciseIndex: Int,
    textFieldState: String
): MutableList<ExerciseWithIndex> {
    // removes the selected exercise from the newly created list
    exercises.remove(
        ExerciseWithIndex(
            description = description,
            index = exerciseIndex
        )
    )

    // Creates a new exercise with an index one higher than the previous
    exercises.add(
        ExerciseWithIndex(
            description = textFieldState,
            index = exerciseIndex
        )
    )
    // Sorts the list ascending based on the index of the exercise
    exercises.sortBy { exerciseWithIndex -> exerciseWithIndex.index }

    return exercises
}

/**
 * @author Oskar Wiiala
 * @param exercises a list of exercises with index
 * @param description the description of an individual exercise
 * @param exerciseIndex the index of an individual exercise
 * @return a list of exercises with index
 * This function handles deleting an individual exercise from an existing list of exercises and returns the new list
 */
fun deleteExercise(
    exercises: MutableList<ExerciseWithIndex>,
    description: String,
    exerciseIndex: Int
): List<ExerciseWithIndex> {
    // removes the selected exercise from the newly created list
    exercises.remove(
        ExerciseWithIndex(
            description = description,
            index = exerciseIndex
        )
    )
    // Sorts the list ascending based on the index of the exercise
    exercises.sortBy { exerciseWithIndex -> exerciseWithIndex.index }
    return exercises
}

data class ExerciseWithIndex(var description: String, val index: Int)

data class WorkoutDate(val day: Int, val month: Int, val year: Int)

data class CustomTimeType(val type: String, val value: Long)
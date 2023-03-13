package com.example.gainsbookxml.utils

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import com.example.gainsbookxml.R
import com.example.gainsbookxml.viewmodels.LogViewModel
import com.example.gainsbookxml.viewmodels.SupportViewModel
import com.google.android.material.button.MaterialButton

fun newYearPopup(supportViewModel: SupportViewModel, context: Context) {
    val TAG = "NewYearPopup"
    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    // sets a custom dialog interface for the popup
    val li = LayoutInflater.from(context)
    val view = li.inflate(R.layout.dialog_text_field, null)

    val userInput = view.findViewById<EditText>(R.id.editText)

    // set some attributes to userInput
    userInput.inputType = InputType.TYPE_CLASS_NUMBER
    userInput.hint = context.getString(R.string.EnterNewYear)

    // get OK/Cancel buttons
    val btnOk = view.findViewById<MaterialButton>(R.id.buttonOk)
    val btnCancel = view.findViewById<MaterialButton>(R.id.buttonCancel)


    builder.setView(view)
    builder.setCancelable(true)

    // Puts the popup to the screen
    val dialog: AlertDialog = builder.create()
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()

    btnOk.setOnClickListener {
        Log.d(TAG, "clicked OK")
        val input = userInput.text.toString().toIntOrNull()
        if (input != null) supportViewModel.insertYear(input)
        dialog.cancel()
    }
    btnCancel.setOnClickListener {
        Log.d(TAG, "clicked Cancel")
        dialog.cancel()
    }
}

fun newExercisePopup(
    supportViewModel: SupportViewModel,
    context: Context,
    type: String,
    description: String,
    exerciseIndex: Int
) {
    val TAG = "NewExercisePopup"

    // exercises
    val exercises = supportViewModel.exercises.value
    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    // sets a custom dialog interface for the popup
    val li = LayoutInflater.from(context)
    val view = li.inflate(R.layout.dialog_text_field, null)

    val dialogTitle = view.findViewById<TextView>(R.id.dialogTitle)
    // Set title
    dialogTitle.text = context.getString(R.string.AddNewExercise2)

    val userInput = view.findViewById<EditText>(R.id.editText)

    // set hint
    if (type == "new") userInput.hint = context.getString(R.string.EnterNewExercise)
    else if (type == "edit") userInput.hint = context.getString(R.string.EditExercise)

    // set the text of the editable text field as the exercise's description
    if (type == "edit") userInput.setText(description, TextView.BufferType.EDITABLE)

    // get OK/Cancel buttons
    val btnOk = view.findViewById<MaterialButton>(R.id.buttonOk)
    val btnCancel = view.findViewById<MaterialButton>(R.id.buttonCancel)

    builder.setView(view)
    builder.setCancelable(true)

    // Puts the popup to the screen
    val dialog: AlertDialog = builder.create()
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()

    btnOk.setOnClickListener {
        Log.d(TAG, "clicked OK")
        // Copies the contents of exercises from the view model to a new list
        var exercisesList =
            mutableListOf(ExerciseWithIndex(description = "fail", index = 0))

        if (type == "new") {
            exercisesList = newExercise(
                exercises = exercises.toMutableList(),
                textFieldState = userInput.text.toString()
            )
        } else if (type == "edit") {
            exercisesList = editExercise(
                exercises = exercises.toMutableList(),
                description = description,
                exerciseIndex = exerciseIndex,
                textFieldState = userInput.text.toString()
            )
        }

        // Finally recreates the exercises in the view model by adding a new list
        // containing all of the exercises
        supportViewModel.addExercises(exercisesList)

        dialog.cancel()
    }
    btnCancel.setOnClickListener {
        Log.d(TAG, "clicked Cancel")
        dialog.cancel()
    }
}

fun pickDatePopup(supportViewModel: SupportViewModel, context: Context) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    // sets a custom dialog interface for the popup
    val li = LayoutInflater.from(context)
    val view = li.inflate(R.layout.dialog_date, null)

    val day = supportViewModel.date.value.day
    val month = supportViewModel.date.value.month - 1
    val year = supportViewModel.date.value.year

    // get date picker
    val datePicker = view.findViewById<DatePicker>(R.id.datePicker)
    datePicker.init(year, month, day, null)

    // get OK/Cancel buttons
    val btnOk = view.findViewById<MaterialButton>(R.id.buttonOk)
    val btnCancel = view.findViewById<MaterialButton>(R.id.buttonCancel)

    builder.setView(view)
    builder.setCancelable(true)

    // Puts the popup to the screen
    val dialog: AlertDialog = builder.create()
    dialog.show()

    btnOk.setOnClickListener {

        val newDate = WorkoutDate(
            day = datePicker.dayOfMonth,
            month = datePicker.month + 1,
            year = datePicker.year
        )
        supportViewModel.setDate(newDate)

        dialog.cancel()
    }
    btnCancel.setOnClickListener {
        dialog.cancel()
    }
}

fun deletePopup(
    supportViewModel: SupportViewModel?,
    logViewModel: LogViewModel? = null,
    workoutId: Int = 0,
    year: Int = 0,
    month: Int = 0,
    context: Context,
    description: String = "",
    exerciseIndex: Int = 0,
    type: String = "exercise",
) {
    val TAG = "deletePopup"
    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    // sets a custom dialog interface for the popup
    val li = LayoutInflater.from(context)
    val view = li.inflate(R.layout.dialog_delete, null)

    val dialogTitle = view.findViewById<TextView>(R.id.dialogTitle)
    if (type == "exercise") dialogTitle.text = context.getString(R.string.DeleteExercise)
    else if (type == "workout") dialogTitle.text = context.getString(R.string.DeleteWorkout)


    // get OK/Cancel buttons
    val btnOk = view.findViewById<MaterialButton>(R.id.buttonOk)
    val btnCancel = view.findViewById<MaterialButton>(R.id.buttonCancel)

    builder.setView(view)
    builder.setCancelable(true)

    // Puts the popup to the screen
    val dialog: AlertDialog = builder.create()
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()

    btnOk.setOnClickListener {

        if (type == "exercise") {
            val exercises = supportViewModel?.exercises?.value
            val exercisesList = deleteExercise(
                exercises = exercises?.toMutableList() ?: emptyList<ExerciseWithIndex>() as MutableList<ExerciseWithIndex>,
                description = description,
                exerciseIndex = exerciseIndex
            )
            supportViewModel?.addExercises(exercisesList)
        } else if (type == "workout") {
            Log.d(TAG, "delete workout, logViewModel: $logViewModel, workoutId: $workoutId, year: $year, month = $month")
            logViewModel?.deleteWorkoutByID(workoutID = workoutId, year = year, month = month)
        }


        dialog.cancel()
    }
    btnCancel.setOnClickListener {
        dialog.cancel()
    }
}
package com.example.gainsbookxml.utils

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.InputType
import android.view.LayoutInflater
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatSpinner
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.gainsbookxml.R
import com.example.gainsbookxml.database.entities.Profile
import com.example.gainsbookxml.viewmodels.*
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

/**
 * Dialog for adding a new year to database. Accepts only integers as input.
 * @param supportViewModel is used to insert new year into database
 * @param context is required to build the AlertDialog and inflate layout
 * @author Oskar Wiiala
 */
fun newYearPopup(supportViewModel: SupportViewModel, context: Context) {
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
    val dialog: AlertDialog = builder.create()

    // Makes the dialog window transparent so that the red rounded corners look proper
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()

    btnOk.setOnClickListener {
        // Only accepts integers as input
        val input = userInput.text.toString().toIntOrNull()
        if (input != null) supportViewModel.insertYear(input)
        dialog.cancel()
    }
    btnCancel.setOnClickListener {
        dialog.cancel()
    }
}

/**
 * Dialog for adding a new variable to database.
 * @param statsViewModel is used to insert new variable into database
 * @param context is required to build the AlertDialog and inflate layout
 * @author Oskar Wiiala
 */
fun newVariablePopup(statsViewModel: StatsViewModel, context: Context) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    // sets a custom dialog interface for the popup
    val li = LayoutInflater.from(context)
    val view = li.inflate(R.layout.dialog_text_field, null)

    val dialogTitle = view.findViewById<TextView>(R.id.dialogTitle)
    // Set title
    dialogTitle.text = context.getString(R.string.AddNewVariable2)

    val userInput = view.findViewById<EditText>(R.id.editText)

    // set some attributes to userInput
    userInput.hint = context.getString(R.string.EnterNewVariable)

    // get OK/Cancel buttons
    val btnOk = view.findViewById<MaterialButton>(R.id.buttonOk)
    val btnCancel = view.findViewById<MaterialButton>(R.id.buttonCancel)

    builder.setView(view)
    builder.setCancelable(true)
    val dialog: AlertDialog = builder.create()

    // Makes the dialog window transparent so that the red rounded corners look proper
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()

    btnOk.setOnClickListener {
        val input = userInput.text.toString()
        statsViewModel.insertVariable(input)
        dialog.cancel()
    }
    btnCancel.setOnClickListener {
        dialog.cancel()
    }
}

/**
 * Dialog for adding a new exercise to the list of exercises in NewWorkoutFragment or EditWorkoutFragment.
 * @param supportViewModel is used to update the list of exercises
 * @param context is required to build the AlertDialog and inflate layout
 * @param type accept only "new" and "edit"
 * @param description the description of the exercise being edited
 * @param exerciseIndex the index of the exercise being edited
 * @author Oskar Wiiala
 */
fun newExercisePopup(
    supportViewModel: SupportViewModel,
    context: Context,
    type: String,
    description: String = "",
    exerciseIndex: Int = 0
) {
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
    val dialog: AlertDialog = builder.create()

    // Makes the dialog window transparent so that the red rounded corners look proper
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()

    btnOk.setOnClickListener {
        // Copies the contents of exercises from the view model to a new list
        var exercisesList =
            mutableListOf(ExerciseWithIndex(description = "fail", index = 0))

        // Updates the exercise list based on type
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
        dialog.cancel()
    }
}

/**
 * Dialog for picking a specific date on a calendar.
 * @param supportViewModel is used to update the date
 * @param context is required to build the AlertDialog and inflate layout
 * @author Oskar Wiiala
 */
fun pickDatePopup(supportViewModel: SupportViewModel, context: Context) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    // sets a custom dialog interface for the popup
    val li = LayoutInflater.from(context)
    val view = li.inflate(R.layout.dialog_date, null)

    val day = supportViewModel.date.value.day
    val month = supportViewModel.date.value.month - 1
    val year = supportViewModel.date.value.year

    val datePicker = view.findViewById<DatePicker>(R.id.datePicker)
    // Gives the datePicker initial day/month/year values
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

/**
 * Dialog for deleting an item. Currently only used to delete an exercise from a list or a workout from the database.
 * @param supportViewModel is used to update the list of exercises
 * @param logViewModel is used to delete a workout from the database
 * @param workoutId id of the workout being deleted
 * @param year year of the workout
 * @param month month of the workout
 * @param context is required to build the AlertDialog and inflate layout
 * @param type accept only "exercise" or "workout"
 * @param description the description of the exercise being deleted
 * @param exerciseIndex the index of the exercise being deleted
 * @author Oskar Wiiala
 */
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
    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    // sets a custom dialog interface for the popup
    val li = LayoutInflater.from(context)
    val view = li.inflate(R.layout.dialog_delete, null)

    // Set title based on type
    val dialogTitle = view.findViewById<TextView>(R.id.dialogTitle)
    if (type == "exercise") dialogTitle.text = context.getString(R.string.DeleteExercise)
    else if (type == "workout") dialogTitle.text = context.getString(R.string.DeleteWorkout)

    // get OK/Cancel buttons
    val btnOk = view.findViewById<MaterialButton>(R.id.buttonOk)
    val btnCancel = view.findViewById<MaterialButton>(R.id.buttonCancel)

    builder.setView(view)
    builder.setCancelable(true)
    val dialog: AlertDialog = builder.create()

    // Makes the dialog window transparent so that the red rounded corners look proper
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()

    btnOk.setOnClickListener {
        // Deletion of exercise or workout
        if (type == "exercise") {
            val exercises = supportViewModel?.exercises?.value
            val exercisesList = deleteExercise(
                exercises = exercises?.toMutableList()
                    ?: emptyList<ExerciseWithIndex>() as MutableList<ExerciseWithIndex>,
                description = description,
                exerciseIndex = exerciseIndex
            )
            supportViewModel?.addExercises(exercisesList)
        } else if (type == "workout") {
            logViewModel?.deleteWorkoutByID(workoutID = workoutId, year = year, month = month)
        }
        dialog.cancel()
    }
    btnCancel.setOnClickListener {
        dialog.cancel()
    }
}

/**
 * Dialog for picking a specific time from a dropdown between 10 seconds and 15 minutes. Used in TimerFragment.
 * @param timerViewModel is used to set the time and visibility of count down timer
 * @param context is required to build the AlertDialog and inflate layout
 * @param lifecycleScope is required for timeSpinner
 * @author Oskar Wiiala
 */
fun timerPopup(
    timerViewModel: TimerViewModel,
    context: Context,
    lifecycleScope: LifecycleCoroutineScope
) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    // sets a custom dialog interface for the popup
    val li = LayoutInflater.from(context)
    val view = li.inflate(R.layout.dialog_timer, null)

    val spinner = view.findViewById<AppCompatSpinner>(R.id.time_spinner)

    // Set up the spinner for selecting times
    timeSpinner(
        spinner = spinner,
        mainViewModel = timerViewModel,
        context = context,
        lifecycleScope = lifecycleScope
    )

    // get OK/Cancel buttons
    val btnOk = view.findViewById<MaterialButton>(R.id.buttonOk)
    val btnCancel = view.findViewById<MaterialButton>(R.id.buttonCancel)

    builder.setView(view)
    builder.setCancelable(true)
    val dialog: AlertDialog = builder.create()

    // Makes the dialog window transparent so that the red rounded corners look proper
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()

    btnOk.setOnClickListener {
        timerViewModel.startTimer(
            type = "CountDown",
            time = timerViewModel.customTimeType.value.value
        )
        timerViewModel.setVisibility("CountDown", true)
        dialog.cancel()
    }
    btnCancel.setOnClickListener {
        dialog.cancel()
    }
}

/**
 * Dialog for editing profile info. Includes a photo, username and user description.
 * @param profileViewModel is used to update profile info and profile photo in the dialog
 * @param context is required to build the AlertDialog and inflate layout
 * @param singlePhotoPickerLauncher used to launch photo picker
 * @param lifecycleScope is required for collecting preview photo from view model
 * @author Oskar Wiiala
 */
fun editProfilePopup(
    profileViewModel: ProfileViewModel,
    context: Context,
    singlePhotoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
    lifecycleScope: LifecycleCoroutineScope
) {
    var imageUriString = ""

    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    // sets a custom dialog interface for the popup
    val li = LayoutInflater.from(context)
    val view = li.inflate(R.layout.dialog_profile, null)

    val profilePicture = view.findViewById<ImageView>(R.id.image)
    val nameEditText = view.findViewById<EditText>(R.id.editName)
    val descriptionEditText = view.findViewById<EditText>(R.id.editDescription)

    // Listens for changes in the view model and updates preview photo based on changes
    lifecycleScope.launch {
        profileViewModel.profilePictureTemp.collect {
            profilePicture.setImageURI(Uri.parse(it))

            // Is used to used to update real profile photo
            imageUriString = it
        }
    }

    // Initially set the ediText from the view model
    nameEditText.setText(profileViewModel.profile.value.firstOrNull()?.username ?: "null")
    descriptionEditText.setText(profileViewModel.profile.value.firstOrNull()?.description ?: "null")

    // Launch photo picker when clicking on preview photo
    profilePicture.setOnClickListener {
        singlePhotoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    // get OK/Cancel buttons
    val btnOk = view.findViewById<MaterialButton>(R.id.buttonOk)
    val btnCancel = view.findViewById<MaterialButton>(R.id.buttonCancel)

    builder.setView(view)
    builder.setCancelable(true)
    val dialog: AlertDialog = builder.create()

    // Makes the dialog window transparent so that the red rounded corners look proper
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()

    btnOk.setOnClickListener {
        val profile = Profile(
            userID = 0,
            username = nameEditText.text.toString(),
            description = descriptionEditText.text.toString()
        )

        // Update profile info and photo
        profileViewModel.setProfile(profile)
        profileViewModel.setProfilePicture(picture = imageUriString)
        dialog.cancel()
    }
    btnCancel.setOnClickListener {
        dialog.cancel()
    }
}

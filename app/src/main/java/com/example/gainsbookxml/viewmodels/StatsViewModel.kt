package com.example.gainsbookxml.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gainsbookxml.database.AppDatabase
import com.example.gainsbookxml.database.entities.Statistic
import com.example.gainsbookxml.database.entities.Variable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

/**
 * @author Oskar Wiiala
 * @param context
 * View model for StatsScreen and NewStatisticScreen
 * Handles fetching variables and statistics from database
 * Also handles adding a new variable to database
 */
class StatsViewModel(context: Context) : ViewModel() {
    private val dao = AppDatabase.getInstance(context).appDao

    private val _variables = MutableStateFlow(listOf<Variable>())
    val variables: StateFlow<List<Variable>> get() = _variables

    private val _variable = MutableStateFlow(Variable(0, "default"))
    val variable: StateFlow<Variable> get() = _variable

    private val types = listOf("10rm", "5rm", "1rm")

    private val _type = MutableStateFlow(types.first())
    val type: StateFlow<String> get() = _type

    private val _statistics = MutableStateFlow(listOf<Statistic>())
    val statistics: StateFlow<List<Statistic>> get() = _statistics

    private val _newValue = MutableStateFlow(0.0)
    val newValue: StateFlow<Double> get() = _newValue

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getVariables()
            if (variables.value.isEmpty()) {
                insertVariable("Bench press")
                insertVariable("Squat")
                insertVariable("Deadlift")
                insertVariable("Overhead press")
                insertVariable("Chin up")
                insertVariable("Seal row")
                getVariables()
                _variable.emit(variables.value.first())
                getStatisticsBySelection(
                    variableID = variable.value.variableID,
                    type = type.value,
                    month = Calendar.getInstance().get(Calendar.MONTH) + 1,
                    year = Calendar.getInstance().get(Calendar.YEAR)
                )
            } else {
                _variable.emit(variables.value.first())
                getStatisticsBySelection(
                    variableID = variable.value.variableID,
                    type = type.value,
                    month = Calendar.getInstance().get(Calendar.MONTH) + 1,
                    year = Calendar.getInstance().get(Calendar.YEAR)
                )
            }
        }
    }

    private suspend fun getVariables() {
        val response = dao.getVariables()
        _variables.emit(response)
    }

    suspend fun changeVariable(variable: Variable) {
        _variable.emit(variable)
    }

    fun getStatisticsBySelection(variableID: Int, type: String, month: Int, year: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = dao.getStatisticsBySelection(
                variableID = variableID,
                type = type,
                month = month,
                year = year
            )
            _statistics.emit(response)
        }
    }

    // Inserts a new variable to database and updates _variables
    fun insertVariable(variableName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val variable = Variable(
                0,
                variableName = variableName
            )
            dao.insertVariable(variable = variable)
            getVariables()
        }
    }

    // Handles setting a new value for editable text field in NewStatisticScreen
    fun setNewValue(value: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            _newValue.emit(value)
        }
    }

    suspend fun changeType(type: String) {
        _type.emit(type)
    }

    // Inserts a new statistic to database
    fun insertStatistic(
        variableName: String,
        type: String,
        value: Double,
        year: Int,
        month: Int,
        day: Int,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val identifier = dao.getVariableIDByName(variableName = variableName)
            val statistic = Statistic(
                statisticID = 0,
                variableID = identifier,
                type = type,
                value = value,
                year = year,
                month = month,
                day = day
            )
            dao.insertStatistic(statistic = statistic)
        }
    }
}

inline fun <VM : ViewModel> statsViewModelFactory(crossinline f: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = f() as T
    }
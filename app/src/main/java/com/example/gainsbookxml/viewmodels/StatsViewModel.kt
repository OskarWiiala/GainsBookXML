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

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getVariables()
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
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
                    month = currentMonth,
                    year = currentYear
                )
            } else {
                _variable.emit(variables.value.first())
                getStatisticsBySelection(
                    variableID = variable.value.variableID,
                    type = type.value,
                    month = currentMonth,
                    year = currentYear
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
        }
    }

    suspend fun changeType(type: String) {
        _type.emit(type)
    }

    fun getTypes(): List<String> {
        return types
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

class StatsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatsViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
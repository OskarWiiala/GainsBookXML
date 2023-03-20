package com.example.gainsbookxml.viewmodels

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gainsbookxml.utils.CustomTimeType
import com.example.gainsbookxml.utils.TimerProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.security.InvalidParameterException

class TimerViewModel(private val timerProgress: TimerProgress) : ViewModel() {
    val TAG = "TimerViewModel"

    lateinit var timer: CountDownTimer

    val customTimeTypes = listOf(
        CustomTimeType(type = "10s", value = 10L),
        CustomTimeType(type = "1 min", value = 60L),
        CustomTimeType(type = "2 min", value = 120L),
        CustomTimeType(type = "3 min", value = 180L),
        CustomTimeType(type = "4 min", value = 240L),
        CustomTimeType(type = "5 min", value = 300L),
        CustomTimeType(type = "10 min", value = 600L),
        CustomTimeType(type = "15 min", value = 900L),
    )

    private val _customTimeType =
        MutableStateFlow(CustomTimeType(type = "1 min", value = 60L))
    val customTimeType: StateFlow<CustomTimeType> get() = _customTimeType

    private val _isCountDownRunning = MutableStateFlow(false)
    val isCountDownRunning: StateFlow<Boolean> get() = _isCountDownRunning

    private val _isCountUpRunning = MutableStateFlow(false)
    val isCountUpRunning: StateFlow<Boolean> get() = _isCountUpRunning

    private val _secondsRemaining = MutableStateFlow(customTimeType.value.value)
    val secondsRemaining: StateFlow<Long> get() = _secondsRemaining

    private val _countUpSeconds = MutableStateFlow(0L)
    val countUpSeconds: StateFlow<Long> get() = _countUpSeconds

    private val _isCountDownVisible = MutableStateFlow(false)
    val isCountDownVisible: StateFlow<Boolean> get() = _isCountDownVisible

    private val _isCountUpVisible = MutableStateFlow(false)
    val isCountUpVisible: StateFlow<Boolean> get() = _isCountUpVisible

    private val _isTimerPaused = MutableStateFlow(false)
    val isTimerPaused: StateFlow<Boolean> get() = _isTimerPaused

    private val _timerType = MutableStateFlow("None")
    val timerType: StateFlow<String> get() = _timerType

    fun setVisibility(element: String, value: Boolean) {
        viewModelScope.launch {
            when (element) {
                "CountDown" -> {
                    _isCountDownVisible.emit(value)
                }
                "CountUp" -> {
                    _isCountUpVisible.emit(value)

                }
                else -> throw InvalidParameterException("Unknown element: $element. Only elements CountDown and CountUp are allowed")
            }
        }
    }

    private suspend fun setTimerType(type: String) {
        if (type != "CountDown" && type != "CountUp" && type != "None") throw InvalidParameterException()
        _timerType.emit(type)
    }

    suspend fun setIsTimerPaused(value: Boolean) {
        _isTimerPaused.emit(value)
    }

    fun startTimer(type: String, time: Long = 60) {
        viewModelScope.launch {
            setTimerType(type = type)
            when (timerType.value) {
                "CountDown" -> {
                    setCountDownRunning(true)
                    timer = object : CountDownTimer(time * 1000, 1000) {
                        override fun onFinish() {
                            setCountDownRunning(false)
                            timer.cancel()
                        }

                        override fun onTick(p0: Long) {
                            viewModelScope.launch {
                                _secondsRemaining.emit(p0 / 1000)
                                // percentage of how much time is left
                                val newValue =
                                    (((p0 / 1000).toFloat() / customTimeType.value.value) * 100).toInt()
                                timerProgress.newProgressBarValue(newValue = newValue)
                            }
                        }
                    }.start()
                }
                "CountUp" -> {
                    setCountUpRunning(true)
                    delay(10L)
                    while (isCountUpRunning.value) {
                        delay(1000L)
                        viewModelScope.launch {
                            val newValue = countUpSeconds.value + 1L
                            _countUpSeconds.emit(newValue)
                            timerProgress.newCountUpValue(newValue = newValue)
                        }
                    }
                }
                else -> throw InvalidParameterException()
            }
        }
    }

    suspend fun resetCountUpSeconds() {
        _countUpSeconds.emit(0L)
    }

    fun setCustomTimeType(customTimeType: CustomTimeType) {
        viewModelScope.launch(Dispatchers.IO) {
            _customTimeType.emit(customTimeType)
        }
    }

    fun setCountDownRunning(value: Boolean) {
        viewModelScope.launch {
            _isCountDownRunning.emit(value)
        }
    }

    suspend fun setCountUpRunning(value: Boolean) {
        viewModelScope.launch {
            _isCountUpRunning.emit(value)
        }
    }
}

class TimerViewModelFactory(private val timerProgress: TimerProgress) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimerViewModel(timerProgress) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
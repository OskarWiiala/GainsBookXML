package com.example.gainsbookxml.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gainsbookxml.CustomTimeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {
    private val _customTimeType =
        MutableStateFlow(CustomTimeType(type = "1 min", value = 60L))
    val customTimeType: StateFlow<CustomTimeType> get() = _customTimeType

    private val _totalTime = MutableStateFlow(customTimeType.value.value)
    val totalTime: StateFlow<Long> get() = _totalTime

    private val _isCountDownVisible = MutableStateFlow(false)
    val isCountDownVisible: StateFlow<Boolean> get() = _isCountDownVisible

    private val _isCountUpVisible = MutableStateFlow(false)
    val isCountUpVisible: StateFlow<Boolean> get() = _isCountUpVisible

    private val _isButtonCountDownVisible = MutableStateFlow(true)
    val isButtonCountDownVisible: StateFlow<Boolean> get() = _isButtonCountDownVisible

    private val _isButtonCountUpVisible = MutableStateFlow(true)
    val isButtonCountUpVisible: StateFlow<Boolean> get() = _isButtonCountUpVisible

    fun setVisibility(element: String, value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            when (element) {
                "CountDown" -> _isCountDownVisible.emit(value)
                "CountUp" -> _isCountUpVisible.emit(value)
                "ButtonCountDown" -> _isButtonCountDownVisible.emit(value)
                "ButtonCountUp" -> _isButtonCountUpVisible.emit(value)
                else -> Log.d("setVisibility", "element: $element not supported")
            }
        }
    }

    fun setCustomTimeType(customTimeType: CustomTimeType) {
        viewModelScope.launch(Dispatchers.IO) {
            _customTimeType.emit(customTimeType)
        }
    }

    fun setTotalTime(totalTime: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _totalTime.emit(totalTime)
        }
    }
}
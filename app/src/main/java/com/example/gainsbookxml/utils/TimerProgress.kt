package com.example.gainsbookxml.utils

interface TimerProgress {
    fun newProgressBarValue(newValue: Int)
    fun newCountUpValue(newValue: Long)
}
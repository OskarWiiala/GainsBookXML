package com.example.gainsbookxml.utils

/**
 * Handles setting a new value for the timer progress bar and new timer value for count up timer
 */
interface TimerProgress {
    fun newProgressBarValue(newValue: Int)
    fun newCountUpValue(newValue: Long)
}
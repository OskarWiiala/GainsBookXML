package com.example.gainsbookxml.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.gainsbookxml.databinding.FragmentTimerBinding
import com.example.gainsbookxml.utils.TimerProgress
import com.example.gainsbookxml.utils.timerPopup
import com.example.gainsbookxml.viewmodels.*
import kotlinx.coroutines.launch

/**
 * This fragment is used to display a count down and count up timer
 * The user can also pause and restart the timers
 * Count down has a fancy red circular progress bar
 */
class TimerFragment : Fragment(), TimerProgress {
    private lateinit var binding: FragmentTimerBinding

    private val timerViewModel: TimerViewModel by viewModels {
        TimerViewModelFactory(timerProgress = this@TimerFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTimerBinding.inflate(layoutInflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        // pause/resume buttons use the same element, only the text changes, so initially set as pause
        binding.pauseResume = "Pause"

        // Change the displayed time whenever secondsRemaining changes
        // secondsRemaining is only used with count down
        lifecycleScope.launch {
            timerViewModel.secondsRemaining.collect {
                binding.time = "${(it / 60)} m ${(it % 60)} s"
            }
        }

        // Whether or not timers are active are dependent if they are visible or not
        lifecycleScope.launch {
            timerViewModel.isCountDownVisible.collect {
                if (it) {
                    timerViewModel.setVisibility("CountUp", false)
                    binding.startLayout.visibility = View.GONE
                    binding.timerLayout.visibility = View.VISIBLE
                    binding.countDown.visibility = View.VISIBLE
                    initProgressBar()
                }
            }
        }

        // Whether or not timers are active are dependent if they are visible or not
        lifecycleScope.launch {
            timerViewModel.isCountUpVisible.collect {
                if (it) {
                    timerViewModel.setVisibility("CountDown", false)
                    binding.startLayout.visibility = View.GONE
                    binding.timerLayout.visibility = View.VISIBLE
                    binding.countDown.visibility = View.GONE
                }
            }
        }

        binding.countDownButton.setOnClickListener {
            // Popup where you can select a count down time between 10 seconds and 15 minutes
            timerPopup(
                timerViewModel = timerViewModel,
                context = requireContext(),
                lifecycleScope = lifecycleScope
            )
        }

        binding.countUpButton.setOnClickListener {
            timerViewModel.setVisibility("CountUp", true)
            timerViewModel.startTimer(type = "CountUp")
        }

        binding.stopButton.setOnClickListener {
            lifecycleScope.launch {
                handleStop()
            }
        }

        binding.pauseButton.setOnClickListener {
            lifecycleScope.launch {
                handlePauseOrResume()
            }
        }

        binding.restartButton.setOnClickListener {
            lifecycleScope.launch {
                handleRestart()
            }
        }
    }

    /**
     * Sets the initial value of the red circular progress bar, which starts at 100 % full
     */
    private fun initProgressBar() {
        binding.countDown.progress = 100
    }

    /**
     * Handles stopping the timer and setting the timer visibility to gone
     */
    private suspend fun handleStop() {
        timerViewModel.setIsTimerPaused(value = false)
        binding.pauseResume = "Pause"
        timerViewModel.setVisibility("CountDown", false)
        timerViewModel.setVisibility("CountUp", false)
        binding.countDown.visibility = View.GONE
        binding.timerLayout.visibility = View.GONE
        binding.startLayout.visibility = View.VISIBLE

        val timerType = timerViewModel.timerType.value
        if (timerType == "CountDown") {
            timerViewModel.timer.cancel()
        } else if (timerType == "CountUp") {
            timerViewModel.setCountUpRunning(value = false)
            timerViewModel.resetCountUpSeconds()
        }
    }

    /**
     * Pauses or resumes the timer. Also changes the text of pauseResume button.
     */
    private suspend fun handlePauseOrResume() {
        timerViewModel.setIsTimerPaused(value = !timerViewModel.isTimerPaused.value)
        val isPaused = timerViewModel.isTimerPaused.value
        val timerType = timerViewModel.timerType.value
        if (timerType == "CountDown") {
            if (isPaused) {
                timerViewModel.setIsTimerPaused(value = true)
                timerViewModel.timer.cancel()
                timerViewModel.setCountDownRunning(false)
                binding.pauseResume = "Resume"
            }
            if (!isPaused) {
                timerViewModel.setIsTimerPaused(value = false)
                timerViewModel.startTimer(
                    type = "CountDown",
                    time = timerViewModel.secondsRemaining.value
                )
                timerViewModel.setCountDownRunning(true)
                binding.pauseResume = "Pause"
            }

        } else if (timerType == "CountUp") {
            if (isPaused) {
                timerViewModel.setIsTimerPaused(value = true)
                timerViewModel.setCountUpRunning(false)
                binding.pauseResume = "Resume"
            }
            if (!isPaused) {
                timerViewModel.setIsTimerPaused(value = false)
                timerViewModel.startTimer(
                    type = "CountUp",
                )
                timerViewModel.setCountUpRunning(true)
                binding.pauseResume = "Pause"
            }
        }
    }

    /**
     * Handles the restarting of the timer
     */
    private suspend fun handleRestart() {
        binding.pauseResume = "Pause"
        val timerType = timerViewModel.timerType.value
        timerViewModel.setIsTimerPaused(value = false)

        if (timerType == "CountDown") {
            timerViewModel.timer.cancel()
            val time = timerViewModel.customTimeType.value.value
            timerViewModel.startTimer(type = timerType, time = time)
        } else if (timerType == "CountUp") {
            timerViewModel.resetCountUpSeconds()
        }
    }

    /**
     * Gives the red circular progress bar a new value. Happens every second.
     * @param newValue the new value, should be between 0 and 100
     */
    override fun newProgressBarValue(newValue: Int) {
        binding.countDown.progress = newValue
    }

    /**
     * Updates the displayed time when count up is running. Happens every second
     * @param newValue value in seconds.
     */
    override fun newCountUpValue(newValue: Long) {
        binding.time = "${(newValue / 60)} m ${(newValue % 60)} s"
    }
}
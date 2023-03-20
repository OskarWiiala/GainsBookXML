package com.example.gainsbookxml.fragments

import android.os.Bundle
import android.util.Log
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

class TimerFragment : Fragment(), TimerProgress {
    val TAG = "TimerFragment"

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
        binding.pauseResume = "Pause"

        lifecycleScope.launch {
            timerViewModel.secondsRemaining.collect {
                binding.time = "${(it / 60)} m ${(it % 60)} s"
            }
        }

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

    private fun initProgressBar() {
        binding.countDown.progress = 100
    }

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

    override fun newProgressBarValue(newValue: Int) {
        Log.d("newProgressBarValue", "value: $newValue")
        binding.countDown.progress = newValue
    }

    override fun newCountUpValue(newValue: Long) {
        binding.time = "${(newValue / 60)} m ${(newValue % 60)} s"
    }
}
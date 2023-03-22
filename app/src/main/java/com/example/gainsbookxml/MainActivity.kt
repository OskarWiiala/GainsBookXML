package com.example.gainsbookxml

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.gainsbookxml.databinding.ActivityMainBinding
import com.example.gainsbookxml.utils.CustomTypefaceSpan

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_GainsBookXML)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        installSplashScreen().apply {
            // Do optional operations here before app loads
        }
        setContentView(binding.root)
        customTitle()
        initNavigation()
    }

    private fun customTitle() {
        val typeface = ResourcesCompat.getFont(applicationContext, R.font.medievalsharp_regular)
        val text = "GainsBook"
        val ss = SpannableString(text)
        val customSpan1 = CustomTypefaceSpan("", typeface!!)
        val customSpan2 = CustomTypefaceSpan("", typeface)
        ss.setSpan(customSpan1, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(customSpan2, 5, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.topAppBarText.text = ss
    }

    private fun initNavigation() {
        val navHost =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        navController = navHost.navController

        val navView = binding.bottomNavigation
        navView.setupWithNavController(navController)
        navView.setOnItemSelectedListener { item ->
            NavigationUI.onNavDestinationSelected(item, navController)
            true
        }

        navController.addOnDestinationChangedListener(onDestChangedListener)
    }

    private val onDestChangedListener = NavController.OnDestinationChangedListener { _, d, _ ->
        when (d.id) {
            R.id.logFragment -> binding.bottomNavigation.visibility = View.VISIBLE
        }
    }
}
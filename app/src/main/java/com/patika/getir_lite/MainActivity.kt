package com.patika.getir_lite

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    /**
     * ViewModel providing data and functionality to the UI. It is lazily initialized
     * the first time it is used.
     */
    private val productViewModel: ProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                getColor(R.color.md_theme_light_primary),
                getColor(R.color.md_theme_light_surface),
            )
        )

        productViewModel.initializeProductData()

        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            false

        handleInsets()
    }

    /**
     * Handles the application of window insets to adjust the padding of the main view,
     * ensuring it does not overlap with system bars.
     */
    private fun handleInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

package com.wildwalls.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.wildwalls.app.ui.navigation.WildWallsRoot
import com.wildwalls.app.ui.theme.WildWallsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val container = (application as WildWallsApp).container
        setContent {
            WildWallsTheme {
                WildWallsRoot(container = container)
            }
        }
    }
}

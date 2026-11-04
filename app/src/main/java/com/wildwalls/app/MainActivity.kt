package com.wildwalls.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import com.wildwalls.app.ui.components.LocalWildImageLoader
import com.wildwalls.app.ui.navigation.WildWallsRoot
import com.wildwalls.app.ui.theme.WildWallsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val container = (application as WildWallsApp).container
        setContent {
            // Every AsyncImage in the tree gets the app's own Coil loader, which carries the
            // User-Agent Wikimedia requires. Falling back to the default client returns HTTP 403
            // and leaves every tile blank.
            CompositionLocalProvider(LocalWildImageLoader provides container.imageLoader) {
                WildWallsTheme {
                    WildWallsRoot(container = container)
                }
            }
        }
    }
}

package com.wildwalls.app

import android.app.Application
import coil.Coil
import com.wildwalls.app.di.AppContainer

class WildWallsApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Compose's AsyncImage resolves its loader through the Coil singleton. Without this the
        // default OkHttp client is used, which sends no descriptive User-Agent - and Wikimedia
        // answers those requests with HTTP 403, leaving every image blank.
        Coil.setImageLoader(container.imageLoader)
    }
}

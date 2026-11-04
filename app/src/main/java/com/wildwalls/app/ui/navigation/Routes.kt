package com.wildwalls.app.ui.navigation

object Routes {
    const val HOME = "home"
    const val EXPLORE = "explore?category={category}"
    const val FAVORITES = "favorites"
    const val DOWNLOADS = "downloads"
    const val SETTINGS = "settings"
    const val CREDITS = "credits"
    const val DETAIL = "detail/{wallpaperId}"

    fun explore(categoryId: String? = null): String =
        if (categoryId.isNullOrBlank()) "explore?category=" else "explore?category=$categoryId"

    fun detail(wallpaperId: Long): String = "detail/$wallpaperId"

    val bottomBarRoutes = setOf(
        "explore?category={category}",
        HOME,
        FAVORITES,
        DOWNLOADS,
        SETTINGS
    )
}

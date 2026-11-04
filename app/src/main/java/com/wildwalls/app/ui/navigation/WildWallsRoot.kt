package com.wildwalls.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wildwalls.app.di.AppContainer
import com.wildwalls.app.ui.WildViewModelFactory
import com.wildwalls.app.ui.credits.CreditsScreen
import com.wildwalls.app.ui.credits.CreditsViewModel
import com.wildwalls.app.ui.detail.DetailScreen
import com.wildwalls.app.ui.detail.DetailViewModel
import com.wildwalls.app.ui.downloads.DownloadsScreen
import com.wildwalls.app.ui.downloads.DownloadsViewModel
import com.wildwalls.app.ui.explore.ExploreScreen
import com.wildwalls.app.ui.explore.ExploreViewModel
import com.wildwalls.app.ui.favorites.FavoritesScreen
import com.wildwalls.app.ui.favorites.FavoritesViewModel
import com.wildwalls.app.ui.home.HomeScreen
import com.wildwalls.app.ui.home.HomeViewModel
import com.wildwalls.app.ui.settings.SettingsScreen
import com.wildwalls.app.ui.settings.SettingsViewModel
import com.wildwalls.app.ui.components.WildIcons
import com.wildwalls.app.ui.theme.Accent
import com.wildwalls.app.ui.theme.Black
import com.wildwalls.app.ui.theme.GlassBorder
import com.wildwalls.app.ui.theme.GlassFill
import com.wildwalls.app.ui.theme.TextTertiary

@Composable
fun WildWallsRoot(container: AppContainer) {
    val navController = rememberNavController()
    val factory = remember { WildViewModelFactory(container) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in Routes.bottomBarRoutes

    Scaffold(
        containerColor = Black,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                WildBottomBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) {
            composable(Routes.HOME) {
                val viewModel: HomeViewModel = viewModel(factory = factory)
                HomeScreen(
                    viewModel = viewModel,
                    onOpenWallpaper = { navController.navigate(Routes.detail(it)) },
                    onOpenCategory = { navController.navigate(Routes.explore(it)) },
                    onOpenSearch = { navController.navigate(Routes.explore()) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) }
                )
            }

            composable(
                route = Routes.EXPLORE,
                arguments = listOf(
                    navArgument("category") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { entry ->
                val viewModel: ExploreViewModel = viewModel(factory = factory)
                ExploreScreen(
                    viewModel = viewModel,
                    initialCategory = entry.arguments?.getString("category"),
                    onOpenWallpaper = { navController.navigate(Routes.detail(it)) }
                )
            }

            composable(Routes.FAVORITES) {
                val viewModel: FavoritesViewModel = viewModel(factory = factory)
                FavoritesScreen(
                    viewModel = viewModel,
                    onOpenWallpaper = { navController.navigate(Routes.detail(it)) }
                )
            }

            composable(Routes.DOWNLOADS) {
                val viewModel: DownloadsViewModel = viewModel(factory = factory)
                DownloadsScreen(
                    viewModel = viewModel,
                    onOpenWallpaper = { navController.navigate(Routes.detail(it)) }
                )
            }

            composable(Routes.SETTINGS) {
                val viewModel: SettingsViewModel = viewModel(factory = factory)
                SettingsScreen(
                    viewModel = viewModel,
                    onOpenCredits = { navController.navigate(Routes.CREDITS) }
                )
            }

            composable(Routes.CREDITS) {
                val viewModel: CreditsViewModel = viewModel(factory = factory)
                CreditsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("wallpaperId") { type = NavType.LongType })
            ) { entry ->
                val viewModel: DetailViewModel = viewModel(factory = factory)
                val wallpaperId = entry.arguments?.getLong("wallpaperId") ?: -1L
                DetailScreen(
                    viewModel = viewModel,
                    wallpaperId = wallpaperId,
                    onBack = { navController.popBackStack() },
                    onOpenWallpaper = { navController.navigate(Routes.detail(it)) }
                )
            }
        }
    }
}

private data class BottomItem(
    /** Route pattern used to decide whether this tab is selected. */
    val matchRoute: String,
    /** Concrete route to navigate to (optional args left at their defaults). */
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
private fun WildBottomBar(navController: NavHostController, currentRoute: String?) {
    val items = listOf(
        BottomItem(Routes.HOME, Routes.HOME, "Home", Icons.Outlined.Home),
        BottomItem(Routes.EXPLORE, "explore", "Explore", WildIcons.Compass),
        BottomItem(Routes.FAVORITES, Routes.FAVORITES, "Favorites", Icons.Outlined.FavoriteBorder),
        BottomItem(Routes.DOWNLOADS, Routes.DOWNLOADS, "Downloads", WildIcons.Download),
        BottomItem(Routes.SETTINGS, Routes.SETTINGS, "Settings", Icons.Outlined.Settings)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Black)
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.matchRoute
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selected) GlassFill else Black)
                    .clickable {
                        if (!selected) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = if (selected) Accent else TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
                if (selected) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(width = 14.dp, height = 2.dp)
                            .background(Accent, RoundedCornerShape(1.dp))
                    )
                } else {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = GlassBorder,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

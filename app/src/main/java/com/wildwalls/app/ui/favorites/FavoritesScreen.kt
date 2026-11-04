package com.wildwalls.app.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wildwalls.app.ui.components.EmptyState
import com.wildwalls.app.ui.components.WallpaperCard
import com.wildwalls.app.ui.components.WildWallsWordmark
import com.wildwalls.app.ui.theme.Black
import com.wildwalls.app.ui.theme.TextSecondary

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onOpenWallpaper: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
            WildWallsWordmark(compact = true)
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Favorites",
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = if (state.items.isEmpty()) "Saved wallpapers appear here"
                else "${state.items.size} saved",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        if (state.items.isEmpty() && !state.loading) {
            EmptyState(
                title = "Nothing saved yet",
                message = "Tap the heart on any wallpaper to keep it here. Favorites are stored on this device."
            )
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.items, key = { it.id }) { wallpaper ->
                    WallpaperCard(
                        wallpaper = wallpaper,
                        onClick = { onOpenWallpaper(wallpaper.id) },
                        isFavorite = true,
                        onToggleFavorite = { viewModel.remove(wallpaper) }
                    )
                }
            }
        }
    }
}

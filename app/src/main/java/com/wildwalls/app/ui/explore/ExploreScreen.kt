package com.wildwalls.app.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wildwalls.app.domain.model.CategoryCatalog
import com.wildwalls.app.ui.components.EmptyState
import com.wildwalls.app.ui.components.ErrorState
import com.wildwalls.app.ui.components.ShimmerBox
import com.wildwalls.app.ui.components.SuggestionPanel
import com.wildwalls.app.ui.components.TagPill
import com.wildwalls.app.ui.components.WallpaperCard
import com.wildwalls.app.ui.components.WildSearchBar
import com.wildwalls.app.ui.components.WildWallsWordmark
import com.wildwalls.app.ui.theme.Accent
import com.wildwalls.app.ui.theme.Black
import com.wildwalls.app.ui.theme.GlassFill
import com.wildwalls.app.ui.theme.TextPrimary
import com.wildwalls.app.ui.theme.TextSecondary

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    initialCategory: String?,
    onOpenWallpaper: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gridState = rememberLazyStaggeredGridState()

    LaunchedEffect(initialCategory) {
        if (!initialCategory.isNullOrBlank()) viewModel.openCategory(initialCategory)
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = gridState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 4
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMore()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WildWallsWordmark(compact = true, modifier = Modifier.weight(1f))
        }

        WildSearchBar(
            query = state.query,
            onQueryChange = viewModel::onQueryChange,
            onSubmit = { viewModel.submitSearch(state.query) },
            onFocusChange = viewModel::onSearchFocus,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        if (state.searchActive && (state.suggestions.isNotEmpty() || state.recentSearches.isNotEmpty())) {
            Spacer(Modifier.height(8.dp))
            SuggestionPanel(
                suggestions = state.suggestions,
                recent = state.recentSearches,
                onPick = { picked -> viewModel.submitSearch(picked) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        Spacer(Modifier.height(14.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CategoryCatalog.all) { category ->
                TagPill(
                    text = category.name,
                    highlighted = state.categoryId == category.id,
                    modifier = Modifier.clickable {
                        if (state.categoryId == category.id) viewModel.clearCategory()
                        else viewModel.openCategory(category.id)
                    }
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.activeQuery,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Text(
                    text = if (state.categoryId != null) "Wikimedia Commons category"
                    else "Wikimedia Commons search",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            if (state.categoryId != null) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(GlassFill)
                        .clickable { viewModel.clearCategory() }
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Clear category filter",
                        tint = TextSecondary,
                        modifier = Modifier.height(16.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        when {
            state.loading -> StaggeredPlaceholders()
            state.error != null && state.items.isEmpty() -> ErrorState(
                message = state.error ?: "",
                onRetry = viewModel::retry,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            state.items.isEmpty() -> EmptyState(
                title = "No wallpapers found",
                message = "Try another animal, or pick a category above."
            )
            else -> LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                state = gridState,
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.items, key = { it.id }) { wallpaper ->
                    WallpaperCard(
                        wallpaper = wallpaper,
                        onClick = { onOpenWallpaper(wallpaper.id) }
                    )
                }
                if (state.loadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ShimmerBox(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp),
                                shape = RoundedCornerShape(5.dp)
                            )
                        }
                    }
                }
                if (state.error != null && state.items.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .clickable { viewModel.loadMore() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Could not load more · tap to retry",
                                style = MaterialTheme.typography.bodySmall,
                                color = Accent
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StaggeredPlaceholders() {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        repeat(3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(if (it % 2 == 0) 210.dp else 170.dp)
                )
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(if (it % 2 == 0) 170.dp else 210.dp)
                )
            }
        }
    }
}

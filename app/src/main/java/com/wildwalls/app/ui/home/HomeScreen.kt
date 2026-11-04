package com.wildwalls.app.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wildwalls.app.domain.model.CategoryCatalog
import com.wildwalls.app.domain.model.Wallpaper
import com.wildwalls.app.ui.components.ErrorState
import com.wildwalls.app.ui.components.HeroWallpaperCard
import com.wildwalls.app.ui.components.SectionHeader
import com.wildwalls.app.ui.components.ShimmerBox
import com.wildwalls.app.ui.components.TagPill
import com.wildwalls.app.ui.components.WallpaperCard
import com.wildwalls.app.ui.components.WideWallpaperCard
import com.wildwalls.app.ui.components.WildSearchBar
import com.wildwalls.app.ui.components.WildWallsWordmark
import com.wildwalls.app.ui.components.glass
import com.wildwalls.app.ui.theme.Accent
import com.wildwalls.app.ui.theme.Black
import com.wildwalls.app.ui.theme.GlassBorder
import com.wildwalls.app.ui.theme.TextPrimary
import com.wildwalls.app.ui.theme.TextSecondary
import com.wildwalls.app.ui.theme.TextTertiary

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenWallpaper: (Long) -> Unit,
    onOpenCategory: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Keep the feed live: refresh the newest-first section whenever the app comes back to front.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) viewModel.refreshIfStale()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Black),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            HomeTopBar(onOpenSettings = onOpenSettings)
        }
        item {
            WildSearchBar(
                query = "",
                onQueryChange = {},
                onSubmit = onOpenSearch,
                readOnly = true,
                onClick = onOpenSearch,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        item { Spacer(Modifier.height(22.dp)) }

        // Newest photographs first - the "live" part of the home screen.
        item {
            SectionHeader(
                title = "Fresh from the wild",
                subtitle = "Live · newest uploads first, straight from Wikimedia Commons",
                actionLabel = "Refresh",
                onAction = viewModel::loadFresh
            )
            Spacer(Modifier.height(12.dp))
            when {
                state.freshLoading -> Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(3) {
                        ShimmerBox(
                            modifier = Modifier
                                .width(150.dp)
                                .aspectRatio(0.78f)
                        )
                    }
                }
                state.freshError != null -> ErrorState(
                    message = state.freshError ?: "",
                    onRetry = viewModel::loadFresh
                )
                else -> LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.fresh) { wallpaper ->
                        WideWallpaperCard(
                            wallpaper = wallpaper,
                            onClick = { onOpenWallpaper(wallpaper.id) }
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(26.dp)) }

        item {
            when {
                state.featuredLoading -> ShimmerBox(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .aspectRatio(0.92f),
                    shape = RoundedCornerShape(26.dp)
                )
                state.featuredError != null -> ErrorState(
                    message = state.featuredError ?: "",
                    onRetry = viewModel::loadFeatured
                )
                state.featured.isNotEmpty() -> FeaturedCarousel(
                    items = state.featured,
                    onOpenWallpaper = onOpenWallpaper
                )
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(
                title = "Popular animals",
                subtitle = "Hand-picked categories"
            )
            Spacer(Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CategoryCatalog.popular) { id ->
                    val category = CategoryCatalog.byId(id)
                    if (category != null) {
                        TagPill(
                            text = category.name,
                            modifier = Modifier.clickable { onOpenCategory(id) }
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(
                title = "Trending wallpapers",
                subtitle = "Quality images from Wikimedia Commons",
                actionLabel = "See all",
                onAction = onOpenSearch
            )
            Spacer(Modifier.height(12.dp))
            when {
                state.trendingLoading -> Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(3) {
                        ShimmerBox(
                            modifier = Modifier
                                .width(150.dp)
                                .aspectRatio(0.78f)
                        )
                    }
                }
                state.trendingError != null -> ErrorState(
                    message = state.trendingError ?: "",
                    onRetry = viewModel::loadTrending
                )
                else -> LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.trending) { wallpaper ->
                        WideWallpaperCard(
                            wallpaper = wallpaper,
                            onClick = { onOpenWallpaper(wallpaper.id) }
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(
                title = "Daily wildlife",
                subtitle = "One new frame every day"
            )
            Spacer(Modifier.height(12.dp))
            DailyCard(
                wallpaper = state.daily,
                loading = state.dailyLoading,
                error = state.dailyError,
                onRetry = viewModel::loadDaily,
                onClick = { state.daily?.let { onOpenWallpaper(it.id) } }
            )
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(
                title = "Browse categories",
                subtitle = "24 collections of wildlife photography"
            )
            Spacer(Modifier.height(12.dp))
            CategoryGrid(onOpenCategory = onOpenCategory)
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(
                title = "Recently added",
                subtitle = "Freshest uploads from the archive"
            )
            Spacer(Modifier.height(12.dp))
            when {
                state.recentLoading -> Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(2) {
                        ShimmerBox(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.8f)
                        )
                    }
                }
                state.recentError != null -> ErrorState(
                    message = state.recentError ?: "",
                    onRetry = viewModel::loadRecent
                )
                else -> RecentGrid(
                    items = state.recent.take(4),
                    onOpenWallpaper = onOpenWallpaper
                )
            }
        }

        item {
            Spacer(Modifier.height(30.dp))
            AttributionFooter()
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun HomeTopBar(onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WildWallsWordmark(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .glass(CircleShape)
                .clickable(onClick = onOpenSettings),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = TextSecondary,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeaturedCarousel(items: List<Wallpaper>, onOpenWallpaper: (Long) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { items.size })
    Column {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 12.dp
        ) { page ->
            HeroWallpaperCard(
                wallpaper = items[page],
                onClick = { onOpenWallpaper(items[page].id) }
            )
        }
        if (items.size > 1) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(items.size.coerceAtMost(6)) { index ->
                    val active = index == pagerState.currentPage % 6
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(width = if (active) 18.dp else 6.dp, height = 6.dp)
                            .clip(CircleShape)
                            .background(if (active) Accent else GlassBorder)
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyCard(
    wallpaper: Wallpaper?,
    loading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onClick: () -> Unit
) {
    when {
        loading -> ShimmerBox(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(22.dp)
        )
        error != null -> ErrorState(message = error, onRetry = onRetry)
        wallpaper != null -> Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .clickable(onClick = onClick)
        ) {
            WallpaperCard(
                wallpaper = wallpaper,
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(CircleShape)
                    .background(Color(0x99000000))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "TODAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = Accent
                )
            }
        }
    }
}

@Composable
private fun CategoryGrid(onOpenCategory: (String) -> Unit) {
    val categories = CategoryCatalog.all
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        categories.chunked(3).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { category ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF141419), Color(0xFF0A0A0C))
                                )
                            )
                            .clickable { onOpenCategory(category.id) }
                            .padding(12.dp)
                    ) {
                        Column(modifier = Modifier.align(Alignment.BottomStart)) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = category.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun RecentGrid(items: List<Wallpaper>, onOpenWallpaper: (Long) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        items.chunked(2).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { wallpaper ->
                    WallpaperCard(
                        wallpaper = wallpaper,
                        onClick = { onOpenWallpaper(wallpaper.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AttributionFooter() {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .glass(RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Every photograph in WildWalls comes from Wikimedia Commons under a reusable licence. " +
                "Credits, licence links and the original file page are shown on each wallpaper.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "WildWalls does not own these photographs.",
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary
        )
    }
}

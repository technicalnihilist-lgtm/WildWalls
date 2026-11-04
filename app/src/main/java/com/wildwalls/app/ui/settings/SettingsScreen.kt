package com.wildwalls.app.ui.settings

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wildwalls.app.BuildConfig
import com.wildwalls.app.ui.components.HairlineDivider
import com.wildwalls.app.ui.components.WildIcons
import com.wildwalls.app.ui.components.WildWallsWordmark
import com.wildwalls.app.ui.components.glass
import com.wildwalls.app.ui.theme.Accent
import com.wildwalls.app.ui.theme.Black
import com.wildwalls.app.ui.theme.GlassFill
import com.wildwalls.app.ui.theme.TextPrimary
import com.wildwalls.app.ui.theme.TextSecondary
import com.wildwalls.app.ui.theme.TextTertiary
import com.wildwalls.app.util.ShareUtils
import com.wildwalls.app.util.formatBytes

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onOpenCredits: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.message) {
        val message = state.message
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Black)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    WildWallsWordmark(compact = true)
                    Spacer(Modifier.height(14.dp))
                    Text(text = "Settings", style = MaterialTheme.typography.displayMedium)
                    Text(
                        text = "WildWalls ${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            item {
                SettingsCard(title = "APPEARANCE") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Grid density",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                            Text(
                                "Columns in the wallpaper grid",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary
                            )
                        }
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(GlassFill)
                                .padding(4.dp)
                        ) {
                            listOf(2, 3).forEach { columns ->
                                val selected = state.settings.gridColumns == columns
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(
                                            if (selected) Accent.copy(alpha = 0.2f) else Color.Transparent
                                        )
                                        .clickable { viewModel.setGridColumns(columns) }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "$columns",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) Accent else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                    HairlineDivider()
                    SwitchRow(
                        title = "Data saver",
                        subtitle = "Load the 800px preview instead of the full-resolution file",
                        checked = state.settings.dataSaver,
                        onCheckedChange = viewModel::setDataSaver
                    )
                }
            }

            item {
                SettingsCard(title = "STORAGE") {
                    ActionRow(
                        icon = Icons.Outlined.Delete,
                        title = "Clear image cache",
                        subtitle = formatBytes(state.cacheBytes),
                        onClick = viewModel::clearCache
                    )
                    HairlineDivider()
                    ActionRow(
                        icon = Icons.Outlined.Delete,
                        title = "Clear search history",
                        subtitle = "Removes recent searches and recently viewed",
                        onClick = viewModel::clearSearchHistory
                    )
                }
            }

            item {
                SettingsCard(title = "ABOUT") {
                    ActionRow(
                        icon = WildIcons.Doc,
                        title = "Credits and licensing",
                        subtitle = "Every photographer, licence and source",
                        onClick = onOpenCredits
                    )
                    HairlineDivider()
                    ActionRow(
                        icon = WildIcons.External,
                        title = "Wikimedia Commons",
                        subtitle = "commons.wikimedia.org",
                        onClick = { ShareUtils.openUrl(context, "https://commons.wikimedia.org/") }
                    )
                    HairlineDivider()
                    ActionRow(
                        icon = WildIcons.External,
                        title = "Licence terms",
                        subtitle = "creativecommons.org/licenses",
                        onClick = {
                            ShareUtils.openUrl(context, "https://creativecommons.org/licenses/")
                        }
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = "WildWalls is a browser for freely licensed wildlife photography. " +
                            "Photographs remain the property of their photographers and are used " +
                            "under the licence shown on each image. WildWalls claims no ownership " +
                            "of any photograph.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glass(RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Accent,
                checkedTrackColor = Accent.copy(alpha = 0.3f),
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = GlassFill
            )
        )
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
        }
        Icon(
            imageVector = Icons.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(18.dp)
        )
    }
}

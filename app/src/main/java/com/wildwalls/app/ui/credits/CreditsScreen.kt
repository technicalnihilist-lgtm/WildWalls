package com.wildwalls.app.ui.credits

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wildwalls.app.ui.components.AttributionRow
import com.wildwalls.app.ui.components.HairlineDivider
import com.wildwalls.app.ui.components.PawMark
import com.wildwalls.app.ui.components.glass
import com.wildwalls.app.ui.theme.Accent
import com.wildwalls.app.ui.theme.Black
import com.wildwalls.app.ui.theme.TextPrimary
import com.wildwalls.app.ui.theme.TextSecondary
import com.wildwalls.app.ui.theme.TextTertiary
import com.wildwalls.app.util.ShareUtils

@Composable
fun CreditsScreen(
    viewModel: CreditsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Black),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Credits",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                PawMark(size = 28.dp)
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Photographs and licensing",
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "WildWalls does not own any of the photographs shown in the app. Every " +
                        "image is served directly from Wikimedia Commons and is displayed with its " +
                        "original title, photographer, licence and a link to the file page.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Only images under CC0, Public Domain, CC BY or CC BY-SA are shown. " +
                        "Non-free, fair use, NoDerivatives and NonCommercial material is filtered " +
                        "out before it ever reaches the screen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .glass(RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Text(
                    text = "SOURCES",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
                Spacer(Modifier.height(8.dp))
                AttributionRow(
                    label = "Photographs",
                    value = "Wikimedia Commons",
                    onClick = { ShareUtils.openUrl(context, "https://commons.wikimedia.org/") }
                )
                AttributionRow(
                    label = "API",
                    value = "commons.wikimedia.org/w/api.php",
                    onClick = {
                        ShareUtils.openUrl(context, "https://commons.wikimedia.org/w/api.php")
                    }
                )
                AttributionRow(
                    label = "Encyclopaedia",
                    value = "Wikipedia",
                    onClick = { ShareUtils.openUrl(context, "https://www.wikipedia.org/") }
                )
                AttributionRow(
                    label = "Licences",
                    value = "Creative Commons",
                    onClick = { ShareUtils.openUrl(context, "https://creativecommons.org/licenses/") }
                )
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "IMAGES IN YOUR LIBRARY",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(8.dp))
        }

        if (state.credited.isEmpty()) {
            item {
                Text(
                    text = "Favourite or download a wallpaper and its full credit will be listed here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        } else {
            items(state.credited, key = { it.id }) { wallpaper ->
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 10.dp)
                        .fillMaxWidth()
                        .glass(RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = wallpaper.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${wallpaper.artist} · ${wallpaper.licenseShort}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Original page",
                            style = MaterialTheme.typography.labelLarge,
                            color = Accent,
                            modifier = Modifier.clickable {
                                ShareUtils.openUrl(context, wallpaper.pageUrl)
                            }
                        )
                        Text(
                            text = "Licence",
                            style = MaterialTheme.typography.labelLarge,
                            color = Accent,
                            modifier = Modifier.clickable {
                                ShareUtils.openUrl(context, wallpaper.licenseUrl)
                            }
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
            HairlineDivider(modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Text content is available under the Creative Commons Attribution-ShareAlike " +
                    "Licence; image licensing is described on each file page at Wikimedia Commons.",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

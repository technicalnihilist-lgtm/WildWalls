package com.wildwalls.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wildwalls.app.ui.theme.Accent
import com.wildwalls.app.ui.theme.GlassBorder
import com.wildwalls.app.ui.theme.GlassFill
import com.wildwalls.app.ui.theme.TextPrimary
import com.wildwalls.app.ui.theme.TextSecondary
import com.wildwalls.app.ui.theme.TextTertiary

/** Frosted panel used across the app: translucent fill plus a hairline border. */
fun Modifier.glass(
    shape: RoundedCornerShape = RoundedCornerShape(22.dp),
    fill: Color = GlassFill,
    borderColor: Color = GlassBorder
): Modifier = this
    .clip(shape)
    .background(Brush.verticalGradient(listOf(fill, fill.copy(alpha = fill.alpha * 0.6f))), shape)
    .border(1.dp, borderColor, shape)

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = Accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun TagPill(
    text: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (highlighted) Accent.copy(alpha = 0.16f) else GlassFill)
            .border(1.dp, if (highlighted) Accent.copy(alpha = 0.55f) else GlassBorder, CircleShape)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (highlighted) Accent else TextSecondary
        )
    }
}

/** Letter-spaced wordmark with a small paw mark. */
@Composable
fun WildWallsWordmark(modifier: Modifier = Modifier, compact: Boolean = false) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        PawMark(size = if (compact) 22.dp else 26.dp)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = "WILDWALLS",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = if (compact) 17.sp else 20.sp,
                letterSpacing = 3.2.sp,
                color = TextPrimary
            )
            if (!compact) {
                Text(
                    text = "WILDLIFE, FRAMED",
                    style = MaterialTheme.typography.labelSmall,
                    color = Accent,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

/** Minimal geometric paw, drawn without any bitmap assets. */
@Composable
fun PawMark(size: androidx.compose.ui.unit.Dp = 26.dp, tint: Color = Accent) {
    Box(modifier = Modifier.size(size)) {
        val dot = size * 0.26f
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .size(width = size * 0.62f, height = size * 0.5f)
                .clip(CircleShape)
                .background(tint)
        )
        Box(
            Modifier
                .align(Alignment.TopStart)
                .padding(start = size * 0.16f, top = size * 0.10f)
                .size(dot)
                .clip(CircleShape)
                .background(tint)
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(end = size * 0.16f, top = size * 0.10f)
                .size(dot)
                .clip(CircleShape)
                .background(tint)
        )
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .size(dot * 0.9f)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.85f))
        )
    }
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    Box(
        modifier = modifier
            .clip(shape)
            .alpha(alpha)
            .background(Color(0xFF141418), shape)
    )
}

@Composable
fun LoadingState(modifier: Modifier = Modifier, label: String = "Loading wallpapers") {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShimmerBox(modifier = Modifier.size(width = 64.dp, height = 6.dp), shape = RoundedCornerShape(3.dp))
        Spacer(Modifier.height(14.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
    }
}

@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .glass(RoundedCornerShape(22.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        if (onRetry != null) {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, Accent.copy(alpha = 0.6f), CircleShape)
                    .clickable(onClick = onRetry)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Retry", style = MaterialTheme.typography.labelLarge, color = Accent)
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PawMark(size = 34.dp, tint = TextTertiary)
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextTertiary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HairlineDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(GlassBorder)
    )
}

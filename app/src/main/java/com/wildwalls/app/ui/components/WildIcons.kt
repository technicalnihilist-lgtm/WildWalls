package com.wildwalls.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * The handful of glyphs WildWalls needs that are not in material-icons-core.
 * Drawn as vectors so the app ships without the 40 MB extended icon pack.
 */
object WildIcons {

    private fun build(name: String, block: ImageVector.Builder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply(block).build()

    private fun ImageVector.Builder.stroke(block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit) {
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.7f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = block
        )
    }

    private fun ImageVector.Builder.solid(block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit) {
        path(fill = SolidColor(Color.White), pathBuilder = block)
    }

    val Compass: ImageVector by lazy {
        build("Compass") {
            stroke {
                moveTo(21f, 12f)
                arcTo(9f, 9f, 0f, true, true, 3f, 12f)
                arcTo(9f, 9f, 0f, true, true, 21f, 12f)
                close()
            }
            solid {
                moveTo(15.6f, 8.4f)
                lineTo(13.5f, 13.5f)
                lineTo(8.4f, 15.6f)
                lineTo(10.5f, 10.5f)
                close()
            }
        }
    }

    val Download: ImageVector by lazy {
        build("Download") {
            stroke {
                moveTo(12f, 3.5f)
                lineTo(12f, 14.5f)
                moveTo(7.5f, 10.5f)
                lineTo(12f, 15f)
                lineTo(16.5f, 10.5f)
                moveTo(4.5f, 19.5f)
                lineTo(19.5f, 19.5f)
            }
        }
    }

    val History: ImageVector by lazy {
        build("History") {
            stroke {
                moveTo(21f, 12f)
                arcTo(9f, 9f, 0f, true, true, 3f, 12f)
                arcTo(9f, 9f, 0f, true, true, 21f, 12f)
                close()
                moveTo(12f, 7.2f)
                lineTo(12f, 12.4f)
                lineTo(15.8f, 14.6f)
            }
        }
    }

    val Wallpaper: ImageVector by lazy {
        build("Wallpaper") {
            stroke {
                moveTo(7f, 2.8f)
                lineTo(17f, 2.8f)
                arcTo(2.2f, 2.2f, 0f, false, true, 19.2f, 5f)
                lineTo(19.2f, 19f)
                arcTo(2.2f, 2.2f, 0f, false, true, 17f, 21.2f)
                lineTo(7f, 21.2f)
                arcTo(2.2f, 2.2f, 0f, false, true, 4.8f, 19f)
                lineTo(4.8f, 5f)
                arcTo(2.2f, 2.2f, 0f, false, true, 7f, 2.8f)
                close()
                moveTo(6.2f, 16.8f)
                lineTo(9.6f, 12.2f)
                lineTo(12.2f, 15.2f)
                lineTo(14.2f, 13f)
                lineTo(17.8f, 16.8f)
            }
        }
    }

    val Phone: ImageVector by lazy {
        build("Phone") {
            stroke {
                moveTo(9f, 2.5f)
                lineTo(15f, 2.5f)
                arcTo(2f, 2f, 0f, false, true, 17f, 4.5f)
                lineTo(17f, 19.5f)
                arcTo(2f, 2f, 0f, false, true, 15f, 21.5f)
                lineTo(9f, 21.5f)
                arcTo(2f, 2f, 0f, false, true, 7f, 19.5f)
                lineTo(7f, 4.5f)
                arcTo(2f, 2f, 0f, false, true, 9f, 2.5f)
                close()
                moveTo(10.8f, 18.4f)
                lineTo(13.2f, 18.4f)
            }
        }
    }

    val Doc: ImageVector by lazy {
        build("Doc") {
            stroke {
                moveTo(6.5f, 2.8f)
                lineTo(14f, 2.8f)
                lineTo(18f, 6.8f)
                lineTo(18f, 21.2f)
                lineTo(6.5f, 21.2f)
                close()
                moveTo(14f, 2.8f)
                lineTo(14f, 6.8f)
                lineTo(18f, 6.8f)
                moveTo(9.2f, 11.4f)
                lineTo(15.2f, 11.4f)
                moveTo(9.2f, 15f)
                lineTo(15.2f, 15f)
                moveTo(9.2f, 18.4f)
                lineTo(12.8f, 18.4f)
            }
        }
    }

    val External: ImageVector by lazy {
        build("External") {
            stroke {
                moveTo(13.5f, 3.6f)
                lineTo(20.4f, 3.6f)
                lineTo(20.4f, 10.5f)
                moveTo(20.4f, 3.6f)
                lineTo(11.5f, 12.5f)
                moveTo(17.6f, 13.6f)
                lineTo(17.6f, 18.8f)
                lineTo(5.2f, 18.8f)
                lineTo(5.2f, 6.4f)
                lineTo(10.4f, 6.4f)
            }
        }
    }
}

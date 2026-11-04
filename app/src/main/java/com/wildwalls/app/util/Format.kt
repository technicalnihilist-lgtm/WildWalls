package com.wildwalls.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatBytes(bytes: Long): String = when {
    bytes >= 1_000_000_000 -> String.format(Locale.US, "%.1f GB", bytes / 1_000_000_000.0)
    bytes >= 1_000_000 -> String.format(Locale.US, "%.1f MB", bytes / 1_000_000.0)
    bytes >= 1_000 -> String.format(Locale.US, "%.0f KB", bytes / 1_000.0)
    else -> "$bytes B"
}

fun formatDate(millis: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(millis))

fun formatDimensions(width: Int, height: Int): String = "$width x $height px"

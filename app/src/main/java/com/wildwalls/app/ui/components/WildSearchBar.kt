package com.wildwalls.app.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.wildwalls.app.ui.theme.Accent
import com.wildwalls.app.ui.theme.GlassBorder
import com.wildwalls.app.ui.theme.GlassFill
import com.wildwalls.app.ui.theme.TextPrimary
import com.wildwalls.app.ui.theme.TextTertiary

@Composable
fun WildSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search wildlife, species, photographer",
    onFocusChange: (Boolean) -> Unit = {},
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(GlassFill)
            .border(1.dp, GlassBorder, RoundedCornerShape(26.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(19.dp)
        )
        Spacer(Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary
                )
            }
            if (!readOnly) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
                    cursorBrush = SolidColor(Accent),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { onFocusChange(it.isFocused) }
                )
            }
        }
        if (query.isNotEmpty() && !readOnly) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Clear search",
                tint = TextTertiary,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onQueryChange("") }
            )
        }
    }
}

/** Suggestion list: recent searches first, then vocabulary matches. Never fake results. */
@Composable
fun SuggestionPanel(
    suggestions: List<String>,
    recent: List<String>,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glass(RoundedCornerShape(20.dp))
            .padding(vertical = 6.dp)
    ) {
        if (recent.isNotEmpty() && suggestions.isEmpty()) {
            Text(
                text = "RECENT",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
            )
        }
        val items = if (suggestions.isEmpty()) recent else suggestions
        items.take(7).forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPick(item) }
                    .padding(horizontal = 16.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = if (suggestions.isEmpty()) WildIcons.History else Icons.Outlined.Search,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }
        }
    }
}

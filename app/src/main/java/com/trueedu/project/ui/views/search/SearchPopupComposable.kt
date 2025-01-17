package com.trueedu.project.ui.views.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trueedu.project.ui.common.DividerHorizontal
import com.trueedu.project.ui.common.TrueText

@Composable
fun SearchPopupItem(
    index: Int,
    checked: Boolean,
    onClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
            .clickable { onClick() }
    ) {
        TrueText(
            s = "관심 그룹 $index",
            fontSize = 16,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(vertical = 8.dp),
        )

        val icon = if (checked) {
            Icons.Filled.Star
        } else {
            Icons.Outlined.StarOutline
        }
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = icon,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "icon"
        )
    }
    DividerHorizontal()
}

package com.trueedu.project.ui.views.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TrueText

@Preview(showBackground = true)
@Composable
fun AddOrderSchedule(
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Outlined.Add,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "checked"
        )
        Margin(8)
        TrueText(
            s = "예약 매매 추가",
            fontSize = 16,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

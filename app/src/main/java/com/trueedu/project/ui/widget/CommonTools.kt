package com.trueedu.project.ui.widget

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun MySwitch(
    checked: Boolean = true,
    onCheckedChange: (Boolean) -> Unit = {},
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.outlineVariant,
            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f),
            disabledCheckedThumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            disabledUncheckedThumbColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.12f),
            disabledCheckedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            disabledUncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.12f)
        )
    )

}

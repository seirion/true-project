package com.trueedu.project.ui.views.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TouchIcon24
import com.trueedu.project.utils.NetworkImage

@Preview(showBackground = true)
@Composable
fun AccountView(
    imageUrl: String = "",
    email: String = "abc@gmail.com",
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 12.dp)
            .padding(horizontal = 16.dp)
    ) {
        NetworkImage(
            imageUrl = imageUrl,
            modifier = Modifier
                .clip(CircleShape)
                .size(32.dp)
        )
        Margin(16)
        BasicText(
            s = email,
            fontSize = 14,
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AccountNumView(
    accountNum: String = "12345678-01",
    selected: Boolean = true,
    onDelete: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    val icon = if (selected) {
        Icons.Outlined.RadioButtonChecked
    } else {
        Icons.Outlined.RadioButtonUnchecked
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
            .padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = icon,
                tint = MaterialTheme.colorScheme.tertiary,
                contentDescription = "checked"
            )
            Margin(8)
            BasicText(
                s = accountNum,
                fontSize = 16,
                color = MaterialTheme.colorScheme.primary,
                style = TextStyle(textDecoration = TextDecoration.Underline),
            )
        }
        TouchIcon24(
            icon = Icons.Outlined.RemoveCircleOutline,
            tint = MaterialTheme.colorScheme.error,
            onClick = onDelete,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddIcon(
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Outlined.Add,
            tint = MaterialTheme.colorScheme.tertiary,
            contentDescription = "checked"
        )
        Margin(8)
        BasicText(
            s = "계좌, 키 추가하기",
            fontSize = 16,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

package com.trueedu.project.ui.views.spac

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.widget.DigitInput

@Preview(showBackground = true)
@Composable
fun SpacValueSection() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 12.dp, 16.dp, 2.dp),
    ) {
        TrueText(
            s = "기준가격",
            fontSize = 14,
            color = MaterialTheme.colorScheme.primary,
        )
        TrueText(
            s = "청산가격",
            fontSize = 14,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun ColumnScope.SpacValueView(
    baseInput: MutableState<TextFieldValue>,
    targetInput: MutableState<TextFieldValue>,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        DigitInput(baseInput, Modifier.width(120.dp))
        DigitInput(targetInput, Modifier.width(120.dp))
    }
}

@Composable
fun ColumnScope.SpacDataView(title: String, value: String, valueColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        TrueText(
            s = title,
            fontSize = 16,
            color = MaterialTheme.colorScheme.primary,
        )
        TrueText(
            s = value,
            fontSize = 16,
            color = valueColor,
        )
    }
}

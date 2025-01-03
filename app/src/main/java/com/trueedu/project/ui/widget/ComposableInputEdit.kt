package com.trueedu.project.ui.widget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.project.ui.common.TouchIcon24
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.utils.getDigitInput

@Preview(showBackground = true)
@Composable
fun InputSet(
    label: String = "가격",
    input: MutableState<TextFieldValue> = mutableStateOf(TextFieldValue("1000")),
    increase: () -> Unit = {},
    decrease: () -> Unit = {},
) {
    Row {
        InputLabel(label)
        Spacer(modifier = Modifier.width(40.dp))
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        TouchIcon24(Icons.Outlined.RemoveCircleOutline, onClick = decrease)
        DigitInput(input, Modifier.weight(1f))
        TouchIcon24(Icons.Outlined.AddCircleOutline, onClick = increase)
    }
}

@Composable
fun InputLabel(label: String) {
    TrueText(
        s = label,
        fontSize = 14,
        color = MaterialTheme.colorScheme.secondary,
    )
}

@Composable
fun DigitInput(
    input: MutableState<TextFieldValue>,
    modifier: Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }
    BasicTextField(
        value = input.value,
        onValueChange = {
            val text = getDigitInput(it.text)
            input.value = it.copy(
                text = text,
                selection = TextRange(text.length)
            )
        },
        modifier = modifier
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    // 포커스를 받았을 때 커서를 텍스트 끝으로 이동
                    input.value = input.value.copy(
                        selection = TextRange(input.value.text.length)
                    )
                }
            }
            .background(
                color = if (isFocused) {
                    MaterialTheme.colorScheme.surfaceDim
                } else {
                    MaterialTheme.colorScheme.background
                }
            )
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(12.dp),
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
    )
}

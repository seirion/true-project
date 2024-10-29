package com.trueedu.project.ui.views.order

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.utils.getDigitInput

class OrderViewDrawer(
    private val vm: OrderViewModel,
): ComposableDrawer {
    @Composable
    override fun Draw() {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column {
                    Section()
                    OrderBook(vm.sells(), vm.buys(), vm.price(), vm.previousClose())
                }
                Margin(8)
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    InputLabel("가격")
                    DigitInput(vm.priceInput)
                    Margin(24)
                    InputLabel("수량")
                    DigitInput(vm.quantityInput)
                }
            }
            SellBuyButtons()
        }
    }
}

@Composable
fun InputLabel(label: String) {
    BasicText(
        s = label,
        fontSize = 14,
        color = MaterialTheme.colorScheme.secondary,
    )
}

@Composable
fun DigitInput(input: MutableState<Long>) {
    var isFocused by remember { mutableStateOf(false) }
    BasicTextField(
        value = input.value.toString(),
        onValueChange = { it: String ->
            input.value = getDigitInput(it)
        },
        modifier = Modifier
            .onFocusChanged {
                isFocused = it.isFocused
            }
            .fillMaxWidth()
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
            textAlign = TextAlign.End,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
    )
}

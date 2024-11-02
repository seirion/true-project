package com.trueedu.project.ui.views.order

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TouchIcon24
import com.trueedu.project.utils.getDigitInput

class OrderViewDrawer(
    private val vm: OrderViewModel,
    private val buy: () -> Unit,
    private val sell: () -> Unit,
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
                    OrderBook(vm.sells(), vm.buys(), vm.price(), vm.previousClose()) {
                        vm.setPrice(it)
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .padding(horizontal = 2.dp)
                ) {
                    InputSet("가격", vm.priceInput, vm::increasePrice, vm::decreasePrice)
                    Margin(24)
                    InputSet("수량", vm.quantityInput, vm::increaseQuantity, vm::decreaseQuantity)
                }
            }
            SellBuyButtons(buy, sell)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InputSet(
    label: String = "가격",
    input: MutableState<String> = mutableStateOf("1000"),
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
private fun InputLabel(label: String) {
    TrueText(
        s = label,
        fontSize = 14,
        color = MaterialTheme.colorScheme.secondary,
    )
}

@Composable
private fun DigitInput(
    input: MutableState<String>,
    modifier: Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }
    BasicTextField(
        value = input.value,
        onValueChange = { it: String ->
            input.value = getDigitInput(it)
        },
        modifier = modifier
            .onFocusChanged {
                isFocused = it.isFocused
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
            textAlign = TextAlign.End,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
    )
}

package com.trueedu.project.ui.views.order

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.project.model.dto.price.OrderModifiableDetail
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TouchIcon24
import com.trueedu.project.utils.formatter.cashFormatter

class ModifiableViewDrawer(
    private val vm: OrderModifyViewModel,
): ComposableDrawer {
    @Composable
    override fun Draw() {
        if (vm.loading.value || vm.items.value == null) {
            LoadingView()
        } else {
            val state = rememberLazyListState()
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                item { ModifiableSection() }
                val items = vm.items.value!!.orderModifiableDetail
                itemsIndexed(items, key = { _, item -> item.code }) { index, item ->
                    val checked = vm.checked.containsKey(item.code)
                    ItemView(item, checked, vm::onChecked) {

                    }
                }
            }
        }
    }

    @Composable
    private fun ItemView(
        item: OrderModifiableDetail,
        checked: Boolean,
        onChecked: (String) -> Unit,
        onClick: () -> Unit,
    ) {
        val icon = if (checked) {
            Icons.Filled.CheckBox
        } else {
            Icons.Filled.CheckBoxOutlineBlank
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .clickable { onClick() }
        ) {
            TouchIcon24(icon) { onChecked(item.code) }
            BasicText(
                s = item.nameKr,
                fontSize = 12,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1.5f),
            )
            listOf(
                cashFormatter.format(item.price.toDouble()),
                cashFormatter.format(item.quantity.toDouble()),
                item.orderTime.chunked(2).joinToString(":"),
            ).forEach {
                BasicText(
                    s = it,
                    fontSize = 12,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ModifiableSection() {
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    val textList = listOf("종목", "가격", "수량", "시간")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .border(width = 1.dp, borderColor)
            .padding(vertical = 2.dp),
    ) {
        val textColor = MaterialTheme.colorScheme.primary
        val bgColor = MaterialTheme.colorScheme.background
        Margin(40) // chechbox 영역
        textList.forEachIndexed { index, s ->
            val weight = if (index == 0) 1.5f else 1f
            BasicText(
                s =  s,
                fontSize = 12,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(weight)
            )
            VerticalDivider(thickness = 1.dp, color = borderColor)
        }
        Margin(1)
    }
}

package com.trueedu.project.ui.views.order

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.trueedu.project.ui.common.TouchIcon32
import com.trueedu.project.utils.formatter.cashFormatter

class ModifiableViewDrawer(
    private val vm: OrderViewModel,
    private val modifiableVm: OrderModifyViewModel,
): ComposableDrawer {
    @Composable
    override fun Draw() {
        if (modifiableVm.loading.value || modifiableVm.items.value == null) {
            LoadingView()
        } else {
            val state = rememberLazyListState()
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                item { ModifiableSection() }
                val items = modifiableVm.items.value!!.orderModifiableDetail
                itemsIndexed(items, key = { _, item -> item.code }) { index, item ->
                    val checked = modifiableVm.checked.containsKey(item.code)
                    ItemView(item, checked, modifiableVm::onChecked) {

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
            TouchIcon32(icon) { onChecked(item.code) }
            Margin(8)
            listOf(
                item.nameKr,
                cashFormatter.format(item.price.toDouble()),
                cashFormatter.format(item.quantity.toDouble()),
                item.orderTime,
            ).forEach {
                BasicText(
                    s = item.nameKr,
                    fontSize = 12,
                    color = MaterialTheme.colorScheme.primary,
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
    val textList = listOf(
        "종목",
        "가격",
        "수량",
        "시간",
    )
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
        Margin(48) // chechbox 영역
        textList.forEach {
            BasicText(
                s =  it,
                fontSize = 12,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(thickness = 1.dp, color = borderColor)
        }
        Margin(1)
    }
}

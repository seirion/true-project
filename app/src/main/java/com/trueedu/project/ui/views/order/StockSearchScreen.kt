package com.trueedu.project.ui.views.order

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.trueedu.project.model.dao.StockInfoLocal
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TouchIcon32
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.views.common.RoundedBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockSearchScreen(
    vm: OrderViewModel,
    onClose: () -> Unit,
) {
    BackHandler { onClose() }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    TouchIcon32(
                        icon = Icons.Filled.ChevronLeft,
                        onClick = onClose,
                    )
                },
                title = {
                    TrueText(
                        s = "종목 검색",
                        fontSize = 20,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = vm.searchQuery.value,
                onValueChange = { vm.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .focusRequester(focusRequester),
                placeholder = {
                    TrueText(
                        s = "종목이름, 종목코드",
                        fontSize = 14,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    )
                },
                trailingIcon = {
                    if (vm.searchQuery.value.isNotEmpty()) {
                        IconButton(onClick = { vm.updateSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            )

            val state = rememberLazyListState()
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    vm.searchResults.value,
                    key = { _, item -> item.code }
                ) { _, item ->
                    StockSearchItem(item) {
                        vm.selectStock(item)
                        onClose()
                    }
                }
            }
        }
    }
}

@Composable
private fun StockSearchItem(
    item: StockInfoLocal,
    onClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .height(48.dp)
            .padding(horizontal = 16.dp)
    ) {
        TrueText(
            s = item.nameKr,
            fontSize = 16,
            fontWeight = FontWeight.W600,
            color = MaterialTheme.colorScheme.primary,
        )
        Margin(8)
        TrueText(
            s = item.code,
            fontSize = 14,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        if (item.kospi) {
            RoundedBadge("KOSPI", Color(0xFF1565C0))
        } else {
            RoundedBadge("KOSDAQ", Color(0xFF6A1B9A))
        }
    }
}

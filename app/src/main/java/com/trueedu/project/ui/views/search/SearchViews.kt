package com.trueedu.project.ui.views.search

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.project.model.dto.StockInfo
import com.trueedu.project.ui.common.BasicText

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun SearchBar(
    searchText: MutableState<String> = mutableStateOf(""),
    modifier: Modifier = Modifier,
    hint: String = "종목이름, 심볼",
    onSearch: (String) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = searchText.value,
        onValueChange = { searchText.value = it },
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { BasicText(hint, 14, color = MaterialTheme.colorScheme.surfaceVariant) },
        /*
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
         */
        trailingIcon = {
            if (searchText.value.isNotEmpty()) {
                IconButton(onClick = { searchText.value = "" }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear"
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            onSearch(searchText.value)
            focusManager.clearFocus() // Optional: Hide keyboard
        }),
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        )
    )
}

@Composable
fun SearchList(list: List<StockInfo>) {
    val state = rememberLazyListState()
    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(list, key = { _, item -> item.code }) { index, item ->
            SearchStockItem(item)
        }
    }
}

@Preview
@Composable
fun SearchStockItem(
    item: StockInfo = StockInfo("003456", "삼성전자", "")
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp)
    ) {
        val s = "${item.nameKr} (${item.code})"
        BasicText(s = s, fontSize = 16, color = MaterialTheme.colorScheme.primary)
    }
}

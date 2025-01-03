package com.trueedu.project.ui.assets

import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.trueedu.project.R
import com.trueedu.project.data.ManualAssets
import com.trueedu.project.data.StockPool
import com.trueedu.project.model.dto.firebase.UserAsset
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BottomBar
import com.trueedu.project.ui.common.ButtonAction
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.PopupFragment
import com.trueedu.project.ui.common.PopupType
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.widget.InputSet
import com.trueedu.project.utils.decreasePrice
import com.trueedu.project.utils.decreaseQuantity
import com.trueedu.project.utils.increasePrice
import com.trueedu.project.utils.increaseQuantity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EditAssetFragment: BaseFragment() {
    companion object {
        fun show(
            code: String,
            fragmentManager: FragmentManager,
            onCompleted: () -> Unit,
        ): EditAssetFragment {
            return EditAssetFragment().also {
                it.code = code
                it.onCompleted = onCompleted
                it.show(fragmentManager, "edit-asset")
            }
        }
    }

    @Inject
    lateinit var manualAssets: ManualAssets
    @Inject
    lateinit var stockPool: StockPool

    lateinit var code: String
    lateinit var onCompleted: () -> Unit

    private val buttonEnabled = mutableStateOf(false)

    private val editMode = mutableStateOf(false)
    // 주문 입력 (숫자만)
    private val priceInput = mutableStateOf(TextFieldValue(""))
    private val quantityInput = mutableStateOf(TextFieldValue("0"))
    private val memoInput = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenSheetKeyboardDialogTheme)
    }

    override fun init() {
        manualAssets.assets.value.firstOrNull { it.code == code }?.let { myAsset ->
            // 이 종목을 이미 보유한 경우 원래 값을 입력 해 줌
            priceInput.value = myAsset.price.toInt().toString().let { TextFieldValue(it) }
            quantityInput.value = myAsset.quantity.toInt().toString().let { TextFieldValue(it) }
            memoInput.value = myAsset.memo
            editMode.value = true // 편집 모드임
        }

        lifecycleScope.launch {
            merge(
                snapshotFlow { priceInput.value.text },
                snapshotFlow { quantityInput.value.text }
            )
                .collectLatest {
                    checkButtonEnabled()
                }
        }
    }

    @Composable
    override fun BodyScreen() {
        if (!::code.isInitialized) dismissAllowingStateLoss()
        Scaffold(
            topBar = {
                val nameKr = stockPool.get(code)?.nameKr?: ""
                val onAction = if (editMode.value) ::onDelete else null
                BackTitleTopBar(
                    title = nameKr,
                    onBack = ::dismissAllowingStateLoss,
                    actionIcon = Icons.Outlined.Delete,
                    onAction = onAction,
                )
            },
            bottomBar = { BottomBar("저장", buttonEnabled.value, ::onSave) },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.End,
            ) {
                Margin(24)
                InputSet("평단가", priceInput, ::increasePrice, ::decreasePrice)
                Margin(24)
                InputSet("수량", quantityInput, ::increaseQuantity, ::decreaseQuantity)
                Margin(24)
                MemoInput(memoInput)
            }
        }
    }

    private fun onSave() {
        trueAnalytics.clickButton("${screenName()}__save__click")
        val stock = stockPool.get(code)
        manualAssets.addAsset(
            UserAsset(
                code = code,
                nameKr = stock?.nameKr?: "",
                price = priceInput.value.text.toDouble(),
                quantity = quantityInput.value.text.toDouble(),
                memo = memoInput.value,
            )
        ) {
            dismissAllowingStateLoss()
            Toast.makeText(requireContext(), "추가되었습니다", Toast.LENGTH_SHORT).show()
            onCompleted()
        }
    }

    private fun onDelete() {
        trueAnalytics.clickButton("${screenName()}__delete__click")
        PopupFragment.show(
            title = "종목 삭제",
            desc = "종목을 삭제합니다",
            popupType = PopupType.DELETE_CANCEL,
            buttonActions = listOf(
                ButtonAction(label = "삭제", onClick = ::delete),
                ButtonAction(label = "취소", onClick = {}),
            ),
            cancellable = true,
            fragmentManager = parentFragmentManager,
        )
    }

    private fun delete() {
        manualAssets.deleteAsset(code) {
            Toast.makeText(requireContext(), "삭제되었습니다", Toast.LENGTH_SHORT).show()
            dismissAllowingStateLoss()
            onCompleted()
        }
    }

    private fun checkButtonEnabled() {
        buttonEnabled.value = priceInput.value.text.isNotEmpty() &&
                priceInput.value.text.toDouble() > 0 &&
                quantityInput.value.text.isNotEmpty() &&
                quantityInput.value.text.toDouble() > 0
    }

    private fun increasePrice() {
        priceInput.value = priceInput.value.copy(
            text = increasePrice(priceInput.value.text)
        )
    }

    private fun decreasePrice() {
        // TODO: 상하한가 체크 필요
        priceInput.value = priceInput.value.copy(
            text = decreasePrice(priceInput.value.text)
        )
    }

    private fun increaseQuantity() {
        quantityInput.value = quantityInput.value.copy(
            text = increaseQuantity(quantityInput.value.text)
        )
    }

    private fun decreaseQuantity() {
        quantityInput.value = quantityInput.value.copy(
            text = decreaseQuantity(quantityInput.value.text)
        )
    }
}

@Composable
private fun MemoInput(s: MutableState<String>) {
    OutlinedTextField(
        value = s.value,
        onValueChange = {
            s.value = it.take(128)
        },
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 40.dp),
        label = {
            TrueText(
                s = "메모(최대 128자)",
                fontSize = 14,
                color = MaterialTheme.colorScheme.secondary
            )
        },
        maxLines = 8,
    )
}

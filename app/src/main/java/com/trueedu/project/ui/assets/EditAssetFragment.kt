package com.trueedu.project.ui.assets

import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.trueedu.project.R
import com.trueedu.project.data.AssetManager
import com.trueedu.project.data.StockPool
import com.trueedu.project.model.dto.firebase.UserAsset
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BottomBar
import com.trueedu.project.ui.common.Margin
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
            fragmentManager: FragmentManager
        ): EditAssetFragment {
            return EditAssetFragment().also {
                it.code = code
                it.show(fragmentManager, "edit-asset")
            }
        }
    }

    @Inject
    lateinit var assetManager: AssetManager
    @Inject
    lateinit var stockPool: StockPool

    lateinit var code: String

    private val buttonEnabled = mutableStateOf(false)

    // 주문 입력 (숫자만)
    val priceInput = mutableStateOf(TextFieldValue(""))
    val quantityInput = mutableStateOf(TextFieldValue("1"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenSheetKeyboardDialogTheme)
    }

    override fun init() {
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
                BackTitleTopBar(
                    title = "보유 종목 추가",
                    onBack = ::dismissAllowingStateLoss,
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
            }
        }
    }

    private fun onSave() {
        trueAnalytics.clickButton("${screenName()}__save__click")
        val stock = stockPool.get(code)
        assetManager.addAsset(
            UserAsset(
                code = code,
                nameKr = stock?.nameKr?: "",
                price = priceInput.value.text.toDouble(),
                quantity = quantityInput.value.text.toDouble(),
            )
        ) {
            dismissAllowingStateLoss()
            Toast.makeText(requireContext(), "추가되었습니다", Toast.LENGTH_SHORT).show()
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

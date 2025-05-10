package com.trueedu.project.ui.views.schedule

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.trueedu.project.R
import com.trueedu.project.model.dto.order.ScheduleOrderResultDetail
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TouchIcon24
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.views.order.ModifyButtons
import com.trueedu.project.ui.widget.InputSet
import com.trueedu.project.utils.formatter.dateFormat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScheduleModifyFragment: BaseFragment() {
    companion object {
        private val TAG = ScheduleModifyFragment::class.java.simpleName

        fun show(
            orderDetail: ScheduleOrderResultDetail,
            fragmentManager: FragmentManager,
            onCompleted: (String, String) -> Unit,
        ): ScheduleModifyFragment {
            return ScheduleModifyFragment().also {
                it.orderDetail = orderDetail
                it.onCompleted = onCompleted
                it.show(fragmentManager, "schedule_modify")
            }
        }
    }

    lateinit var orderDetail: ScheduleOrderResultDetail
    var onCompleted: (String, String) -> Unit = { _, _ -> }

    private val priceInput = mutableStateOf(TextFieldValue(""))

    private val quantityInput = mutableStateOf(TextFieldValue("1"))

    override fun init() {
        super.init()
        if (!::orderDetail.isInitialized) {
            dismissAllowingStateLoss()
        }
        priceInput.value = priceInput.value.copy(
            text = orderDetail.price
        )
        quantityInput.value = quantityInput.value.copy(
            text = orderDetail.orderReservedQuantity
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenSheetKeyboardDialogTheme)

        if (!::orderDetail.isInitialized) {
            dismissAllowingStateLoss()
        }
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = {
                BackTitleTopBar(
                    title = "예약 매매 수정",
                    onBack = ::dismissAllowingStateLoss,
                )
            },
            bottomBar = {
                val isBuy = orderDetail.sellBuyDivisionCode == "02"
                ModifyButtons(isBuy, ::onClick)
            },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TouchIcon24(icon = Icons.Outlined.Search) {}
                    Margin(8)
                    TrueText(s = orderDetail.nameKr, fontSize = 14)
                }
                Margin(12)
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.width(240.dp)
                ) {
                    InputSet("가격", priceInput, ::increasePrice, ::decreasePrice)
                    Margin(24)
                    InputSet("수량", quantityInput, ::increaseQuantity, ::decreaseQuantity)
                }
                Margin(48)
                InfoViews(orderDetail)
            }
        }
    }

    private fun increasePrice() {
        priceInput.value = priceInput.value.copy(
            text = com.trueedu.project.utils.increasePrice(priceInput.value.text)
        )
    }

    private fun decreasePrice() {
        priceInput.value = priceInput.value.copy(
            text = com.trueedu.project.utils.decreasePrice(priceInput.value.text)
        )
    }

    private fun increaseQuantity() {
        quantityInput.value = quantityInput.value.copy(
            text = com.trueedu.project.utils.increaseQuantity(quantityInput.value.text)
        )
    }

    private fun decreaseQuantity() {
        quantityInput.value = quantityInput.value.copy(
            text = com.trueedu.project.utils.decreaseQuantity(quantityInput.value.text)
        )
    }

    private fun onClick() {
        trueAnalytics.clickButton("${screenName()}__button__click")
        if (priceInput.value.text.isBlank() || quantityInput.value.text.isBlank()) {
            return
        }

        val price = priceInput.value.text
        val quantity = quantityInput.value.text
        onCompleted(price, quantity)
        dismissAllowingStateLoss()
    }
}

@Composable
private fun InfoViews(order: ScheduleOrderResultDetail) {
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        listOf(
            "예약주문 순번" to order.seq,
            "예약 주문일자" to dateFormat(order.orderDate),
            "예약 접수일자" to dateFormat(order.receivedDate),
            "예약 종료일자" to dateFormat(order.endDate),
        ).forEach {
            TrueText(
                s = "${it.first}: ${it.second}",
                fontSize = 14,
            )
        }
    }
}

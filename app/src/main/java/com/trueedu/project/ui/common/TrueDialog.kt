package com.trueedu.project.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * PopupFragment 를 대체하는 순수 Composable Dialog.
 *
 * @param popupType   버튼 색상 타입
 * @param title       다이얼로그 제목
 * @param desc        본문 텍스트
 * @param actions     버튼 목록. ButtonAction.onClick 은 버튼 고유 동작만 담당하며,
 *                    다이얼로그 닫기는 TrueDialog 가 별도로 처리합니다.
 * @param cancellable 바깥 터치 / 뒤로가기로 닫기 허용 여부
 * @param onDismiss   다이얼로그가 닫힐 때 호출 (버튼 클릭 및 외부 취소 모두 포함)
 */
@Composable
fun TrueDialog(
    popupType: PopupType = PopupType.OK,
    title: String,
    desc: String,
    actions: List<ButtonAction> = emptyList(),
    cancellable: Boolean = true,
    onDismiss: () -> Unit = {},
) {
    var visible by remember { mutableStateOf(true) }

    fun dismiss() {
        visible = false
        onDismiss()
    }

    if (!visible) return

    Dialog(
        onDismissRequest = { if (cancellable) dismiss() },
        properties = DialogProperties(
            dismissOnBackPress = cancellable,
            dismissOnClickOutside = cancellable,
        ),
    ) {
        TrueDialogBody(
            popupType = popupType,
            title = title,
            desc = desc,
            actions = actions,
            dismissPopup = ::dismiss,
        )
    }
}

@Preview
@Composable
internal fun TrueDialogBody(
    popupType: PopupType = PopupType.OK,
    title: String = "test",
    desc: String = "test\nDESC",
    actions: List<ButtonAction> = listOf(
        ButtonAction("ok", {}),
        ButtonAction("cancel", {}),
    ),
    dismissPopup: () -> Unit = {},
) {
    RoundedColumn(
        radius = 20,
        bgColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .width(300.dp)
            .wrapContentHeight()
    ) {
        TrueText(
            s = title,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 20,
            fontWeight = FontWeight.W600,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 32.dp, start = 20.dp, end = 20.dp)
                .wrapContentHeight()
                .fillMaxWidth()
        )

        val scrollState = rememberScrollState()
        val screenHeightDp = LocalConfiguration.current.screenHeightDp
        val maxBodyHeight = (screenHeightDp * 0.5f).dp

        Box(
            modifier = Modifier
                .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                .heightIn(max = maxBodyHeight)
                .verticalScroll(scrollState)
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            TrueText(
                s = desc,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14,
                textAlign = TextAlign.Center,
                maxLines = Int.MAX_VALUE,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            modifier = Modifier
                .padding(16.dp)
                .wrapContentSize()
        ) {
            val colorScheme = MaterialTheme.colorScheme

            actions.reversed().forEachIndexed { i, (label, onClick) ->
                val (textColor, bgColor) = if (i == actions.lastIndex) {
                    popupType.mainColor(colorScheme)
                } else {
                    popupType.cancelColor(colorScheme)
                }

                Button(
                    onClick = {
                        onClick?.invoke()
                        dismissPopup()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .padding(horizontal = 8.dp),
                    colors = ButtonColors(
                        containerColor = bgColor,
                        contentColor = textColor,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = Color.Transparent,
                    ),
                ) {
                    TrueText(s = label, fontSize = 14, color = textColor)
                }
            }
        }
    }
}

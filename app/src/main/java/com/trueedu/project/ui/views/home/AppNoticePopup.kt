package com.trueedu.project.ui.views.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.trueedu.project.model.dto.firebase.AppNotice
import com.trueedu.project.ui.common.ButtonAction
import com.trueedu.project.ui.common.PopupType
import com.trueedu.project.ui.common.TrueDialog

@Composable
fun AppNoticePopup(
    appNotice: AppNotice,
    onClickButton: (() -> Unit) = {},
) {
    TrueDialog(
        title = appNotice.title,
        desc = appNotice.body,
        popupType = PopupType.OK,
        actions = listOf(
            ButtonAction("확인") { onClickButton() }
        ),
        cancellable = appNotice.cancellable,
        onDismiss = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun AppNoticePopupPreview() {
    AppNoticePopup(
        appNotice = AppNotice(
            id = 1,
            title = "공지사항",
            body = "공지 내용입니다",
            cancellable = true,
        )
    )
}

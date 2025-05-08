package com.trueedu.project.ui.views.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentManager
import com.trueedu.project.model.dto.firebase.AppNotice
import com.trueedu.project.ui.common.ButtonAction
import com.trueedu.project.ui.common.PopupFragment
import com.trueedu.project.ui.common.PopupType


@Composable
fun AppNoticePopup(
    appNotice: AppNotice,
    fm: FragmentManager,
    onClickButton: (() -> Unit) = {},
) {
    AppNoticePopupInternal(
        appNotice.title,
        appNotice.body,
        cancellable = appNotice.cancellable,
        fm = fm,
        onClickButton = onClickButton,
    )
}

@Preview(showBackground = true)
@Composable
private fun AppNoticePopupInternal(
    title: String = "Title",
    body: String = "공지 내용입니다",
    cancellable: Boolean = true,
    fm: FragmentManager? = null,
    onClickButton: (() -> Unit) = {},
) {
    PopupFragment.show(
        title = title,
        desc = body,
        popupType = PopupType.OK,
        buttonActions = listOf(
            ButtonAction("확인") {
                onClickButton()
            }
        ),
        cancellable = cancellable,
        fm!!,
    )
}

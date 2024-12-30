package com.trueedu.project.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.trueedu.project.R
import com.trueedu.project.data.ScreenControl
import com.trueedu.project.ui.theme.TrueProjectTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

data class ButtonAction(
    val label: String,
    val onClick: (() -> Unit)?
)

enum class PopupType {
    OK,
    YES_NO,
    DELETE_CANCEL,
    ;

    /**
     * return textColor, backgroundColor
     */
    fun mainColor(colorScheme: ColorScheme): Pair<Color, Color> {
        return when (this) {
            OK -> {
                colorScheme.background to colorScheme.inverseSurface
            }
            YES_NO -> {
                colorScheme.background to colorScheme.primaryContainer
            }
            DELETE_CANCEL -> {
                colorScheme.background to colorScheme.error
            }
        }
    }

    /**
     * return textColor, backgroundColor
     */
    fun cancelColor(colorScheme: ColorScheme): Pair<Color, Color> {
        return colorScheme.background to colorScheme.outline
    }
}

@AndroidEntryPoint
class PopupFragment: DialogFragment() {
    companion object {
        private val TAG = PopupFragment::class.java.simpleName

        fun show(
            title: String,
            desc: String,
            popupType: PopupType,
            buttonActions: List<ButtonAction> = listOf(),
            cancellable: Boolean = true,
            fragmentManager: FragmentManager,
        ): PopupFragment {
            return PopupFragment().also {
                it.title = title
                it.desc = desc
                it.popupType = popupType
                it.actions.addAll(buttonActions)
                it.isCancelable = cancellable
                it.show(fragmentManager, null)
            }
        }
    }

    private var title: String = ""
    private var desc: String = ""
    private var popupType: PopupType = PopupType.OK
    private val actions: MutableList<ButtonAction> = mutableListOf()

    @Inject
    lateinit var screen: ScreenControl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.PopupDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = ComposeView(requireContext())
        root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        root.setBackgroundColor(Color.Transparent.toArgb())

        root.setContent {
            TrueProjectTheme(
                n = screen.theme.intValue,
                forceDark = screen.forceDark.value
            ) {
                PopupBody(popupType, title, desc, actions) {
                    dismissAllowingStateLoss()
                }
            }
        }
        return root
    }
}

@Preview
@Composable
private fun PopupBody(
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

        TrueText(
            s = desc,
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 14,
            textAlign = TextAlign.Center,
            maxLines = Int.MAX_VALUE,
            modifier = Modifier
                .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                .wrapContentHeight()
                .fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .padding(16.dp)
                .wrapContentSize()
        ) {
            val colorScheme = MaterialTheme.colorScheme

            actions.reversed().forEachIndexed { i, (label, onClick) ->
                val (textColor, bgColor) = if (i == actions.lastIndex) {
                    // 실제 동작 버튼
                    popupType.mainColor(colorScheme)
                } else {
                    // 취소 버튼
                    popupType.cancelColor(colorScheme)
                }

                Button(
                    onClick = {
                        onClick?.invoke()
                        dismissPopup()
                    },
                    modifier = Modifier.weight(1f)
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

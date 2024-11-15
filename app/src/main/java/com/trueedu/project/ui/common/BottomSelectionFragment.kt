package com.trueedu.project.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.trueedu.project.R
import com.trueedu.project.data.ScreenControl
import com.trueedu.project.ui.theme.TrueProjectTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BottomSelectionFragment: BottomSheetDialogFragment() {
    companion object {
        fun show(
            selected: Int?,
            title: String,
            list: List<String>,
            onSelected: (Int) -> Unit,
            fragmentManager: FragmentManager,
        ): BottomSelectionFragment {
            return BottomSelectionFragment().also {
                if (selected != null) it.selected.intValue = selected
                it.title = title
                it.list = list
                it.onSelected = onSelected
                it.show(fragmentManager, "")
            }
        }
    }

    @Inject
    lateinit var screen: ScreenControl

    private val selected = mutableIntStateOf(-1)
    private lateinit var list: List<String>
    lateinit var onSelected: (Int) -> Unit

    lateinit var title: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.PopupDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).also {
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            it.setContent {
                TrueProjectTheme(
                    n = screen.theme.intValue,
                    forceDark = screen.forceDark.value
                ) {
                    Body()
                }
            }
        }
    }

    @Composable
    private fun Body() {
        if (!::title.isInitialized) dismissAllowingStateLoss()
        RoundedTopColumn(
            radius = 20,
            bgColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            TrueText(
                s = title,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20,
                fontWeight = FontWeight.W600,
                modifier = Modifier
                    .padding(16.dp)
                    .wrapContentHeight()
                    .fillMaxWidth()
            )
            list.forEachIndexed { i, s ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelected(i)
                            dismissAllowingStateLoss()
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val icon = if (selected.intValue == i) {
                        Icons.Filled.CheckCircle
                    } else {
                        Icons.Outlined.CheckCircle
                    }
                    val iconColor = if (selected.intValue == i) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    }
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = icon,
                        tint = iconColor,
                        contentDescription = "checked"
                    )
                    TrueText(
                        s = s,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16,
                    )
                }
            }
        }
    }
}

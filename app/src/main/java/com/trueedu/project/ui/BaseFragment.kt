package com.trueedu.project.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.trueedu.project.R
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.data.ScreenControl
import com.trueedu.project.ui.theme.TrueProjectTheme
import javax.inject.Inject

open class BaseFragment: BottomSheetDialogFragment() {

    @Inject
    lateinit var trueAnalytics: TrueAnalytics

    @Inject
    lateinit var screen: ScreenControl

    protected var behavior: BottomSheetBehavior<*>? = null

    protected fun screenName(): String {
        val simpleName = this::class.java.simpleName
        return simpleName
            .substring(0, simpleName.length - "Fragment".length)
            .replace(Regex("(?<=[a-z])([A-Z])")) {
                "_${it.value}"
            }
            .lowercase()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FillScreenSheetTheme)

        trueAnalytics.enterView("${screenName()}__enter")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            (this as? BottomSheetDialog)?.behavior?.run {
                this@BaseFragment.behavior = this
                this.skipCollapsed = true
                this.state = BottomSheetBehavior.STATE_EXPANDED
                this.isDraggable = false
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        init()
        return ComposeView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setContent {
                TrueProjectTheme(
                    n = screen.theme.intValue,
                    forceDark = screen.forceDark.value
                ) {
                    BodyScreen()
                }
            }
        }
    }

    open fun init() {

    }

    @Composable
    open fun BodyScreen() {

    }
}

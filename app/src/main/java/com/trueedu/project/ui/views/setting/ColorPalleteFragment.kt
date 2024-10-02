package com.trueedu.project.ui.views.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BasicText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ColorPaletteFragmentFragment: BaseFragment() {
    companion object {
        fun show(
            fragmentManager: FragmentManager
        ): ColorPaletteFragmentFragment {
            val fragment = ColorPaletteFragmentFragment()
            fragment.show(fragmentManager, "color-palette")
            return fragment
        }
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = { BackTitleTopBar("Color Palette", ::dismissAllowingStateLoss) },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                ColorView()
            }
        }
    }

    @Composable
    private fun getColorSchemeList(): List<Pair<String, Color>> {
        return listOf(
            "primary" to MaterialTheme.colorScheme.primary,
            "onPrimary" to MaterialTheme.colorScheme.onPrimary,
            "primaryContainer" to MaterialTheme.colorScheme.primaryContainer,
            "onPrimaryContainer" to MaterialTheme.colorScheme.onPrimaryContainer,
            "inversePrimary" to MaterialTheme.colorScheme.inversePrimary,
            "secondary" to MaterialTheme.colorScheme.secondary,
            "onSecondary" to MaterialTheme.colorScheme.onSecondary,
            "secondaryContainer" to MaterialTheme.colorScheme.secondaryContainer,
            "onSecondaryContainer" to MaterialTheme.colorScheme.onSecondaryContainer,
            "tertiary" to MaterialTheme.colorScheme.tertiary,
            "onTertiary" to MaterialTheme.colorScheme.onTertiary,
            "tertiaryContainer" to MaterialTheme.colorScheme.tertiaryContainer,
            "onTertiaryContainer" to MaterialTheme.colorScheme.onTertiaryContainer,
            "error" to MaterialTheme.colorScheme.error,
            "onError" to MaterialTheme.colorScheme.onError,
            "errorContainer" to MaterialTheme.colorScheme.errorContainer,
            "onErrorContainer" to MaterialTheme.colorScheme.onErrorContainer,
            "background" to MaterialTheme.colorScheme.background,
            "onBackground" to MaterialTheme.colorScheme.onBackground,
            "surface" to MaterialTheme.colorScheme.surface,
            "onSurface" to MaterialTheme.colorScheme.onSurface,
            "surfaceVariant" to MaterialTheme.colorScheme.surfaceVariant,
            "onSurfaceVariant" to MaterialTheme.colorScheme.onSurfaceVariant,
            "surfaceTint" to MaterialTheme.colorScheme.surfaceTint,
            "inverseSurface" to MaterialTheme.colorScheme.inverseSurface,
            "inverseOnSurface" to MaterialTheme.colorScheme.inverseOnSurface,
            "outline" to MaterialTheme.colorScheme.outline,
            "outlineVariant" to MaterialTheme.colorScheme.outlineVariant,
            "scrim" to MaterialTheme.colorScheme.scrim
        )
    }

    @Preview(showBackground = true)
    @Composable
    private fun ColorView() {
        val list = getColorSchemeList()
        val state = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(state)
        ) {
            list.forEach { (name, color) ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    BasicText(s = name, fontSize = 14, color = MaterialTheme.colorScheme.primary)
                    Box(
                        modifier = Modifier.width(96.dp)
                            .height(48.dp)
                            .background(color = color)
                    ) {

                    }
                }
            }
            if (state.isScrollInProgress) {
                behavior?.isDraggable = state.value == 0
            }
        }
    }
}

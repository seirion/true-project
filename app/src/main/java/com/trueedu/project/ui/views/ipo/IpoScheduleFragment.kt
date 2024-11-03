package com.trueedu.project.ui.views.ipo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.trueedu.project.model.dto.rank.IpoScheduleDetail
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TrueText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IpoScheduleFragment: BaseFragment() {
    companion object {
        fun show(
            fragmentManager: FragmentManager
        ): IpoScheduleFragment {
            val fragment = IpoScheduleFragment()
            fragment.show(fragmentManager, "ipo")
            return fragment
        }
    }

    private val vm by viewModels<IpoScheduleViewModel>()

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = { BackTitleTopBar("공모주 일정", ::dismissAllowingStateLoss) },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (vm.loading.value) {
                    LoadingView()
                }
                val state = rememberLazyListState()
                LazyColumn(
                    state = state,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val items = vm.ipoSchedule.value?.ipoScheduleDetail ?: emptyList()
                    itemsIndexed(items, key = { _, item -> item.code }) { i, item ->
                        IpoItem(item)
                    }
                }
            }
        }
    }
}

@Composable
fun IpoItem(item: IpoScheduleDetail) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        TrueText(
            s = item.nameKr,
            fontSize = 12,
            color = MaterialTheme.colorScheme.primary,
        )
        Margin(4)
        TrueText(
            s = item.subscriptionPeriod,
            fontSize = 12,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
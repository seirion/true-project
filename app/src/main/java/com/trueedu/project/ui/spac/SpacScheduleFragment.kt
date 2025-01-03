package com.trueedu.project.ui.spac

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.trueedu.project.data.firebase.SpacStatusManager
import com.trueedu.project.model.dto.firebase.SpacSchedule
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.utils.formatter.dateFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SpacScheduleFragment: BaseFragment() {
    companion object {
        fun show(
            fragmentManager: FragmentManager
        ): SpacScheduleFragment {
            val fragment = SpacScheduleFragment()
            fragment.show(fragmentManager, "spac-schedule")
            return fragment
        }
    }

    @Inject
    lateinit var spacStatusManager: SpacStatusManager

    private val loading = mutableStateOf(false)
    // key - yyyyMMdd
    private val list = mutableStateListOf<Pair<String, SpacSchedule>>()

    override fun init() {
        super.init()

        lifecycleScope.launch {
            list.addAll(
                spacStatusManager.loadSpacSchedule()
                    .map { it.key to it.value }
                    .sortedBy { it.first }
            )
            MainScope().launch {
                loading.value = false
            }
        }
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = { BackTitleTopBar("스팩 일정", ::dismissAllowingStateLoss) },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->

            if (loading.value) {
                LoadingView()
                return@Scaffold
            }

            val state = rememberScrollState()
            Column(
                modifier = Modifier.padding(innerPadding)
                    .verticalScroll(state)
            ) {
                list.map { (date, schedule) ->
                    val dateString = dateFormat(date)
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 8.dp)
                    ) {
                        TrueText(
                            s = dateString,
                            fontSize = 15,
                            fontWeight = FontWeight.W400,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        val s = "${schedule.nameKr}\n${schedule.note}"
                        TrueText(
                            s = s,
                            fontSize = 15,
                            fontWeight = FontWeight.W400,
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = Int.MAX_VALUE,
                        )
                    }
                }
            }
        }
    }
}
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.data.firebase.SpacStatusManager
import com.trueedu.project.model.dto.firebase.SpacSchedule
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.DividerHorizontal
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.utils.formatter.dateFormat
import com.trueedu.project.utils.yyyyMMdd
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
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
    lateinit var googleAccount: GoogleAccount
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
            loading.value = false
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
                    val dateString = dateFormat(date.take(8))
                    val isPast = LocalDate.now().yyyyMMdd().let { now ->
                        date <= now
                    }
                    val bgColor = if (isPast) {
                        MaterialTheme.colorScheme.surfaceDim.copy(
                            alpha = 0.4f
                        )
                    } else {
                        Color.Transparent
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                            .background(color = bgColor)
                            .padding(vertical = 8.dp, horizontal = 8.dp)
                    ) {
                        TrueText(
                            s = dateString,
                            fontSize = 15,
                            fontWeight = FontWeight.W400,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            TrueText(
                                s = schedule.nameKr,
                                fontSize = 15,
                                fontWeight = FontWeight.W500,
                                textAlign = TextAlign.End,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            TrueText(
                                s = schedule.note,
                                fontSize = 14,
                                fontWeight = FontWeight.W400,
                                textAlign = TextAlign.End,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = Int.MAX_VALUE,
                            )
                        }
                    }
                    DividerHorizontal()
                }
            }
        }
    }
}
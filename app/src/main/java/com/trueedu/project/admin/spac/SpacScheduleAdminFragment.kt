package com.trueedu.project.admin.spac

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.firebase.SpacStatusManager
import com.trueedu.project.model.dto.firebase.SpacSchedule
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BottomBar
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TouchIcon24
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.utils.defaultTextColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SpacScheduleAdminFragment: BaseFragment() {
    companion object {
        fun show(
            fragmentManager: FragmentManager
        ): SpacScheduleAdminFragment {
            return SpacScheduleAdminFragment().also {
                it.show(fragmentManager, "spac_schedule_admin")
            }
        }

        private val TAG = SpacScheduleAdminFragment::class.java.simpleName
    }

    @Inject
    lateinit var stockPool: StockPool
    @Inject
    lateinit var spacStatusManager: SpacStatusManager

    private val loading = mutableStateOf(false)
    // key - yyyyMMdd
    private val list = mutableStateListOf<Pair<String, SpacSchedule>>()

    override fun init() {
        lifecycleScope.launch {
            list.addAll(
                spacStatusManager.loadSpacSchedule(true)
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
            topBar = {
                BackTitleTopBar(
                    title = "스팩 일정 관리",
                    onBack = ::dismissAllowingStateLoss,
                )
            },
            bottomBar = {
                BottomBar("저장", true, ::onSave)
            },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            if (loading.value) {
                LoadingView()
                return@Scaffold
            }

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(bottom = 24.dp)
                    .verticalScroll(scrollState)
            ) {
                list.forEachIndexed { index, (date, schedule) ->
                    SpacScheduleItem(index, date, schedule, ::onUpdate) {
                        list.removeAt(index)
                    }
                }
                AddScheduleView {
                    list.add("" to SpacSchedule("", ""))
                }
            }
        }
    }

    private fun onUpdate(index: Int, date: String, schedule: SpacSchedule) {
        Log.d(TAG, "onUpdate: $date - $schedule")
        list[index] = date to schedule
    }

    private fun onSave() {
        MainScope().launch {
            loading.value = true
            spacStatusManager.writeSpacSchedule(
                list = list.toList(),
                onSuccess = {
                    Toast.makeText(requireContext(), "저장 완료", Toast.LENGTH_SHORT).show()
                },
                onFail = {
                    Toast.makeText(requireContext(), "저장 실패 !!", Toast.LENGTH_SHORT).show()
                }
            )
            MainScope().launch {
                loading.value = false
                dismissAllowingStateLoss()
            }
        }
    }
}

@Composable
private fun SpacScheduleItem(
    index: Int,
    date: String,
    schedule: SpacSchedule,
    onUpdate: (Int, String, SpacSchedule) -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = date,
                onValueChange = {
                    if (it != date) onUpdate(index, it, schedule)
                },
                label = {
                    TrueText(
                        s = "날짜(yyyyMMdd)",
                        fontSize = 14,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                modifier = Modifier.width(160.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                maxLines = 1,
                colors = defaultTextColors(),
            )
            TouchIcon24(
                icon = Icons.Filled.RemoveCircle,
                tint = MaterialTheme.colorScheme.error,
                onClick = onDelete,
            )
        }
        Margin(8)
        OutlinedTextField(
            value = schedule.nameKr,
            onValueChange = {
                val nameKr = it.take(20)
                onUpdate(index, date, schedule.copy(nameKr = nameKr))
            },
            label = {
                TrueText(
                    s = "종목 이름",
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.secondary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            maxLines = 1,
            colors = defaultTextColors(),
        )
        Margin(8)
        OutlinedTextField(
            value = schedule.note,
            onValueChange = {
                onUpdate(index, date, schedule.copy(note = it))
            },
            label = {
                TrueText(
                    s = "내용",
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.secondary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = defaultTextColors(),
        )
        Margin(8)
    }
}

@Preview(showBackground = true)
@Composable
private fun AddScheduleView(
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Outlined.Add,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "checked"
        )
        Margin(8)
        TrueText(
            s = "추가",
            fontSize = 16,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

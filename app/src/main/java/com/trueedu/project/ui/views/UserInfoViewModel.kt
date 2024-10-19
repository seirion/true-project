package com.trueedu.project.ui.views

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.model.local.UserKey
import com.trueedu.project.repository.local.Local
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    private val local: Local,
    private val trueAnalytics: TrueAnalytics,
): ViewModel() {

    companion object {
        private val TAG = UserInfoViewModel::class.java.simpleName
    }

    val userKeys = mutableStateOf<List<UserKey>>(emptyList())

    val selected = mutableIntStateOf(0)

    init {
        // 마지막 item 이 현재 선택된 item 이므로 역순으로 보여줌
        userKeys.value = local.getUserKeys().reversed()
        viewModelScope.launch {
        }
    }
}

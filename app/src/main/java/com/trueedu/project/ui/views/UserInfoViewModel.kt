package com.trueedu.project.ui.views

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.model.local.UserKey
import com.trueedu.project.repository.local.Local
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    private val local: Local,
    private val tokenKeyManager: TokenKeyManager,
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

            // 새로운 키, 계좌가 추가되었다는 것을 직접 알 수 없고
            // userKey 값이 변경될 때, 체크해서 추가 여부를 판단할 수 있음
            snapshotFlow { tokenKeyManager.userKey.value }
                .filterNotNull()
                .collect { userKey ->
                    if (userKeys.value.any { it.accountNum == userKey.accountNum }) return@collect

                    // 새로운 키가 추가된 경우 가장 앞에 추가하기
                    userKeys.value = listOf(userKey) + userKeys.value
                    selected.intValue = 0
                }
        }
    }

    fun onSelected(index: Int) {
        selected.intValue = index
        val item = userKeys.value[index]
        tokenKeyManager.addUserKey(item)
    }

    fun delete(accountNum: String) {
        tokenKeyManager.deleteUserKey(accountNum)

        val newList = userKeys.value.filter { it.accountNum != accountNum }
        userKeys.value = newList

        tokenKeyManager.userKey.value?.let { current ->
            selected.intValue =
                userKeys.value.indexOfFirst { current.accountNum == it.accountNum }
        }
    }
}

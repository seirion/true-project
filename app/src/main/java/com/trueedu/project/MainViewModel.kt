package com.trueedu.project

import androidx.lifecycle.ViewModel
import com.trueedu.project.repository.local.Local
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val local: Local,
): ViewModel() {

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }

    fun init() {
    }
}

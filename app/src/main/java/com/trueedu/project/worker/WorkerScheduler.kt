package com.trueedu.project.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

private const val WORK_NAME = "daily-order-task"

fun initWorker(context: Context) {
    Log.d("WorkScheduler", "initWorker(): ${Date()}")
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    // 현재 시간 가져오기
    val calendar = Calendar.getInstance()
    // 다음 실행 시간을 오전 8:50으로 설정
    calendar.apply {
        set(Calendar.HOUR_OF_DAY, 8)
        set(Calendar.MINUTE, 50)
        set(Calendar.SECOND, 0)
    }

    // 만약 현재 시간이 8:50을 지났다면 다음 날로 설정
    if (calendar.timeInMillis <= System.currentTimeMillis()) {
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }

    // 초기 지연 시간 계산 (다음 8:50까지의 시간)
    val initialDelay = calendar.timeInMillis - System.currentTimeMillis()

    // WorkRequest 설정
    val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyWorker>(
        15, TimeUnit.MINUTES,
    ) // 24 시간 마다 반복
        .setConstraints(constraints)
        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS) // 초기 지연
        .build()

    // WorkManager 에 작업 등록
    WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
}

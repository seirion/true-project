package com.trueedu.project.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.trueedu.project.worker.DailyAlarmManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val dailyAlarmManager = DailyAlarmManager(context)
            dailyAlarmManager.scheduleDailyTask()
        }
    }
}

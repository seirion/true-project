package com.trueedu.project.worker

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

class DailyAlarmManager(private val context: Context) {

    companion object {
        private val TAG = DailyAlarmManager::class.java.simpleName
        private const val ALARM_REQUEST_CODE = 3423325
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleDailyTask() {
    }

    private fun checkAlarmPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).also {
                    context.startActivity(it)
                }
                return false
            }
        }
        return true
    }
}

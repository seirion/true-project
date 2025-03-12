package com.trueedu.project.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.trueedu.project.broadcast.DailyOrderTaskReceiver
import java.util.Calendar
import android.util.Log

class DailyAlarmManager(private val context: Context) {

    companion object {
        private val TAG = DailyAlarmManager::class.java.simpleName
        private const val ALARM_REQUEST_CODE = 3423325
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleDailyTask() {
        Log.d(TAG, "scheduleDailyTask() : ${isAlarmSet()}")
        if (!checkAlarmPermission()) {
            Log.d(TAG, "Not Alarm Permission")
            return
        }

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 50)
            set(Calendar.SECOND, 0)

            // 현재 시간이 8:50을 지났다면 다음날로 설정
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val intent = Intent(context, DailyOrderTaskReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 매일 반복되는 알람 설정
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 2000L, //calendar.timeInMillis,
            AlarmManager.INTERVAL_FIFTEEN_MINUTES,
            pendingIntent
        )
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

    private fun isAlarmSet(): Boolean {
        val intent = Intent(context, DailyOrderTaskReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE // 이미 존재하는지만 확인
        )
        return pendingIntent != null
    }

    // 알람 취소 메소드 추가
    private fun cancelAlarm() {
        val intent = Intent(context, DailyOrderTaskReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            Log.d("DailyAlarmManager", "Alarm cancelled")
        }
    }
}

package com.trueedu.project.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.trueedu.project.broadcast.DailyOrderTaskReceiver
import android.util.Log
import java.time.ZoneId
import java.time.ZonedDateTime

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

        if (isAlarmSet()) cancelAlarm()

        // 한국 시간으로 오전 8시 50분 설정
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))

        var scheduledTime = now.withHour(8)
            .withMinute(50)
            .withSecond(0)

        // 현재 시간이 이미 8:50을 지났다면 다음날로 설정
        if (scheduledTime.isBefore(now)) {
            scheduledTime = scheduledTime.plusDays(1)
        }
        Log.d(TAG, "next schedule: $scheduledTime")

        // AlarmManager 에서 사용하기 위해 밀리초로 변환
        val triggerTimeMillis = scheduledTime.toInstant().toEpochMilli()

        val intent = Intent(context, DailyOrderTaskReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerTimeMillis, pendingIntent),
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

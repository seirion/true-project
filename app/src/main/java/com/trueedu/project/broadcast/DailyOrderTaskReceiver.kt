package com.trueedu.project.broadcast

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.trueedu.project.MainActivity
import com.trueedu.project.R
import com.trueedu.project.repository.local.Local
import com.trueedu.project.worker.DailyAlarmManager
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class DailyOrderTaskReceiver : BroadcastReceiver() {
    companion object {
        private val TAG = DailyOrderTaskReceiver::class.java.simpleName
    }

    @Inject
    lateinit var local: Local

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive()")
        performDailyTask()
        createNotification(context)

        Log.d(TAG, "schedule next task")
        DailyAlarmManager(context).scheduleDailyTask()
        /*
        // FIXME: SecurityException 이 나와서 일단 막아둠
        val wakeLock = (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DailyTask:WakeLock")

        wakeLock.acquire(1 * 60 * 1000L) // 1분

        try {
            performDailyTask()
        } finally {
            wakeLock.release()
        }
         */
    }

    private fun performDailyTask() {
        // 실행할 작업 구현
        Log.d(TAG, "performDailyTask: ${Date()}")
        val currentString = local.orderResult
        local.orderResult = "$currentString\n${Date()}"
    }

    private fun createNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "daily_task"
        val channel = NotificationChannel(
            channelId,
            "Daily Task",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle("Daily Task Completed")
            .setContentText("Task completed at ${Date()}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}

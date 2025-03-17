package com.trueedu.project.broadcast

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.trueedu.project.MainActivity
import com.trueedu.project.R
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.model.event.TokenIssueFail
import com.trueedu.project.model.event.TokenIssued
import com.trueedu.project.model.event.TokenOk
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.OrderRemote
import com.trueedu.project.worker.DailyAlarmManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@AndroidEntryPoint
class DailyOrderTaskReceiver : BroadcastReceiver() {
    companion object {
        private val TAG = DailyOrderTaskReceiver::class.java.simpleName
    }

    @Inject
    lateinit var local: Local
    @Inject
    lateinit var tokenKeyManager: TokenKeyManager
    @Inject
    lateinit var orderRemote: OrderRemote

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
        val orders = local.getOrderSchedule()

        if (orders.isEmpty()) {
            Log.d(TAG, "empty orders")
            local.orderResult = "예약 주문이 없습니다"
            return
        }

        val userKey = tokenKeyManager.userKey.value
        if (userKey == null) {
            Log.d(TAG, "empty orders")
            local.orderResult = "appkey 가 존재하지 않아 주문을 수행할 수 없습니다"
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val tokenEvent = tokenKeyManager.observeTokenKeyEvent().first()
            if (tokenEvent is TokenIssueFail) {
                Log.d(TAG, "token issue failed")
                local.orderResult = "토큰 재발급에 실패했습니다"
                return@launch
            }

            val order = orders.first()
            if (order.isBuy) {
                orderRemote.buy(
                    accountNum = userKey.accountNum ?: "",
                    code = order.code,
                    price = order.price.toString(),
                    quantity = order.quantity.toString(),
                )
            } else { // sell
                orderRemote.sell(
                    accountNum = userKey.accountNum ?: "",
                    code = order.code,
                    price = order.price.toString(),
                    quantity = order.quantity.toString(),
                )
            }
                .catch {
                    local.orderResult = "${Date()}\n    failed: ${it.message}}"
                }
                .collect {
                    local.orderResult = "${Date()}\n    ok: ${it.msg1}\n    ${it.orderDetail?.orderTime}}"
                }
        }
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

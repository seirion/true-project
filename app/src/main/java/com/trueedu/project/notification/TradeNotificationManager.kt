package com.trueedu.project.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.trueedu.project.MainActivity
import com.trueedu.project.R
import com.trueedu.project.model.ws.RealTimeTrade
import com.trueedu.project.model.ws.TradeNotification
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TradeNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val CHANNEL_ID = "trade_realtime"
        const val CHANNEL_NAME = "실시간 체결 알림"
        const val CHANNEL_DESCRIPTION = "웹소켓으로 수신된 실시간 체결 이벤트를 알림으로 표시합니다"
    }

    private val notificationIdCounter = AtomicInteger(1000)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * 내 주문 체결 통보(H0STCNI0)를 notification으로 표시
     */
    fun showOrderExecutionNotification(notification: TradeNotification, stockName: String? = null) {
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) return

        val displayName = if (!stockName.isNullOrEmpty()) stockName else notification.code
        val buySell = if (notification.isBuy) "매수" else "매도"
        val title = "[$buySell 체결] $displayName"
        val body = buildString {
            append("${formatQty(notification.execQty)}주")
            append(" @")
            append(formatPrice(notification.execPrice))
            append("  |  ")
            append(formatTime(notification.execTime))
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("code", notification.code)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notification.code.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_trade)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationIdCounter.getAndIncrement(), notif)
    }

    /**
     * 실시간 체결 이벤트를 notification으로 표시 (시세 체결용 - 현재 미사용)
     */
    fun showTradeNotification(trade: RealTimeTrade, stockName: String? = null) {
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) return

        val displayName = if (!stockName.isNullOrEmpty()) stockName else trade.code
        val sign = if (trade.delta >= 0) "▲" else "▼"
        val title = "$displayName $sign ${formatPrice(trade.price)}"
        val body = buildString {
            append("등락 ${formatRate(trade.rate)}%")
            append("  |  ")
            append("거래량 ${formatVolume(trade.volume)}")
            append("  |  ")
            append("체결 ${formatTime(trade.datetime)}")
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("code", trade.code)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            trade.code.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_trade)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationIdCounter.getAndIncrement(), notification)
    }

    private fun formatPrice(price: Double): String {
        return "%,.0f원".format(price)
    }

    private fun formatQty(qty: Double): String {
        return "%,.0f".format(qty)
    }

    private fun formatRate(rate: Double): String {
        return "%.2f".format(rate)
    }

    private fun formatVolume(volume: Double): String {
        return "%,.0f".format(volume)
    }

    /**
     * HHmmss → HH:mm:ss
     */
    private fun formatTime(time: String): String {
        if (time.length < 6) return time
        return "${time.substring(0, 2)}:${time.substring(2, 4)}:${time.substring(4, 6)}"
    }
}

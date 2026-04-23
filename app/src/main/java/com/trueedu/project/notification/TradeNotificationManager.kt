package com.trueedu.project.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.trueedu.project.MainActivity
import com.trueedu.project.R
import com.trueedu.project.model.ws.RealTimeTrade
import com.trueedu.project.model.ws.TradeNotification
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

        private const val SHORTCUT_ID_PREFIX = "trade_"
        private const val PROFILE_IMAGE_SIZE = 96
        private const val CORNER_RADIUS_DP = 48f // 원형에 가깝게
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
     * 내 주문 체결 통보(H0STCNI0)를 notification으로 표시.
     * profileImageUrl이 있으면 MessagingStyle + Person으로 프로필 이미지를 헤즈업 팝업 좌측에 표시.
     */
    suspend fun showOrderExecutionNotification(
        notification: TradeNotification,
        stockName: String? = null,
        profileImageUrl: String? = null,
    ) = withContext(Dispatchers.IO) {
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) return@withContext

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

        val pendingIntent = makePendingIntent(notification.code)

        val notif = if (profileImageUrl != null) {
            buildMessagingNotification(
                title = title,
                message = body,
                profileImageUrl = profileImageUrl,
                shortcutId = SHORTCUT_ID_PREFIX + notification.code,
                pendingIntent = pendingIntent,
                priority = NotificationCompat.PRIORITY_HIGH,
            )
        } else {
            buildSimpleNotification(
                title = title,
                body = body,
                pendingIntent = pendingIntent,
                priority = NotificationCompat.PRIORITY_HIGH,
            )
        }

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

        val pendingIntent = makePendingIntent(trade.code)

        val notification = buildSimpleNotification(
            title = title,
            body = body,
            pendingIntent = pendingIntent,
            priority = NotificationCompat.PRIORITY_DEFAULT,
        )

        notificationManager.notify(notificationIdCounter.getAndIncrement(), notification)
    }

    // ─────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────

    /**
     * MessagingStyle + Person 방식 알림.
     * 헤즈업 팝업 좌측에 profileImage가 원형으로 표시된다.
     */
    private suspend fun buildMessagingNotification(
        title: String,
        message: String,
        profileImageUrl: String,
        shortcutId: String,
        pendingIntent: PendingIntent,
        priority: Int,
    ): Notification {
        val profileBitmap = loadProfileBitmap(profileImageUrl)
        val personIcon = profileBitmap?.let { IconCompat.createWithBitmap(it) }

        val sender = Person.Builder()
            .setName(title)
            .apply { personIcon?.let { setIcon(it) } }
            .build()

        val me = Person.Builder()
            .setName(context.getString(R.string.app_name))
            .setImportant(false)
            .build()

        // Shortcut 등록: MessagingStyle과 함께 쓰면 시스템이 대화형 알림으로 인식
        val shortcutIcon = personIcon ?: IconCompat.createWithResource(context, R.mipmap.ic_launcher)
        val shortcutIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.let { Intent.makeMainActivity(it.component).setPackage(context.packageName) }
            ?: Intent(Intent.ACTION_MAIN).apply {
                setPackage(context.packageName)
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
        shortcutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val shortcut = ShortcutInfoCompat.Builder(context, shortcutId)
            .setLongLived(true)
            .setIntent(shortcutIntent)
            .setShortLabel(title.take(10))
            .setPerson(sender)
            .setIcon(shortcutIcon)
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)

        val messagingStyle = NotificationCompat.MessagingStyle(me)
            .addMessage(message, System.currentTimeMillis(), sender)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_notification_trade)
            .setStyle(messagingStyle)
            .setShortcutId(shortcutId)
            .setAutoCancel(true)
            .setPriority(priority)
            .build()
    }

    /** 기존 방식 단순 알림 */
    private fun buildSimpleNotification(
        title: String,
        body: String,
        pendingIntent: PendingIntent,
        priority: Int,
    ): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_trade)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun makePendingIntent(code: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("code", code)
        }
        return PendingIntent.getActivity(
            context,
            code.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /** Coil로 URL 로드 → 원형 크롭 비트맵 반환 */
    private suspend fun loadProfileBitmap(url: String): Bitmap? {
        return try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()
            val result = (loader.execute(request) as? SuccessResult)?.drawable
            result?.toBitmap()?.let { bmp ->
                val squared = cropToSquare(bmp)
                val scaled = scaleBitmap(squared, PROFILE_IMAGE_SIZE)
                roundBitmap(scaled, context.resources.displayMetrics.density * CORNER_RADIUS_DP)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun cropToSquare(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        if (bitmap.width == size && bitmap.height == size) return bitmap
        val x = if (bitmap.width > bitmap.height) (bitmap.width - size) / 2 else 0
        return Bitmap.createBitmap(bitmap, x, 0, size, size)
    }

    private fun scaleBitmap(bitmap: Bitmap, size: Int): Bitmap {
        if (bitmap.width == size && bitmap.height == size) return bitmap
        return Bitmap.createScaledBitmap(bitmap, size, size, true)
    }

    private fun roundBitmap(bitmap: Bitmap, radiusPx: Float): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
            shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        canvas.drawRoundRect(
            0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(),
            radiusPx, radiusPx, paint
        )
        return output
    }

    // ─────────────────────────────────────────────────────────
    // Formatters
    // ─────────────────────────────────────────────────────────

    private fun formatPrice(price: Double) = "%,.0f원".format(price)
    private fun formatQty(qty: Double) = "%,.0f".format(qty)
    private fun formatRate(rate: Double) = "%.2f".format(rate)
    private fun formatVolume(volume: Double) = "%,.0f".format(volume)

    /** HHmmss → HH:mm:ss */
    private fun formatTime(time: String): String {
        if (time.length < 6) return time
        return "${time.substring(0, 2)}:${time.substring(2, 4)}:${time.substring(4, 6)}"
    }
}

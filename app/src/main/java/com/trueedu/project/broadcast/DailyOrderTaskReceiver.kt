package com.trueedu.project.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.trueedu.project.repository.local.Local
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
        /*
        // FIXME: SecurityException 이 나와서 일단 막아둠
        val wakeLock = (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DailyTask:WakeLock")

        wakeLock.acquire(1 * 60 * 1000L) // 1분

        try {
            // 여기에 실행할 메소드 구현
            performDailyTask()
        } finally {
            wakeLock.release()
        }
         */
    }

    private fun performDailyTask() {
        // 실행할 작업 구현
        Log.d(TAG, "performDailyTask: ${Date()}")
    }
}

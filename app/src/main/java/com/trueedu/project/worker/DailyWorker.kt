package com.trueedu.project.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.model.dto.order.OrderResponse
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.OrderRemote
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import java.util.Date

@HiltWorker
class DailyWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val local: Local,
    private val keyTokenKeyManager: TokenKeyManager,
    private val orderRemote: OrderRemote,
) : CoroutineWorker(context, workerParams) {

    companion object {
        private val TAG = DailyWorker::class.java.simpleName
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "doWork: ${Date()}")
        try {
            val list = local.getOrderSchedule()
            if (list.isEmpty()) {
                Result.success()
            } else {
                val a = list.first()
                val userKey = keyTokenKeyManager.userKey.value ?: return@withContext Result.failure()
                val b: OrderResponse = orderRemote.sell(
                    accountNum = userKey.accountNum!!,
                    code = a.code,
                    price = a.price.toString(),
                    quantity = a.quantity.toString(),
                ).single()
                val json = kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                }
                val jsonResult: String = json.encodeToString(b)
                local.orderResult = jsonResult
                Result.success()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

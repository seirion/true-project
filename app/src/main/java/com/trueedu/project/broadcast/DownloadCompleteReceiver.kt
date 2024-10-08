package com.trueedu.project.broadcast

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.trueedu.project.utils.StockInfoDownloader
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DownloadCompleteReceiver : BroadcastReceiver() {
    @Inject
    lateinit var stockInfoDownloader: StockInfoDownloader

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("stock", "onReceive download complete0")
        if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            Log.d("stock", "onReceive download complete")
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadId != -1L) {
                stockInfoDownloader.pushDownloadIntent(downloadId)
            }
        }
    }
}

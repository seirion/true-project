package com.trueedu.project.utils

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

private val kospi = "kospi"
private val kosdaq = "kosdaq"

@Singleton
class StockInfoDownloader @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    companion object {
        private val TAG = StockInfoDownloader::class.java.simpleName
    }

    private val downloadEvent = MutableSharedFlow<Long>(1)

    fun pushDownloadIntent(downloadId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "download completed signal: $downloadId")
            downloadEvent.emit(downloadId)
        }
    }

    fun begin() {
        CoroutineScope(Dispatchers.IO).launch {
            listOf(kospi, kosdaq).forEach {
                val url = download(it) ?: return@forEach
                val unzipped = unzipFile(url) ?: return@forEach
                readUnzippedFile(unzipped)
            }
        }
    }

    @SuppressLint("Range")
    suspend fun download(exchange: String): String? {
        Log.d(TAG, "begin download(): $exchange")

        val url =  "https://new.real.download.dws.co.kr/common/master/${exchange}_code.mst.zip"
        val fileName = "${exchange}_code.mst.zip"

        val context = context.applicationContext
        val request = DownloadManager.Request(Uri.parse(url))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            .setTitle("$fileName 다운로드")
            .setDescription("Downloading...")

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        downloadEvent.firstOrNull { it == downloadId } ?: throw RuntimeException("Download Failed: $exchange")

        val query = DownloadManager.Query().setFilterById(downloadId)

        downloadManager.query(query).use { cursor ->
            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    // 다운로드 성공
                    Log.d(TAG, "download completed: $exchange")
                    return cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                } else {
                    // 다운로드 실패
                    throw IOException("Download Failed: $exchange")
                }
            } else {
                throw IOException("Download Failed: $exchange")
            }
        }
    }

    private fun unzipFile(uriStr: String): String? {
        Log.d(TAG, "unzip $uriStr")
        val uri = Uri.parse(uriStr)
        val contentResolver = context.contentResolver

        var unzippedFile: String? = null

        contentResolver.openInputStream(uri)?.use { inputStream ->
            try {
                ZipInputStream(inputStream).use { zis ->
                    var entry: ZipEntry? = zis.nextEntry
                    while (entry != null) {
                        val path = uri.path?.let { File(it).parent }
                        //val currentFile = File(Environment.DIRECTORY_DOWNLOADS, entry.name)
                        val currentFile = File(path, entry.name)
                        Log.d(TAG, "currentFile: $currentFile")
                        if (entry.isDirectory) {
                            currentFile.mkdirs()
                        } else {
                            currentFile.parentFile?.mkdirs()
                            BufferedOutputStream(FileOutputStream(currentFile)).use { bos ->
                                val buffer = ByteArray(1024)
                                var count: Int
                                while (zis.read(buffer).also { count = it } != -1) {
                                    bos.write(buffer, 0, count)
                                }
                            }
                            unzippedFile = currentFile.path
                        }
                        entry = zis.nextEntry
                    }
                    Log.d(TAG, "unzip completed")
                }
            } catch (e: IOException) {
                Log.d(TAG, "unzip failed: $e")
                e.printStackTrace()
            } finally {
                deleteFile(uri)
            }
        }
        return unzippedFile
    }

    private fun deleteFile(uri: Uri) {
        val file = uri.path?.let { File(it) } ?: return
        try {
            file.delete()
            Log.d(TAG, "file deleted")
        } catch (e: IOException) {
            Log.d(TAG, "file not deleted : $e")
        }
    }

    private fun readUnzippedFile(url: String) {
        Log.d(TAG, "read file: $url")
        val unzippedFile = File(url)
        try {
            BufferedReader(InputStreamReader(FileInputStream(unzippedFile), Charset.forName("CP949"))).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    //Log.d(TAG, "> $line")
                }
            }
        } catch (e: IOException) {
            Log.d(TAG, "file open failed: $e")
            e.printStackTrace()
        }
    }
}

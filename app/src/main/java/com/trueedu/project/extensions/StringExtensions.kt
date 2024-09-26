package com.trueedu.project.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context


// 클립보드에 저장하기
fun String.copyToClipboard(context: Context, label: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText(label, this)
    clipboardManager.setPrimaryClip(clipData)
}

// 붙여넣기
fun getClipboardText(context: Context): String? {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    val clipData = clipboardManager.primaryClip
    if (clipData != null && clipData.itemCount > 0) {
        return clipData.getItemAt(0).text?.toString()
    }
    return null
}


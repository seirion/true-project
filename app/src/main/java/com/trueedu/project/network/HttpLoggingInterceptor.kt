package com.trueedu.project.network

import com.orhanobut.logger.Logger
import com.treuedu.project.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject

fun OkHttpClient.Builder.addHttpLoggingInterceptor() = addNetworkInterceptor(HttpLoggingInterceptor {
    val tag = "OkHttp"
    try {
        JSONObject(it)
        Logger.t(tag).json(it)
    } catch (e: JSONException) {
        Logger.t(tag).i(it)
    }
}.apply {
    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
    else HttpLoggingInterceptor.Level.NONE
})

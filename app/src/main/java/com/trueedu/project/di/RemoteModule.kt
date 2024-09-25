package com.trueedu.project.di

import android.os.Build
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.trueedu.project.repository.remote.AuthRemote
import com.trueedu.project.repository.remote.AuthRemoteImpl
import com.trueedu.project.repository.remote.service.AuthService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RemoteModule {
    @Provides
    @Singleton
    @NormalService
    fun providesAuthService(retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }

    @Provides
    @Singleton
    @TokenRefreshService
    fun providesTokenRefreshService(
        @BaseUrl baseUrl: String,
        httpLoggingInterceptor: HttpLoggingInterceptor,
        chuckerInterceptor: ChuckerInterceptor,
        @AppVersion appVersion: String?,
        @AppVersionCode appVersionCode: Long
    ): AuthService {
        val contentType = "application/json".toMediaType()
        val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = false
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(httpLoggingInterceptor)
                    .addInterceptor(chuckerInterceptor)
                    .addInterceptor { chain ->
                        chain.request().newBuilder()
                            //.headers(getApiHeaders(appVersion = appVersion, appVersionCode = appVersionCode))
                            .build()
                            .let {
                                chain.proceed(it)
                            }
                    }
                    .build()
            )
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(AuthService::class.java)
    }

    @Singleton
    @Provides
    fun providesAuthRemote(
        @NormalService
        authService: AuthService
    ): AuthRemote = AuthRemoteImpl(authService = authService)
}

fun getApiHeaders(accessToken: String? = null, appVersion: String?, appVersionCode: Long): Headers {
    val headers = Headers.Builder()

    accessToken?.takeIf {
        checkHeaderValid(it) && it.isNotBlank()
    }?.let {
        headers.set("Authorization", "Bearer $it")
    }

    return headers
        .set(
            "X-USERAGENT",
            "$appVersion" +
                    "/$appVersionCode Android" +
                    "/${Build.VERSION.SDK_INT}" +
                    "/${Build.MANUFACTURER}-${Build.MODEL.trim().replace(" ", "")}"
        )
        .build()
}

private fun checkHeaderValid(value: String): Boolean {
    var result = true
    for (i in value.indices) {
        val c = value[i]
        if (!(c == '\t' || c in '\u0020'..'\u007e')) {
            result = false
            break
        }
    }
    return result
}

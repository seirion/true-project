package com.trueedu.project.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.orhanobut.logger.Logger
import com.trueedu.project.BuildConfig
import com.trueedu.project.network.TokenAuthenticator
import com.trueedu.project.network.TokenInterceptor
import com.trueedu.project.network.addHttpLoggingInterceptor
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.service.AuthService
import com.trueedu.project.repository.remote.service.MyWebSocketService
import com.trueedu.project.repository.remote.service.WebSocketService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {
    private const val TAG = "OkHttp"
    private val connectTimeout = 20.seconds
    private val callTimeout = 20.seconds
    private val writeTimeout = 20.seconds
    private val readTimeout = 20.seconds

    @Provides
    @BaseUrl
    fun providesBaseUrl(): String {
        return if (BuildConfig.DEBUG) {
            "https://openapi.koreainvestment.com:9443"
            //"https://openapivts.koreainvestment.com:29443"
        } else {
            "https://openapi.koreainvestment.com:9443"
        }
    }

    @Provides
    @WebSocketUrl
    fun providesWebsocketUrl(): String {
        return "ws://ops.koreainvestment.com:21000"
    }

    @Provides
    @Singleton
    fun providesLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor(PrettyPrintLogger()).apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun providesChuckerInterceptor(@ApplicationContext context: Context): ChuckerInterceptor {
        return ChuckerInterceptor.Builder(context)
            .alwaysReadResponseBody(true)
            .collector(
                ChuckerCollector(
                    context,
                    showNotification = true,
                    retentionPeriod = RetentionManager.Period.ONE_WEEK
                )
            )
            .build()
    }

    @Provides
    @Singleton
    fun providesFlipperOkhttpInterceptor(networkFlipperPlugin: NetworkFlipperPlugin): FlipperOkhttpInterceptor {
        return FlipperOkhttpInterceptor(networkFlipperPlugin)
    }

    @Provides
    @Singleton
    fun providesTokenAuthenticator(
        @ApplicationContext context: Context,
        @TokenRefreshService authService: AuthService,
        local: Local,
    ): TokenAuthenticator {
        return TokenAuthenticator(
            context = context,
            authService = authService,
            local = local,
        )
    }


    @Provides
    @Singleton
    @KisOkHttp
    fun providesOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        chuckerInterceptor: ChuckerInterceptor,
        tokenInterceptor: TokenInterceptor,
        flipperOkhttpInterceptor: FlipperOkhttpInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .apply {
                if (BuildConfig.DEBUG) {
                    addHttpLoggingInterceptor()
                }
            }
            .addInterceptor(tokenInterceptor)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(chuckerInterceptor)
            .addNetworkInterceptor(flipperOkhttpInterceptor)
            .connectTimeout(connectTimeout.toJavaDuration())
            .callTimeout(callTimeout.toJavaDuration())
            .writeTimeout(writeTimeout.toJavaDuration())
            .readTimeout(readTimeout.toJavaDuration())
            .build()
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    @KisRetrofit
    fun providesRetrofit(
        @BaseUrl baseUrl: String,
        @KisOkHttp okHttpClient: OkHttpClient
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = false
            serializersModule = Json.serializersModule
            explicitNulls = false
        }

        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .addConverterFactory(json.asConverterFactory(contentType))
            .callFactory { okHttpClient.newCall(it) }
            .build()
    }

    private class PrettyPrintLogger : HttpLoggingInterceptor.Logger {
        override fun log(message: String) {
            Logger.t(TAG).run {
                try {
                    val json = Json {
                        prettyPrint = true
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                    json.decodeFromString<JsonElement>(message)
                    json(message)
                } catch (ignore: SerializationException) {
                    i(message)
                } catch (ignore: IllegalArgumentException) {
                    i(message)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    // websocket 관련

    @Provides
    @Singleton
    fun provideWebSocketService(
        @WebSocketUrl webSocketUrl: String,
        @KisOkHttp okHttpClient: OkHttpClient,
    ): WebSocketService {
        return MyWebSocketService(webSocketUrl, okHttpClient)
    }
}

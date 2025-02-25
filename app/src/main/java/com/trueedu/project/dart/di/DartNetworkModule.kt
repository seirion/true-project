package com.trueedu.project.dart.di

import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.trueedu.project.BuildConfig
import com.trueedu.project.di.DartOkHttp
import com.trueedu.project.di.DartRetrofit
import com.trueedu.project.network.addHttpLoggingInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {
    @Provides
    @DartBaseUrl
    fun providesDartBaseUrl(): String {
        return "https://opendart.fss.or.kr/api/"
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    @DartRetrofit
    fun providesDartRetrofit(
        @DartBaseUrl dartBaseUrl: String,
        @DartOkHttp okHttpClient: OkHttpClient
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
            .baseUrl(dartBaseUrl)
            .addConverterFactory(json.asConverterFactory(contentType))
            .callFactory { okHttpClient.newCall(it) }
            .build()
    }

    private val connectTimeout = 30.seconds
    private val callTimeout = 30.seconds
    private val writeTimeout = 30.seconds
    private val readTimeout = 30.seconds

    @Provides
    @Singleton
    @DartOkHttp
    fun providesOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        chuckerInterceptor: ChuckerInterceptor,
        dartTokenInterceptor: DartTokenInterceptor,
        flipperOkhttpInterceptor: FlipperOkhttpInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .apply {
                if (BuildConfig.DEBUG) {
                    addHttpLoggingInterceptor()
                }
            }
            .addInterceptor(dartTokenInterceptor)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(chuckerInterceptor)
            .addNetworkInterceptor(flipperOkhttpInterceptor)
            .connectTimeout(connectTimeout.toJavaDuration())
            .callTimeout(callTimeout.toJavaDuration())
            .writeTimeout(writeTimeout.toJavaDuration())
            .readTimeout(readTimeout.toJavaDuration())
            .build()
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DartBaseUrl

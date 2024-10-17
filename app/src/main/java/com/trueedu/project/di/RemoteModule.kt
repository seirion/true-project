package com.trueedu.project.di

import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.trueedu.project.repository.remote.AccountRemote
import com.trueedu.project.repository.remote.AccountRemoteImpl
import com.trueedu.project.repository.remote.AuthRemote
import com.trueedu.project.repository.remote.AuthRemoteImpl
import com.trueedu.project.repository.remote.PriceRemote
import com.trueedu.project.repository.remote.PriceRemoteImpl
import com.trueedu.project.repository.remote.RankingRemote
import com.trueedu.project.repository.remote.RankingRemoteImpl
import com.trueedu.project.repository.remote.service.AccountService
import com.trueedu.project.repository.remote.service.AuthService
import com.trueedu.project.repository.remote.service.PriceService
import com.trueedu.project.repository.remote.service.RankingService
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
    @NormalService
    fun providesAccountService(retrofit: Retrofit): AccountService {
        return retrofit.create(AccountService::class.java)
    }

    @Provides
    @Singleton
    @NormalService
    fun providesPriceService(retrofit: Retrofit): PriceService {
        return retrofit.create(PriceService::class.java)
    }

    @Provides
    @Singleton
    @NormalService
    fun providesRankingService(retrofit: Retrofit): RankingService {
        return retrofit.create(RankingService::class.java)
    }

    @Provides
    @Singleton
    @TokenRefreshService
    fun providesTokenRefreshService(
        @BaseUrl baseUrl: String,
        httpLoggingInterceptor: HttpLoggingInterceptor,
        chuckerInterceptor: ChuckerInterceptor,
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

    @Singleton
    @Provides
    fun providesAccountRemote(
        @NormalService
        accountService: AccountService
    ): AccountRemote = AccountRemoteImpl(accountService = accountService)

    @Singleton
    @Provides
    fun providesPriceRemote(
        @NormalService
        priceService: PriceService
    ): PriceRemote = PriceRemoteImpl(priceService = priceService)

    @Singleton
    @Provides
    fun providesRankingRemote(
        @NormalService
        rankingService: RankingService
    ): RankingRemote = RankingRemoteImpl(rankingService = rankingService)
}

fun getApiHeaders(
    appKey: String,
    apSecret: String,
    accessToken: String,
): Headers {
    val headers = Headers.Builder()

    listOf(
        "content-type" to "application/json",
        "appkey" to appKey,
        "appsecret" to apSecret,
        "authorization" to "Bearer $accessToken",
    ).forEach { (key, value) ->
        headers[key] = value
    }

    return headers.build()
}

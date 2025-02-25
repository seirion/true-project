package com.trueedu.project.dart.di

import com.trueedu.project.dart.repository.remote.DartRemote
import com.trueedu.project.dart.repository.remote.DartRemoteImpl
import com.trueedu.project.dart.repository.remote.DartService
import com.trueedu.project.di.DartRetrofit
import com.trueedu.project.di.NormalService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DartRemoteModule {
    @Provides
    @Singleton
    @NormalService
    fun providesDartService(@DartRetrofit retrofit: Retrofit): DartService {
        return retrofit.create(DartService::class.java)
    }

    @Singleton
    @Provides
    fun providesDartRemote(
        @NormalService
        dartService: DartService
    ): DartRemote = DartRemoteImpl(dartService = dartService)
}

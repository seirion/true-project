package com.trueedu.project.di

import com.trueedu.project.data.ws.AccountCall
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object WsModuleProvider {
    @Singleton
    @Provides
    fun providesAccountCall(

    ): AccountCall = AccountCallImpl(accountCall = accountCall)
}

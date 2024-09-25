package com.trueedu.project.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppVersion

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppVersionCode

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TokenRefreshService

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NormalService

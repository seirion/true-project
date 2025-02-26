package com.trueedu.project.dart.repository.remote

import com.trueedu.project.dart.model.DartListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.QueryMap

interface DartService {
    @GET("list.json")
    suspend fun list(
        @HeaderMap headers: Map<String, String>,
        @QueryMap queries: Map<String, String>
    ): Response<DartListResponse>
}

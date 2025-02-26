package com.trueedu.project.dart.repository.remote

import com.trueedu.project.dart.model.DartListResponse
import kotlinx.coroutines.flow.Flow

interface DartRemote {
    /**
     * fromDate: yyyyMMdd
     */
    fun list(corpCode: String, fromDate: String): Flow<DartListResponse>
}

package com.trueedu.project.dart.repository.remote

import com.trueedu.project.dart.model.DartListResponse
import kotlinx.coroutines.flow.Flow

interface DartRemote {
    fun list(corpCode: String): Flow<DartListResponse>
}

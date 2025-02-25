package com.trueedu.project.dart.repository.remote

import com.trueedu.project.di.NormalService
import com.trueedu.project.network.apiCallFlow

class DartRemoteImpl(
    @NormalService
    private val dartService: DartService
): DartRemote {
    override fun list(corpCode: String) = apiCallFlow {
        val queries = mapOf(
            "corp_code" to corpCode,
            "bgn_de" to "20250101",
        )
        dartService.list(emptyMap(), queries)
    }
}

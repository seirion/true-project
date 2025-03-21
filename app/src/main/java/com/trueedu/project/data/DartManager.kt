package com.trueedu.project.data

import android.util.Log
import com.trueedu.project.dart.model.DartListItem
import com.trueedu.project.dart.model.DartListResponse
import com.trueedu.project.dart.repository.remote.DartRemote
import com.trueedu.project.data.firebase.FirebaseDartManager
import com.trueedu.project.data.spac.SpacManager
import com.trueedu.project.repository.local.Local
import com.trueedu.project.utils.yyyyMMddHHmm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DartManager @Inject constructor(
    private val local: Local,
    private val dartRemote: DartRemote,
    private val spacManager: SpacManager,
    private val firebaseDartManager: FirebaseDartManager,
) {
    companion object {
        private val TAG = DartManager::class.java.simpleName
    }

    private val items = ConcurrentHashMap<String, List<DartListItem>>()
    private var lastUpdatedAt = 0L // yyyyMMddHHmm (분단위)

    val updateSignal = MutableSharedFlow<Unit>()

    fun init() {
        Log.d(TAG, "init() - ${local.dartApiKey.take(8)}")
        MainScope().launch(Dispatchers.IO) {
            // yyyyMMddHHmm
            val lastUpdatedAtRemote = firebaseDartManager.lastUpdatedAt()
            Log.d(TAG, "lastUpdatedAtRemote: $lastUpdatedAtRemote")
            val now = Date().yyyyMMddHHmm().toLong()
            val hasApiKey = local.dartApiKey.isNotBlank()

            if (hasApiKey && now - lastUpdatedAtRemote > 30) { // 30 minutes
                // 다시 로딩
                while (spacManager.loading.value) {
                    Log.d(TAG, "waiting spacManager")
                    delay(200)
                }
                val list = spacManager.spacList.value
                loadList(list.map { it.code })
                Log.d(TAG, "lastUpdatedAt: $lastUpdatedAt")
            } else {
                lastUpdatedAt = lastUpdatedAtRemote
                firebaseDartManager.loadDartList().forEach {
                    if (it.list?.isNotEmpty() == true) {
                        val code = it.list.first().stockCode
                        items[code] = it.list
                        updateSignal.emit(Unit)
                    }
                }
            }
            Log.d(TAG, "init() completed - ${items.size}")
        }
    }

    fun getSize(): Int {
        return items.size
    }

    fun getListMap(): Map<String, List<DartListItem>> {
        return items
    }

    suspend fun CoroutineScope.loadList(codes: List<String>) {
        if (local.dartApiKey.isBlank()) return

        val fromDate = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd"))

        // 모든 API 호출을 동시에 실행하고 결과를 기다림
        codes.map { code ->
            async {
                val dartInfo = dartCorpMap[code] ?: return@async
                try {
                    val response = dartRemote.list(dartInfo.corpCode, fromDate)
                    response.collect { res ->
                        if (res.list?.isNotEmpty() == true) {
                            Log.d(TAG, "${dartInfo.nameKr} - ${res.list.first().let {"${it.receiptDate} ${it.reportName}"} }")
                            items[code] = res.list
                            updateSignal.emit(Unit)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading dart list for ${dartInfo.nameKr}: ${e.message}")
                }
            }
        }.awaitAll()
        lastUpdatedAt = Date().yyyyMMddHHmm().toLong()

        // 완료 후 firebase 업데이트
        firebaseDartManager.writeDartList(
            items.values.map {
                DartListResponse(status = "", message = "", list = it)
            }
        )
    }

    fun forceLoad() {
        Log.d(TAG, "forceLoad()")
        clear()
        MainScope().launch(Dispatchers.IO) {
            val list = spacManager.spacList.value
            loadList(list.map { it.code })
        }
    }

    fun hasDisclosure(code: String): Boolean {
        return items.containsKey(code)
    }

    private fun clear() {
        items.clear()
        lastUpdatedAt = 0L
    }
}

data class DartCorpCode(
    val corpCode: String,
    val nameKr: String,
    val code: String,
)

private val dartCorpMap = """
01678835 유진스팩9호 442130
01677429 엔에이치스팩27호 440820
01669475 하나금융25호스팩 435620
01675254 엔에이치스팩26호 439410
01674468 엔에이치스팩25호 438580
01677678 교보13호스팩 440790
01656374 신영스팩8호 430220
01689938 신영스팩9호 445970
01696451 대신밸런스제14호스팩 442310
01675227 삼성스팩7호 439250
01674538 엔에이치스팩24호 437780
01689336 비엔케이제1호스팩 445360
01724417 하나29호스팩 454640
01766167 KB제26호스팩 458320
01724338 하나28호스팩 454750
01744703 SK증권제10호스팩 457940
01712616 엔에이치스팩29호 451700
01724709 KB제25호스팩 455250
01791321 하나30호스팩 469880
01785551 KB제27호스팩 464680
01683387 미래에셋드림스팩1호 442900
01751448 대신밸런스제15호스팩 457390
01767494 에이치엠씨제6호스팩 462020
01696275 하나26호스팩 446750
01785056 엔에이치스팩30호 466910
01775952 한국제13호스팩 464440
01760640 교보14호스팩 456490
01792825 대신밸런스제17호스팩 471050
01738483 대신밸런스제16호스팩 457630
01725160 한화플러스제4호스팩 455310
01700374 하나27호스팩 448370
01797440 SK증권제11호스팩 472230
01787072 유진스팩10호 468760
01791561 하나31호스팩 469900
01800285 비엔케이제2호스팩 473370
01717170 상상인제4호스팩 452670
01705625 하이제8호스팩 450050
01700921 삼성스팩8호 448740
01731453 DB금융스팩11호 456440
01725577 SK증권제9호스팩 455910
01796858 신영스팩10호 472220
01792791 IBKS제24호스팩 469480
01692321 미래에셋비전스팩2호 446190
01786514 삼성스팩9호 468510
01781847 교보15호스팩 465320
01816268 KB제29호스팩 478390
01702424 미래에셋비전스팩3호 448830
01818682 한국제15호스팩 479880
01785700 IBKS제23호스팩 467930
01807729 하나33호스팩 475250
01815764 이베스트스팩6호 478110
01671197 SK증권제8호스팩 435870
01845701 교보16호스팩 482520
01798980 유안타제15호스팩 473050
01810796 KB제28호스팩 476470
01814233 미래에셋비전스팩4호 477380
01675421 IBKS제20호스팩 439730
01814589 디비금융스팩12호 477760
01866528 교보17호스팩 489210
01853214 신한제14호스팩 487360
01854408 신한제15호스팩 487830
01847550 하나34호스팩 484130
01807738 하나32호스팩 475240
01866926 KB제31호스팩 492220
01857991 디비금융제13호스팩 489730
01761296 한국제12호스팩 458610
01682740 IBKS제21호스팩 442770
01701328 IBKS제22호스팩 448760
01804476 유안타제16호스팩 474490
01817610 대신밸런스제18호스팩 478780
01801822 SK증권제13호스팩 473950
01814312 한국제14호스팩 477530
01817249 미래에셋비전스팩5호 477470
01834194 엔에이치스팩31호 481890
01845534 대신밸런스제19호스팩 482690
01873272 키움제11호스팩 489480
01872219 유안타제17호스팩 493790
01854392 유진스팩11호 488060
01813377 에이치엠씨제7호스팩 477340
01819867 미래에셋비전스팩6호 478440
01841635 미래에셋비전스팩7호 482680
01851650 KB제30호스팩 486630
01877126 키움제10호스팩 487720
01798722 SK증권제12호스팩 473000
01663639 유안타제9호스팩 430700
01670675 유안타제10호스팩 435380
01688346 유안타제11호스팩 444920
01690235 유안타제12호스팩 446150
01701753 유안타제13호스팩 449020
01706819 유안타제14호스팩 450940
01616482 신한제10호스팩 418210
01719105 신한제11호스팩 452980
01807747 신한제13호스팩 474930
01809569 신한제12호스팩 474660
01616808 키움제6호스팩 413600
01667592 키움제7호스팩 433530
01693922 키움제8호스팩 446840
01881800 한화플러스제5호스팩 498390
""".trimIndent()
    .split("\n").map { line ->
        val parts = line.split(" ")
        DartCorpCode(
            corpCode = parts[0],
            nameKr = parts[1],
            code = parts[2]
        )
    }
    .associateBy(DartCorpCode::code)

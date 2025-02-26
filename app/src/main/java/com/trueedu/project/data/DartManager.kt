package com.trueedu.project.data

import android.util.Log
import com.trueedu.project.dart.model.DartListItem
import com.trueedu.project.dart.repository.remote.DartRemote
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DartManager @Inject constructor(
    private val dartRemote: DartRemote,
) {
    companion object {
        private val TAG = DartManager::class.java.simpleName
    }

    private val items = ConcurrentHashMap<String, List<DartListItem>>()
    private var lastUpdatedAt = 0L // timestamp

    init {
        val timestamp = System.currentTimeMillis()

        // 1시간마다 체크
        if (timestamp - lastUpdatedAt > 1000 * 60 * 60 * 1) {
            lastUpdatedAt = timestamp
            loadList()
        }
    }

    fun loadList() {
        val fromDate = LocalDate.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))

        dartCorpList.forEach {
            dartRemote.list(it.corpCode, fromDate)
                .onEach { res ->
                    if (res.list?.isNotEmpty() == true) {
                        Log.d(TAG, "${it.nameKr} - ${res.list.first().let {"${it.receiptDate} ${it.reportName}"} }")
                        items[it.code] = res.list
                    }
                }
                .launchIn(MainScope())
        }
    }
}

data class DartCorpCode(
    val corpCode: String,
    val nameKr: String,
    val code: String,
)

private val dartCorpList = """
01678835 유진스팩9호 442130
01677429 엔에이치스팩27호 440820
01669475 하나금융25호스팩 435620
01675254 엔에이치스팩26호 439410
01674468 엔에이치스팩25호 438580
01677678 교보13호스팩 440790
01656374 신영스팩8호 352700
01689938 신영스팩9호 445970
01696451 대신밸런스제14호스팩 365590
01675227 삼성스팩7호 439250
01674538 엔에이치스팩24호 437780
01689336 비엔케이제1호스팩 445360
01724417 하나29호스팩 454640
01766167 KB제26호스팩 448710
01724338 하나28호스팩 454750
01744703 SK증권제10호스팩 130660
01712616 엔에이치스팩29호 451700
01724709 KB제25호스팩 455250
01791321 하나30호스팩 017860
01785551 KB제27호스팩 188260
01683387 미래에셋드림스팩1호 442900
01751448 대신밸런스제15호스팩 457390
01767494 에이치엠씨제6호스팩 462020
01696275 하나26호스팩 446750
01785056 엔에이치스팩30호 102280
01775952 한국제13호스팩 302550
01760640 교보14호스팩 274400
01792825 대신밸런스제17호스팩 457550
01738483 대신밸런스제16호스팩 148930
01725160 한화플러스제4호스팩 455310
01700374 하나27호스팩 016610
01797440 SK증권제11호스팩 121440
01787072 유진스팩10호 290120
01791561 하나31호스팩 445180
01800285 비엔케이제2호스팩 469900
01717170 상상인제4호스팩 418550
01705625 하이제8호스팩 450050
01700921 삼성스팩8호 343510
01731453 DB금융스팩11호 456440
01725577 SK증권제9호스팩 018290
01796858 신영스팩10호 472220
01792791 IBKS제24호스팩 469480
01692321 미래에셋비전스팩2호 383310
01786514 삼성스팩9호 451220
01781847 교보15호스팩 465320
01816268 KB제29호스팩 078020
01702424 미래에셋비전스팩3호 211270
01818682 한국제15호스팩 102950
01785700 IBKS제23호스팩 429270
01807729 하나33호스팩 089970
01815764 이베스트스팩6호 290270
01671197 SK증권제8호스팩 067390
01845701 교보16호스팩 435870
01798980 유안타제15호스팩 900100
01810796 KB제28호스팩 053160
01814233 미래에셋비전스팩4호 477380
01675421 IBKS제20호스팩 434480
01814589 디비금융스팩12호 368030
01866528 교보17호스팩 200350
01853214 신한제14호스팩 487360
01854408 신한제15호스팩 203690
01847550 하나34호스팩 466410
01807738 하나32호스팩 290560
01866926 KB제31호스팩 003240
01857991 디비금융제13호스팩 489730
01761296 한국제12호스팩 458610
01682740 IBKS제21호스팩 373220
01701328 IBKS제22호스팩 442770
01804476 유안타제16호스팩 391710
01817610 대신밸런스제18호스팩 478780
01801822 SK증권제13호스팩 136510
01814312 한국제14호스팩 065370
01817249 미래에셋비전스팩5호 477530
01834194 엔에이치스팩31호 400840
01845534 대신밸런스제19호스팩 438220
01873272 키움제11호스팩 300720
01872219 유안타제17호스팩 475460
01854392 유진스팩11호 458650
01813377 에이치엠씨제7호스팩 043090
01819867 미래에셋비전스팩6호 107640
01841635 미래에셋비전스팩7호 482680
01851650 KB제30호스팩 311390
01877126 키움제10호스팩 080000
01798722 SK증권제12호스팩 126730
01663639 유안타제9호스팩 461030
01670675 유안타제10호스팩 430700
01688346 유안타제11호스팩 435380
01690235 유안타제12호스팩 444920
01701753 유안타제13호스팩 446150
01706819 유안타제14호스팩 449020
01616482 신한제10호스팩 002460
01719105 신한제11호스팩 418210
01807747 신한제13호스팩 452980
01809569 신한제12호스팩 474930
01616808 키움제6호스팩 352480
01667592 키움제7호스팩 413600
01693922 키움제8호스팩 433530
""".trimIndent()
    .split("\n").map { line ->
        val parts = line.split(" ")
        DartCorpCode(
            corpCode = parts[0],
            nameKr = parts[1],
            code = parts[2]
        )
    }

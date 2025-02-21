package com.trueedu.project.dart

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentManager
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DartFragment: BaseFragment() {
    companion object {
        fun show(
            fragmentManager: FragmentManager
        ): DartFragment {
            return DartFragment().also {
                it.show(fragmentManager, "dart")
            }
        }
    }

    val loading = mutableStateOf(true)
    lateinit var document: DartDocument

    override fun init() {
        super.init()
        val parser = DartXmlParser()
        document = parser.parse(xml)
        loading.value = false
        Log.d("aaaa", "$document")
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = { BackTitleTopBar("Dart 공시", ::dismissAllowingStateLoss) },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (!loading.value) DartDocumentViewer(document)
            }
        }
    }
}

val xml = """
    <DOCUMENT xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="dart4.xsd">
    <DOCUMENT-NAME ACODE="11305">주요사항보고서(해산사유 발생)</DOCUMENT-NAME>
    <FORMULA-VERSION ADATE="20160118">6.0</FORMULA-VERSION>
    <COMPANY-NAME AREGCIK="01619513">엔에이치기업인수목적23호 주식회사</COMPANY-NAME>
    <BODY ATOCID="2">
    <LIBRARY>
    <SECTION-1 ACLASS="MANDATORY" APARTSOURCE="SOURCE">
    <TITLE ATOC="Y" AASSOCNOTE="D-0-0-0-0" ENG="Report on Major Issues" ATOCID="2">주 요 사 항 보 고 서</TITLE>
    <TABLE ACLASS="NORMAL" AFIXTABLE="Y" WIDTH="600" BORDER="0">
    <COLGROUP WIDTH="600">
    <COL WIDTH="138"/>
    <COL WIDTH="183"/>
    <COL WIDTH="279"/>
    </COLGROUP>
    <TBODY>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD COLSPAN="3" WIDTH="591" HEIGHT="23"/>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="53">
    <TD COLSPAN="3" WIDTH="591" HEIGHT="46"/>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD COLSPAN="2" VALIGN="MIDDLE" WIDTH="312" HEIGHT="23">금융위원회 귀중</TD>
    <TD ALIGN="RIGHT" VALIGN="MIDDLE" WIDTH="270" HEIGHT="23">2025 년 02 월 10 일</TD>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="53">
    <TD COLSPAN="3" WIDTH="591" HEIGHT="46"/>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD WIDTH="129" HEIGHT="23">회 사 명 :</TD>
    <TD COLSPAN="2" WIDTH="453" HEIGHT="23">엔에이치기업인수목적23호 주식회사</TD>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD WIDTH="129" HEIGHT="23">대 표 이 사 :</TD>
    <TD COLSPAN="2" WIDTH="453" HEIGHT="23">이 주 혁</TD>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD WIDTH="129" HEIGHT="23">본 점 소 재 지 :</TD>
    <TD COLSPAN="2" WIDTH="453" HEIGHT="23">서울시 영등포구 여의대로 108</TD>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD WIDTH="129" HEIGHT="23"/>
    <TD COLSPAN="2" WIDTH="453" HEIGHT="23">(전 화)02-750-5742</TD>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD WIDTH="129" HEIGHT="23"/>
    <TD COLSPAN="2" WIDTH="453" HEIGHT="23">(홈페이지)없음</TD>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD WIDTH="129" HEIGHT="23"/>
    <TD COLSPAN="2" WIDTH="453" HEIGHT="23"/>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD COLSPAN="3" WIDTH="591" HEIGHT="23"/>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD COLSPAN="3" WIDTH="591" HEIGHT="23"/>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD WIDTH="129" HEIGHT="23">작 성 책 임 자 :</TD>
    <TD WIDTH="174" HEIGHT="23">(직 책)기타비상무이사</TD>
    <TD WIDTH="270" HEIGHT="23">(성 명)노 경 호</TD>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD WIDTH="129" HEIGHT="23"/>
    <TD COLSPAN="2" WIDTH="453" HEIGHT="23">(전 화)02-750-5742</TD>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD COLSPAN="3" WIDTH="591" HEIGHT="23"/>
    </TR>
    </TBODY>
    </TABLE>
    <P/>
    <PGBRK/>
    </SECTION-1>
    </LIBRARY>
    <SECTION-1 ACLASS="MANDATORY" APARTSOURCE="SOURCE">
    <TITLE ATOC="Y" AASSOCNOTE="D-0-1-0-0" ENG="Occurrence of Causes for Corporate Dissolution" ATOCID="1">해산사유 발생</TITLE>
    <P/>
    <TABLE-GROUP ACLASS="OSM_203" ADELETETABLE="N">
    <TABLE ACLASS="EXTRACTION" AFIXTABLE="Y" WIDTH="597" BORDER="1">
    <COLGROUP WIDTH="597">
    <COL WIDTH="164"/>
    <COL WIDTH="141"/>
    <COL WIDTH="292"/>
    </COLGROUP>
    <TBODY>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="99">
    <TD COLSPAN="2" VALIGN="MIDDLE" WIDTH="296" HEIGHT="92" AUPDATECONT="N" ENG="1. Cause for dissolution">1. 해산사유</TD>
    <TE WIDTH="283" HEIGHT="92" ACODE="OSM_203_01">당사의 주권이 2025년 2월 10일 부로 코스닥시장 상장규정 제73조에 의거 상장폐지됨에 따라 당사 정관 제58조 제3호에 의한 해산사유가 발생하였습니다. </TE>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="306">
    <TD COLSPAN="2" VALIGN="MIDDLE" WIDTH="296" HEIGHT="299" AUPDATECONT="N" ENG="2. Details of dissolution">2. 해산내용</TD>
    <TE WIDTH="283" HEIGHT="299" ACODE="OSM_203_02">
    <SPAN USERMARK="0X000000">상장폐지 이후 당사의 해산 및 청산절차는 상법에 따라 진행될 예정이며, 예치자금 등은 공모전 주주를 제외한 주주에게 지분율대로 분배될 예정입니다.</SPAN>
    <SPAN/>
    <SPAN USERMARK="0X000000">또한, 예치자금 등을 제외한 잔여재산에 대해서는 공모전 발행 주식 등 및 공모주식을 대상으로 하여 정관 제59조에서 정하는 방법에 따라 지급될 예정입니다.</SPAN>
    <SPAN/>
    <SPAN USERMARK="0X000000">향후 구체적인 예치금 분배 방법 및 금액은 최종 청산 종결 시 잔여재산 분배 계획에 의거 진행할 예정임을 알려드립니다.</SPAN>
    </TE>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD COLSPAN="2" VALIGN="MIDDLE" WIDTH="296" HEIGHT="23" AUPDATECONT="N" ENG="3. Date of occurrence of causes for corporate dissolution (decision date)">3. 해산사유발생일(결정일)</TD>
    <TU ALIGN="CENTER" WIDTH="283" HEIGHT="23" AUNIT="OSM_203_05" AUNITVALUE="20250210">2025년 02월 10일</TU>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD ROWSPAN="2" VALIGN="MIDDLE" WIDTH="155" HEIGHT="53" AUPDATECONT="N" ENG="- Attendance of outside directors"> - 사외이사 참석여부</TD>
    <TD ALIGN="CENTER" VALIGN="MIDDLE" WIDTH="132" HEIGHT="23" AUPDATECONT="N" ENG="Present (No.)">참석 (명)</TD>
    <TE ALIGN="CENTER" WIDTH="283" HEIGHT="23" ACODE="OSM_203_06">1</TE>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD ALIGN="CENTER" VALIGN="MIDDLE" WIDTH="132" HEIGHT="23" AUPDATECONT="N" ENG="Absent (No.)">불참 (명)</TD>
    <TE ALIGN="CENTER" WIDTH="283" HEIGHT="23" ACODE="OSM_203_07">-</TE>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD COLSPAN="2" VALIGN="MIDDLE" WIDTH="296" HEIGHT="23" AUPDATECONT="N" ENG="- Attendance of auditors (members of Audit Committee)"> - 감사(감사위원) 참석 여부</TD>
    <TU ALIGN="CENTER" WIDTH="283" HEIGHT="23" AUNIT="OSM_203_08" AUNITVALUE="Y" ENG="Present">참석</TU>
    </TR>
    </TBODY>
    </TABLE>
    </TABLE-GROUP>
    <TABLE ACLASS="NORMAL" AFIXTABLE="Y" WIDTH="600" BORDER="0">
    <COLGROUP WIDTH="600">
    <COL WIDTH="600"/>
    </COLGROUP>
    <TBODY>
    <TR ACOPY="N" ADELETE="N" HEIGHT="30">
    <TD WIDTH="591" HEIGHT="23" AUPDATECONT="N" ENG="4. Other matters to be factored into investment decisions">4. 기타 투자판단에 참고할 사항</TD>
    </TR>
    </TBODY>
    </TABLE>
    <P/>
    <P>상장폐지 이후 당사의 해산 및 청산절차는 상법에 따라 진행될 예정이며, 예치자금 등은 공모전 주주를 제외한 주주에게 지분율대로 분배될 예정입니다.또한, 예치자금 등을 제외한 잔여재산에 대해서는 공모전 발행주식등 및 공모주식을 대상으로 하여 정관 제59조에서 정하는 방법에 따라 지급될 예정입니다.향후 구체적인 예치금 분배 방법 및 금액은 향후 최종 청산 종결 시 잔여재산 분배 계획에 의거 진행할 예정임을 알려드립니다.- 해산 진행 일정(예정)하기 일정에 따라 해산 및 청산업무를 진행할 예정이오니, 참고하여 판단하시기 바랍니다.</P>
    <TABLE BORDER="1" WIDTH="600" ACLASS="NORMAL" AFIXTABLE="N">
    <COLGROUP WIDTH="600">
    <COL WIDTH="300"/>
    <COL WIDTH="300"/>
    </COLGROUP>
    <TBODY>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD ALIGN="CENTER" WIDTH="291" USERMARK="BC0XDCDCDC" HEIGHT="23">진행절차</TD>
    <TD ALIGN="CENTER" WIDTH="291" USERMARK="BC0XDCDCDC" HEIGHT="23">일자</TD>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD ALIGN="CENTER" WIDTH="291" HEIGHT="23">상장폐지일(해산등기일)</TD>
    <TD ALIGN="CENTER" WIDTH="291" HEIGHT="23">2025년 2월 10일</TD>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD ALIGN="CENTER" WIDTH="291" HEIGHT="23">채권최고 공고</TD>
    <TD ALIGN="CENTER" WIDTH="291" HEIGHT="23">2025년 2월10일, 2025년 2월11일</TD>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD ALIGN="CENTER" WIDTH="291" HEIGHT="23">채권최고 제출기간</TD>
    <TD ALIGN="CENTER" WIDTH="291" HEIGHT="23">2025년 2월 10일 ~2025년 4월 11일</TD>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD ALIGN="CENTER" WIDTH="291" HEIGHT="23">청산재산 보고를 위한 임시주주총회</TD>
    <TD ALIGN="CENTER" WIDTH="291" HEIGHT="23">2025년 3월 31일</TD>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD ALIGN="CENTER" WIDTH="291" HEIGHT="23">잔여재산 분배 기준일(예정)</TD>
    <TD ALIGN="CENTER" WIDTH="291" HEIGHT="23">2025년 4월 18일</TD>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD ALIGN="CENTER" WIDTH="291" HEIGHT="23">잔여재산 분배(예정)</TD>
    <TD ALIGN="CENTER" WIDTH="291" HEIGHT="23">2025년 5월 20일</TD>
    </TR>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="30">
    <TD ALIGN="CENTER" WIDTH="291" HEIGHT="23">청산보고 임시주주총회(예정)</TD>
    <TD ALIGN="CENTER" WIDTH="291" HEIGHT="23">2025년 5월 30일</TD>
    </TR>
    </TBODY>
    </TABLE>
    <TABLE BORDER="0" WIDTH="600" ACLASS="NORMAL" AFIXTABLE="N">
    <COLGROUP WIDTH="600">
    <COL WIDTH="600"/>
    </COLGROUP>
    <TBODY>
    <TR ACOPY="Y" ADELETE="Y" HEIGHT="76">
    <TD WIDTH="591" HEIGHT="69">주) 해산 및 청산 절차 진행에 따라 일정 변동이 발생할 수 있는 점을 유의하여 주시기 바랍니다. 절차 진행에 따른 기준일 공고 및 명의개서 정지 공고 등은 당사 정관에 따라 이루어질 예정입니다.</TD>
    </TR>
    </TBODY>
    </TABLE>
    <P/>
    </SECTION-1>
    </BODY>
    </DOCUMENT>
""".trimIndent()

package com.trueedu.project.dart

// XML 파싱을 위한 데이터 클래스들
data class DartDocument(
    val documentName: String,
    val companyName: String,
    val body: DocumentBody
)

data class DocumentBody(
    val tables: List<TableData>,
    val sections: List<SectionData>
)

data class SectionData(
    val title: String,          // 예: "해산사유 발생"
    val titleEng: String?,      // 예: "Occurrence of Causes for Corporate Dissolution"
    val content: List<SectionContent>
)

sealed class SectionContent {
    data class Text(val content: String) : SectionContent()
    data class Table(val tableData: TableData) : SectionContent()
}

data class TableData(
    val width: Int?,
    //val columns: List<Int>,     // 컬럼 너비
    val rows: List<TableRow>,
    val isFixedTable: Boolean,  // AFIXTABLE 속성
    val tableClass: String?     // ACLASS 속성
)

data class TableRow(
    val cells: List<TableCell>,
    val height: Int?
)

data class TableCell(
    val content: String,
    val colspan: Int = 1,
    val rowspan: Int = 1,
    val align: String? = null,  // 정렬 (LEFT, CENTER, RIGHT)
    val width: Int? = null,
    val code: String? = null,
)
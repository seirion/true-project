package com.trueedu.project.dart

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// 가로 스크롤을 위한 커스텀 컴포저블
@Composable
fun HorizontalPager(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
    ) {
        content()
    }
}


@Composable
fun DartDocumentViewer(document: DartDocument) {
    // 가로 스크롤을 위한 HorizontalScrollable 추가
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        HorizontalPager(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    // 최소 너비를 지정하여 스크롤 가능하게 함
                    .width(480.dp)  // 또는 더 큰 값으로 설정 가능
                    .padding(16.dp)
            ) {
                // 문서 헤더
                item {
                    DocumentHeader(
                        documentName = document.documentName,
                        companyName = document.companyName
                    )
                }

                // 섹션별 렌더링
                document.body.sections.forEach { section ->
                    item {
                        DartSection(section)
                    }
                }

                // 독립적인 테이블 렌더링
                document.body.tables.forEach { table ->
                    item {
                        DartTable(table)
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentHeader(
    documentName: String,
    companyName: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = documentName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = companyName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun DartTable(table: TableData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            table.rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    row.cells.forEach { cell ->
                        TableCell(
                            cell = cell,
                            modifier = Modifier.weight(
                                when {
                                    cell.width != null -> cell.width.toFloat()
                                    cell.colspan > 1 -> cell.colspan.toFloat()
                                    else -> 1f
                                }
                            )
                        )
                    }
                }
                if (table.rows.last() != row) {
                    Divider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TableCell(
    cell: TableCell,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxHeight(),
        contentAlignment = when(cell.align?.uppercase()) {
            "CENTER" -> Alignment.Center
            "RIGHT" -> Alignment.CenterEnd
            else -> Alignment.CenterStart
        }
    ) {
        Text(
            text = cell.content,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (cell.code != null) FontWeight.Bold else FontWeight.Normal,
            // TE 태그가 있는 경우 특별한 스타일 적용
            color = if (cell.code != null)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun DartSection(section: SectionData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // 섹션 제목
        Text(
            text = section.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 영문 제목 (있는 경우)
        section.titleEng?.let { engTitle ->
            Text(
                text = engTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // 섹션 내용
        section.content.forEach { content ->
            when (content) {
                is SectionContent.Text -> {
                    Text(
                        text = content.content,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                is SectionContent.Table -> {
                    DartTable(content.tableData)
                }
            }
        }
    }
}

@Composable
fun TableCellContent(cell: TableCell) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxHeight(),
        contentAlignment = when(cell.align?.uppercase()) {
            "CENTER" -> Alignment.Center
            "RIGHT" -> Alignment.CenterEnd
            else -> Alignment.CenterStart
        }
    ) {
        Text(
            text = cell.content,
            style = MaterialTheme.typography.bodyMedium,
            // TE 태그의 내용은 강조 표시할 수 있음
            fontWeight = if (cell.code != null) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// 미리보기
@Preview(showBackground = true)
@Composable
fun DartDocumentPreview() {
    MaterialTheme {
        // 샘플 데이터로 미리보기
        val sampleDocument = DartDocument(
            documentName = "주요사항보고서(해산사유 발생)",
            companyName = "엔에이치기업인수목적23호 주식회사",
            body = DocumentBody(
                sections = listOf(
                    SectionData(
                        title = "해산사유 발생",
                        titleEng = "Occurrence of Causes for Corporate Dissolution",
                        content = listOf(
                            SectionContent.Text("상장폐지 이후 당사의 해산 및 청산절차는 상법에 따라 진행될 예정입니다."),
                            SectionContent.Table(
                                TableData(
                                    width = null,
                                    rows = listOf(
                                        TableRow(
                                            cells = listOf(
                                                TableCell("구분", colspan = 1),
                                                TableCell("일자", colspan = 1),
                                            ),
                                            height = null,
                                        )
                                    ),
                                    isFixedTable = true,
                                    tableClass = "NORMAL"
                                )
                            )
                        )
                    )
                ),
                tables = emptyList()
            )
        )

        DartDocumentViewer(sampleDocument)
    }
}
package com.trueedu.project.dart

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

// XML 파서
class DartXmlParser {
    fun parse(xml: String): DartDocument {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        var eventType = parser.eventType
        var documentName = ""
        var companyName = ""
        val tables = mutableListOf<TableData>()
        val sections = mutableListOf<SectionData>()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "DOCUMENT-NAME" -> {
                            documentName = parser.nextText()
                        }
                        "COMPANY-NAME" -> {
                            companyName = parser.nextText()
                        }
                        "TABLE" -> {
                            tables.add(parseTable(parser))
                        }
                        "SECTION-1" -> {
                            sections.add(parseSection(parser))
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return DartDocument(
            documentName = documentName,
            companyName = companyName,
            body = DocumentBody(tables = tables, sections = sections)
        )
    }

    private fun parseTable(parser: XmlPullParser): TableData {
        val rows = mutableListOf<TableRow>()
        var eventType = parser.eventType

        // 테이블 속성 파싱
        val width = parser.getAttributeValue(null, "WIDTH")?.toIntOrNull()
        val isFixed = parser.getAttributeValue(null, "AFIXTABLE") == "Y"
        val tableClass = parser.getAttributeValue(null, "ACLASS")

        while (!(eventType == XmlPullParser.END_TAG && parser.name == "TABLE")) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "TR") {
                rows.add(parseRow(parser))
            }
            eventType = parser.next()
        }

        return TableData(
            rows = rows,
            width = width,
            isFixedTable = isFixed,
            tableClass = tableClass
        )
    }

    private fun parseRow(parser: XmlPullParser): TableRow {
        val cells = mutableListOf<TableCell>()
        var eventType = parser.eventType
        val height = parser.getAttributeValue(null, "HEIGHT")?.toIntOrNull()

        while (!(eventType == XmlPullParser.END_TAG && parser.name == "TR")) {
            if (eventType == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "TD", "TH", "TE" -> {
                        cells.add(parseCell(parser))
                    }
                }
            }
            eventType = parser.next()
        }

        return TableRow(cells = cells, height = height)
    }

    private fun parseCell(parser: XmlPullParser): TableCell {
        // 셀 속성 파싱
        val colspan = parser.getAttributeValue(null, "COLSPAN")?.toIntOrNull() ?: 1
        val rowspan = parser.getAttributeValue(null, "ROWSPAN")?.toIntOrNull() ?: 1
        val width = parser.getAttributeValue(null, "WIDTH")?.toIntOrNull()
        val align = parser.getAttributeValue(null, "ALIGN")
        val code = parser.getAttributeValue(null, "ACODE") // TE 태그의 특수 속성

        val content = StringBuilder()
        var eventType = parser.eventType

        while (!((eventType == XmlPullParser.END_TAG &&
                    (parser.name == "TD" || parser.name == "TH" || parser.name == "TE")))) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "SPAN" -> {
                            // SPAN 태그 내용 파싱
                            val spanContent = parseSpan(parser)
                            content.append(spanContent)
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    content.append(parser.text)
                }
            }
            eventType = parser.next()
        }

        return TableCell(
            content = content.toString().trim(),
            colspan = colspan,
            rowspan = rowspan,
            width = width,
            align = align,
            code = code
        )
    }

    private fun parseSpan(parser: XmlPullParser): String {
        val content = StringBuilder()
        var eventType = parser.eventType

        while (!(eventType == XmlPullParser.END_TAG && parser.name == "SPAN")) {
            if (eventType == XmlPullParser.TEXT) {
                content.append(parser.text)
            }
            eventType = parser.next()
        }

        return content.toString().trim()
    }

    ///
    private fun parseSection(parser: XmlPullParser): SectionData {
        var title = ""
        var titleEng = ""
        val contents = mutableListOf<SectionContent>()

        var eventType = parser.eventType
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "SECTION-1")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "TITLE" -> {
                            title = parser.nextText()
                            titleEng = parser.getAttributeValue(null, "ENG")
                        }
                        "TABLE" -> {
                            contents.add(SectionContent.Table(parseTable(parser)))
                        }
                        "P" -> {
                            val text = parser.nextText()
                            if (text.isNotBlank()) {
                                contents.add(SectionContent.Text(text))
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return SectionData(title, titleEng, contents)
    }

}
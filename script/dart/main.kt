
import java.io.File

fun main() {
    val filePath = "./CORPCODE.xml"
    val file = File(filePath).also {
        if (!it.exists()) {
            throw IllegalArgumentException("파일이 존재하지 않습니다: $filePath") 
        }
        if (!it.canRead()) {
            throw IllegalArgumentException("파일을 읽을 수 없습니다: $filePath")
        }
    }

    var started = false
    var corpCode: String = ""
    var corpName: String = ""
    var stockCode: String = ""
    var i = 0

    file.forEachLine { line ->
        if (line.contains("<list>")) {
            started = true
        } else if (line.contains("</list>")) {
            started = false
            corpName = ""
            corpCode = ""
            stockCode = ""
        } else if (started) {
            if (line.contains("<corp_code>")) {
                corpCode = line.substring(line.indexOf("<corp_code>") + 11, line.indexOf("</corp_code>"))
            } else if (line.contains("<corp_name>")) {
                corpName = line.substring(line.indexOf("<corp_name>") + 11, line.indexOf("</corp_name>"))
            } else if (line.contains("<stock_code>")) {
                stockCode = line.substring(line.indexOf("<stock_code>") + 12, line.indexOf("</stock_code>"))
            }

            if (corpCode.isBlank() || corpName.isBlank() || stockCode.isBlank()) {
                return@forEachLine
            }

            if (corpName.contains("스팩")) {
                println("$corpCode $corpName $stockCode")
                corpName = ""
                corpCode = ""
                stockCode = ""
                i++
            }
        }
    }
    println(i)
}


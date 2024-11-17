import java.io.File
import java.nio.charset.Charset

fun convertCp949ToUtf8(inputFile: File, outputFile: File) {
    val cp949Bytes = inputFile.readBytes()
    val utf8String = String(cp949Bytes, Charset.forName("CP949"))
    outputFile.writeText(utf8String, Charset.forName("UTF-8"))
}

fun main() {
    listOf(
        "kospi_code.mst" to "kospi.txt",
        "kosdaq_code.mst" to "kosdaq.txt",
    ).forEach { (input, output) ->
        val inputFile = File(input)
        val outputFile = File(output)
        convertCp949ToUtf8(inputFile, outputFile)
    }
}

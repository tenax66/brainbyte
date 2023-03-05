package brainbyte

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    val code = File(args[0]).readText()
    val fileName = args[1]
    val path = Paths.get("$fileName.class")

    Files.write(path, ByteCodeCompiler().compile(fileName, code))
}

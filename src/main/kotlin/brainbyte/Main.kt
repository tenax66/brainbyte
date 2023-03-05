package brainbyte

import java.io.File

fun main(args: Array<String>) {
    val bcc = ByteCodeCompiler()
    val inputFile = File(args[0])
    val code: String = inputFile.readText()

    bcc.run(args[1], code)
}

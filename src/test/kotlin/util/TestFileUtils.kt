package util

import java.nio.file.Path
import java.nio.file.Paths

object TestFileUtils {
    fun getResourceFile(fileName: String, dir: String = "input"): Path =
        Paths.get(this.javaClass.classLoader.getResource("$dir/$fileName").toURI())
}
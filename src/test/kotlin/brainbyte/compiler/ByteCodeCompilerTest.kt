package brainbyte.compiler

import org.junit.jupiter.api.Test
import brainbyte.util.ByteClassLoader
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import util.TestFileUtils
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ByteCodeCompilerTest {

    private val standardOut = System.out

    @Test
    fun testCompile() {
        val outContent = ByteArrayOutputStream()
        System.setOut(PrintStream(outContent))

        val inputPath = TestFileUtils.getResourceFile("HelloWorld.txt")
        val codeBytes = ByteCodeCompiler().compile("HelloWorld", inputPath.toFile().readText())

        // load a class from ByteArray
        val byteClassLoader = ByteClassLoader(this.javaClass.classLoader)
        byteClassLoader.loadDataInBytes(codeBytes, "HelloWorld")
        val klass = byteClassLoader.loadClass("HelloWorld")

        // invoke main method of the loaded class
        val main = klass.getMethod("main", Array<String>::class.java)
        main.invoke(null, arrayOf<String>())

        // get an actual value from OutputStream
        assertThat(outContent.toString()).isEqualTo("Hello World!" + System.lineSeparator())
    }

    @AfterEach
    fun tearDown() {
        // restore System.out
        System.setOut(standardOut)
    }

}
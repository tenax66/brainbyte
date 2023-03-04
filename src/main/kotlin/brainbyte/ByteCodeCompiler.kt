package brainbyte

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    val bcc = ByteCodeCompiler()
    val inputFile = File(args[0])
    val code: String = inputFile.readText()

    bcc.run(args[1], code)
}

class ByteCodeCompiler {
    private val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)

    fun run(fileName: String, code: String) {
        val p = Paths.get("$fileName.class")
        Files.write(p, serializeToBytes(fileName, code))
    }

    private fun serializeToBytes(fileName: String, code: String): ByteArray {
        classWriter.visit(
            Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, fileName,
            null, "java/lang/Object", null
        )
        addConstructor()
        addMainMethod(code)
        classWriter.visitEnd()
        return classWriter.toByteArray()
    }

    private fun addConstructor() {
        val mv: MethodVisitor =
            classWriter.visitMethod(
                Opcodes.ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null
            )
        mv.visitVarInsn(Opcodes.ALOAD, 0)
        mv.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/Object",
            "<init>",
            "()V",
            false
        )
        mv.visitInsn(Opcodes.RETURN)
        // passing dummy values to compute the stack value frame automatically
        mv.visitMaxs(0, 0)
        mv.visitEnd()
    }

    private fun addMainMethod(code: String) {
        val mv: MethodVisitor =
            classWriter.visitMethod(
                Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                "main",
                "([Ljava/lang/String;)V",
                null,
                null
            )
        mv.visitCode()

        // prepare a local variable for a memory array
        mv.visitIntInsn(Opcodes.SIPUSH, 30000)
        mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE)
        // this has index 0
        mv.visitVarInsn(Opcodes.ASTORE, 0)

        // prepare a local variable for a pointer
        mv.visitInsn(Opcodes.ICONST_0)
        // this has index 1
        mv.visitVarInsn(Opcodes.ISTORE, 1)

        compile(code.byteInputStream(), mv)

        mv.visitInsn(Opcodes.RETURN)
        //passing dummy values to compute the stack value frame automatically
        mv.visitMaxs(0, 0)
        mv.visitEnd()
    }

    private fun compile(codeStream: InputStream, mv: MethodVisitor) {
        while (true) {
            val byte = codeStream.read()
            if (byte == -1) {
                break
            }

            when (Char(byte)) {
                '[' -> {
                    val loopStart = Label()
                    val loopEnd = Label()

                    mv.visitLabel(loopStart)

                    // load the current value of the pointer
                    mv.visitVarInsn(Opcodes.ALOAD, 0)
                    mv.visitVarInsn(Opcodes.ILOAD, 1)
                    mv.visitInsn(Opcodes.BALOAD)

                    mv.visitJumpInsn(Opcodes.IFEQ, loopEnd)

                    // process codes inside the loop recursively
                    compile(codeStream, mv)

                    mv.visitLabel(loopEnd)

                    // load the current value of the pointer
                    mv.visitVarInsn(Opcodes.ALOAD, 0)
                    mv.visitVarInsn(Opcodes.ILOAD, 1)
                    mv.visitInsn(Opcodes.BALOAD)

                    mv.visitJumpInsn(Opcodes.IFNE, loopStart)

                }

                ']' -> {
                    // simply return to the '[' block
                    return
                }

                else -> {
                    compileNonLoopElement(Char(byte), mv)
                }
            }
        }
    }


    private fun compileNonLoopElement(command: Char, mv: MethodVisitor) {
        when (command) {
            '+' -> {
                mv.visitVarInsn(Opcodes.ALOAD, 0)
                mv.visitVarInsn(Opcodes.ILOAD, 1)
                mv.visitInsn(Opcodes.DUP2)
                mv.visitInsn(Opcodes.BALOAD)
                mv.visitInsn(Opcodes.ICONST_1)
                mv.visitInsn(Opcodes.IADD)
                mv.visitInsn(Opcodes.BASTORE)
            }

            '-' -> {
                mv.visitVarInsn(Opcodes.ALOAD, 0)
                mv.visitVarInsn(Opcodes.ILOAD, 1)
                mv.visitInsn(Opcodes.DUP2)
                mv.visitInsn(Opcodes.BALOAD)
                mv.visitInsn(Opcodes.ICONST_M1)
                mv.visitInsn(Opcodes.IADD)
                mv.visitInsn(Opcodes.BASTORE)
            }

            '>' -> mv.visitIincInsn(1, 1)
            '<' -> mv.visitIincInsn(1, -1)
            '.' -> {
                mv.visitFieldInsn(
                    Opcodes.GETSTATIC,
                    "java/lang/System",
                    "out",
                    "Ljava/io/PrintStream;"
                )
                mv.visitVarInsn(Opcodes.ALOAD, 0)
                mv.visitVarInsn(Opcodes.ILOAD, 1)
                mv.visitInsn(Opcodes.BALOAD)
                mv.visitInsn(Opcodes.I2C)
                mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/io/PrintStream",
                    "print",
                    "(C)V",
                    false
                )
            }

            ',' -> {
                mv.visitVarInsn(Opcodes.ALOAD, 0)
                mv.visitVarInsn(Opcodes.ILOAD, 0)
                mv.visitFieldInsn(
                    Opcodes.GETSTATIC,
                    "java/lang/System",
                    "in",
                    "Ljava/io/InputStream;"
                )
                mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/io/InputStream",
                    "read",
                    "()I",
                    false
                )
                mv.visitInsn(Opcodes.BASTORE)
            }

        }
    }
}
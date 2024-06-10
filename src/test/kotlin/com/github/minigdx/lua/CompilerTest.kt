package com.github.minigdx.lua

import com.github.minigdx.lua.bytecode.read
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class CompilerTest {

    @Test
    fun compileSimple() {
        val input = read("src/test/resources/custom/simple.lua").toString(Charsets.UTF_8)
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()

        val output = Compiler().compile(ast)

        val expected = read("src/test/resources/custom/simple.bin")
        assertEquals(expected, output)
    }

    @Test
    fun compileFunction() {
        val input = read("src/test/resources/custom/function.lua").toString(Charsets.UTF_8)
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()

        val output = Compiler().compile(ast)

        val expected = read("src/test/resources/custom/function.bin")
        assertEquals(expected, output)
    }

    @Test
    fun compileAll() {
        val input = read("src/test/resources/lua/all.lua").toString(Charsets.UTF_8)
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()

        val output = Compiler().compile(ast)

        val expected = read("src/test/resources/lua/all.bin")
        assertEquals(expected, output)
    }

    @Test
    fun compileAllFiles() {
        File("src/test/resources/lua").listFiles()!!.filter { it.name.endsWith(".lua") }.forEach {
            val lexer = Lexer(it.absolutePath)
            val tokens = lexer.tokenize()
            val parser = Parser(tokens)
            val ast = parser.parse()

            val output = Compiler().compile(ast)

            val file = it.nameWithoutExtension + ".bin"
            val expected = read(it.parentFile.resolve(file).absolutePath)
            assertEquals(expected, output)
        }
    }
}
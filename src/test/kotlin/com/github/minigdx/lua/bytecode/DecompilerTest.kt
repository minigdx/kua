package com.github.minigdx.lua.bytecode

import kotlin.test.Test

class DecompilerTest {
    @Test
    fun readSimple() {
        val content = read("src/test/resources/custom/simple.bin")
        val header = Decompiler().decompile(content)
        // kotlin.test.assertEquals("\u001BLua", header)
    }

    @Test
    fun readFunction() {
        val content = read("src/test/resources/custom/function.bin")
        val header = Decompiler().decompile(content)
        // kotlin.test.assertEquals("\u001BLua", header)
    }

    @Test
    fun readAll() {
        val content = read("src/test/resources/lua/all.bin")
        val header = Decompiler().decompile(content)
        // kotlin.test.assertEquals("\u001BLua", header)
    }

}
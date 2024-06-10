package com.github.minigdx.lua

import com.github.minigdx.lua.bytecode.read
import org.junit.jupiter.api.Test

class VirtualMachineTest {

    @Test
    fun executeSimple() {
        val content = read("src/test/resources/custom/simple.bin")
        val chunk = Decompiler().decompile(content)

        VirtualMachine().execute(chunk)
    }

    @Test
    fun executeFunction() {
        val content = read("src/test/resources/custom/function.bin")
        val chunk = Decompiler().decompile(content)

        VirtualMachine().execute(chunk)
    }
}
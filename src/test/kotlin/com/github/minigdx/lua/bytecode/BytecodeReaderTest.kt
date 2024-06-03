package com.github.minigdx.lua.bytecode

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals


fun read(file: String): ByteArray {
    return File(file).readBytes()
}

class BytecodeReaderTest {

    @Test
    fun readStringWithLenght() {
        val content = read("src/test/resources/lua/all.bin")
        val header = BytecodeReader(content).readString(4u)
        assertEquals("\u001BLua", header)
    }

    @Test
    fun readString() {
        val content = byteArrayOf(
            -116,
            64,
            115,
            105,
            109,
            112,
            108,
            101,
            46,
            108,
            117,
            97
        )
        val bytecodeReader = BytecodeReader(content)
        val readString = bytecodeReader.readString()
        assertEquals("@simple.lua", readString)
    }

    @Test
    fun readInt8() {
        val content = read("src/test/resources/lua/all.bin")
        val reader = BytecodeReader(content)
        reader.readString(4u)
        val result = reader.readInt8()
        assertEquals(0x54, result)
    }

    @Test
    fun readHeader() {
        val content = read("src/test/resources/lua/all.bin")
        val reader = BytecodeReader(content)
        reader.readHeader()
    }

    @Test
    fun readUnsigned() {
        val result = BytecodeReader(byteArrayOf((0x01).toByte(), (0x85).toByte())).readUnsigned(Int.MAX_VALUE.toUInt())
        assertEquals(133u, result)
    }

    @Test
    fun readDouble() {
        val result = BytecodeReader(byteArrayOf(0, 0, 0, 0, 0, 40, 119, 64)).readDouble()
        assertEquals(370.5, result)
    }

}
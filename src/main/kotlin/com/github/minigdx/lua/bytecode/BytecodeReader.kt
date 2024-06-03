package com.github.minigdx.lua.bytecode

// https://github.com/JustAPerson/lbi/blob/master/src/lbi.lua
internal class BytecodeReader(private val bytecode: ByteArray) {
    // Position of the actual read byte.
    var index: UInt = 0u

    internal fun hasNext(): Boolean {
        return index.toInt() < bytecode.size
    }

    // https://www.lua.org/source/5.4/lundump.c.html#loadStringN
    internal fun readString(): String? {
        val size = readSize()
        if(size == 0u) return null
        return readString(size - 1u)
    }

    internal fun readString(len: UInt): String {
        val start = index.toInt()
        val end = (index + len).toInt()

        val result = bytecode.copyOfRange(start, end)
            .toString(Charsets.ISO_8859_1)
            .also { index += len }
        return result
    }

    private fun inc(): Int {
        val result = index.toInt()
        index++
        return result
    }

    internal fun readByte(): Byte {
        return bytecode[inc()]
    }

    internal fun readInt(): Int {
        return readUnsigned(Int.MAX_VALUE.toUInt()).toInt()
    }

    internal fun readInt8(): Int {
        val value = readByte().toInt() and 0xFF
        return value
    }

    internal fun readInt32(): UInt {
        val a = bytecode[inc()].toUInt() and 0xFF.toUInt()
        val b = bytecode[inc()].toUInt() and 0xFF.toUInt()
        val c = bytecode[inc()].toUInt() and 0xFF.toUInt()
        val d = bytecode[inc()].toUInt() and 0xFF.toUInt()
        return ((d shl 24) or (c shl 16) or (b shl 8) or a)
    }

    internal fun readInt64(): UInt {
        val a = readInt32() and 0xFFFFFFFF.toUInt()
        val b = readInt32()
        return (b shl 32) or a
    }

    internal fun readSize(): UInt {
        return readUnsigned(UInt.MAX_VALUE)
    }

    internal fun readDouble(): Double {
        val bytes = readArray(8u)

        val longBits = (bytes[0].toLong() and 0xFF) or
                ((bytes[1].toLong() and 0xFF) shl 8) or
                ((bytes[2].toLong() and 0xFF) shl 16) or
                ((bytes[3].toLong() and 0xFF) shl 24) or
                ((bytes[4].toLong() and 0xFF) shl 32) or
                ((bytes[5].toLong() and 0xFF) shl 40) or
                ((bytes[6].toLong() and 0xFF) shl 48) or
                ((bytes[7].toLong() and 0xFF) shl 56)

        return Double.fromBits(longBits)
    }

    // https://www.lua.org/source/5.4/lundump.c.html#loadUnsigned
    internal fun readUnsigned(limit: UInt): UInt {
        var x = 0u
        var b: Int
        val adjustedLimit = limit shr 7
        do {
            b = readInt8()
            if (x >= adjustedLimit) {
                TODO()
            }
            x = (x shl 7) or (b and 0x7F).toUInt()
        } while ((b and 0x80) == 0)
        return x
    }

    // https://www.lua.org/source/5.4/lundump.c.html#checkHeader
    internal fun readHeader() {
        assert(readString(LUA_SIGNATURE.length.toUInt()) == LUA_SIGNATURE) {
            "Lua bytecode expected. The code doens't seem to be a Lua compatible bytecode"
        }
        assert(readByte() == LUA_SUPPORTED_VERSION) {
            "Only Lua 5.4 is supported at the moment. " +
                    "Please provide a bytecode generated for Lua 5.4"
        }

        // Lua format
        assert(readByte() == LUAC_FORMAT) {
            "Only the official Lua format is supported. Please use a Lua compiler that emit " +
                    "official Lua bytecode generated for Lua 5.4"
        }

        assert(readString(LUAC_DATA.length.toUInt()) == LUAC_DATA) {
            "The chunk seems to be corrupted. Please check that the file has not been altered. " +
                    "You can also try to recompile it with the official Lua compiler."
        }

        assert(readByte().toInt() == 4) {
            "Instruction size is expected to be 4 bytes."
        }
        assert(readByte().toInt() == 8) {
            "Integer size is expected to be 8 bytes."
        }
        assert(readByte().toInt() == 8) {
            "Double size is expected to be 8 bytes."
        }

        assert(readInt64() == LUAC_INT)
        assert(readDouble() == LUAC_DOUBLE)
    }

    fun readArray(len: UInt, size: Int = 1): ByteArray {
        return bytecode.copyOfRange(index.toInt(), (index + len * size.toUInt()).toInt()).also { index += len * size.toUInt() }
    }

    /**
     * Read an array using the size of 4 bits per element.
     * The size might need to be updated for other cases.
     */
    fun readVector(len: UInt): ByteArray {
        return bytecode.copyOfRange(index.toInt(), (index + len * 4u).toInt()).also { index += len * 4u }
    }

    companion object {
        private const val LUA_SIGNATURE = "\u001BLua"
        private const val LUAC_DATA = "\u0019\u0093\u000D\u000A\u001A\u000A"
        private const val LUA_SUPPORTED_VERSION = 0x54.toByte()
        private const val LUAC_INT = 0x5678u
        private const val LUAC_FORMAT = 0.toByte()

        // https://www.lua.org/source/5.4/lundump.h.html#LUAC_NUM
        private const val LUAC_DOUBLE = 370.5
    }
}
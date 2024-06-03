package com.github.minigdx.lua.bytecode

import com.github.minigdx.lua.type.LuaClosure
import com.github.minigdx.lua.type.LuaProto

class Decompiler {

    // https://www.lua.org/source/5.4/lundump.c.html#loadFunction
    internal fun readFunction(
        reader: BytecodeReader,
        proto: LuaProto,
        psource: String? = null
    ) {
        proto.source = reader.readString() ?: psource

        proto.lineDefined = reader.readInt()
        proto.lastLineDefined = reader.readInt()
        proto.numparams = reader.readInt8()
        proto.isVarArgs = reader.readByte() == 0.toByte()
        proto.maxStackSize = reader.readInt8()

        readCode(reader, proto)
        readConstants(reader, proto)
        readUpValues(reader)
        readProtos(reader, proto)
        readDebug(reader, proto)
    }

    private fun readDebug(reader: BytecodeReader, proto: LuaProto) {
        // https://www.lua.org/source/5.4/lundump.c.html#loadDebug
        val n = reader.readInt()
        reader.readArray(n.toUInt())

        val nn = reader.readInt()
        repeat(nn) {
            reader.readInt()
            reader.readInt()
        }

        val nnn = reader.readInt()
        repeat(nnn) {
            println("varname : " + reader.readString())
            reader.readInt() // startpc
            reader.readInt() // endpc
        }
        var nnnn = reader.readInt()
        if (nnnn == 0) {
            nnnn = proto.sizeupvalues
        }
        repeat(nnnn) {
            println(reader.readString())
        }
    }

    private fun readProtos(reader: BytecodeReader, f: LuaProto) {
        // https://www.lua.org/source/5.4/lundump.c.html#loadProtos
        val n = reader.readInt()
        repeat(n) {
            readFunction(reader, LuaProto(), f.source)
        }
    }

    private fun readCode(reader: BytecodeReader, f: LuaProto) {
        // https://www.lua.org/source/5.4/lundump.c.html#loadCode
        val nbCode = reader.readInt()
        f.sizecode = nbCode
        f.code = reader.readVector(nbCode.toUInt())
    }

    private fun readConstants(reader: BytecodeReader, f: LuaProto) {
        // https://www.lua.org/source/5.4/lundump.c.html#loadConstants
        val n = reader.readInt()

        // f.k = // FIXME
        repeat(n) {
            val byte = reader.readByte()
            when (val r = byte.toInt()) {
                LuaValue.LUA_NILL.value -> println("nil")
                LuaValue.LUA_TRUE.value -> println("true")
                LuaValue.LUA_FALSE.value -> println("false")
                LuaValue.LUA_NUMBER_INT.value -> {
                    println("int ${reader.readInt64()}")
                }

                LuaValue.LUA_NUMBER_FLOAT.value -> {
                    println("float ${reader.readDouble()}")

                }

                LuaValue.LUA_SHORT_STRING.value -> println("str short ${reader.readString()}")
                LuaValue.LUA_LONG_STRING.value -> println("str long ${reader.readString()}")
                else -> {
                    println("type $r unknown")
                }
            }
        }
    }

    private fun readUpValues(reader: BytecodeReader) {
        // https://www.lua.org/source/5.4/lundump.c.html#loadUpvalues
        val n = reader.readInt()
        repeat(n) {
            val instack = reader.readByte()
            val idx = reader.readByte()
            val kind = reader.readByte()

            println("upvalues $instack | $idx | $kind")
        }
    }

    fun decompile(bytecode: ByteArray) {
        val reader = BytecodeReader(bytecode)

        reader.readHeader()

        val nbUpbals = reader.readInt8()

        val cl = LuaClosure(nbUpbals)

        readFunction(reader, cl.p)
    }
}
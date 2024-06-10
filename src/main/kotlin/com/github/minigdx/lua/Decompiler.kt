package com.github.minigdx.lua

import com.github.minigdx.lua.bytecode.BytecodeReader
import com.github.minigdx.lua.bytecode.LuaValueType
import com.github.minigdx.lua.type.AbsoluteLineInfo
import com.github.minigdx.lua.type.LuaValue
import com.github.minigdx.lua.type.LocVar
import com.github.minigdx.lua.type.LuaClosure
import com.github.minigdx.lua.type.LuaDouble
import com.github.minigdx.lua.type.LuaFalse
import com.github.minigdx.lua.type.LuaInteger
import com.github.minigdx.lua.type.LuaNil
import com.github.minigdx.lua.type.LuaProto
import com.github.minigdx.lua.type.LuaString
import com.github.minigdx.lua.type.LuaTrue
import com.github.minigdx.lua.type.UpValue

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
        readUpValues(reader, proto)
        readProtos(reader, proto)
        readDebug(reader, proto)
    }

    private fun readDebug(reader: BytecodeReader, proto: LuaProto) {
        // https://www.lua.org/source/5.4/lundump.c.html#loadDebug
        val n = reader.readInt()
        reader.readArray(n.toUInt())

        val nAbsLine = reader.readInt()
        val absLine = arrayOfNulls<AbsoluteLineInfo>(nAbsLine)
        repeat(nAbsLine) { i ->
            absLine[i] = AbsoluteLineInfo(
                pc = reader.readInt(),
                line = reader.readInt()
            )
        }
        proto.abslineinfo = absLine.requireNoNulls()

        val nLocvars = reader.readInt()
        val locvars = arrayOfNulls<LocVar>(nLocvars)
        repeat(nLocvars) { i ->
            locvars[i] = LocVar(
                varname = reader.readString(),
                startpc = reader.readInt(),
                endpc = reader.readInt()
            )
        }
        proto.locvars = locvars.requireNoNulls()

        var nUpValues = reader.readInt()
        if (nUpValues == 0) {
            nUpValues = proto.sizeupvalues
        }
        repeat(nUpValues) {
            proto.upValues[it].name = reader.readString()
        }
    }

    private fun readProtos(reader: BytecodeReader, f: LuaProto) {
        // https://www.lua.org/source/5.4/lundump.c.html#loadProtos
        val n = reader.readInt()
        val protos = arrayOfNulls<LuaProto>(n)
        repeat(n) {
            val p = LuaProto()
            protos[it] = p
            readFunction(reader, p, f.source)
        }
        f.p = protos.requireNoNulls()
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

        val luaValues = arrayOfNulls<LuaValue>(n)

        repeat(n) {
            val byte = reader.readByte()
            luaValues[it] = when (byte.toInt()) {
                LuaValueType.LUA_NILL.value -> LuaNil
                LuaValueType.LUA_TRUE.value -> LuaTrue
                LuaValueType.LUA_FALSE.value -> LuaFalse
                LuaValueType.LUA_NUMBER_INT.value -> LuaInteger(reader.readInt64())
                LuaValueType.LUA_NUMBER_FLOAT.value -> LuaDouble(reader.readDouble())
                LuaValueType.LUA_SHORT_STRING.value -> LuaString(reader.readString()!!, LuaValueType.LUA_SHORT_STRING)
                LuaValueType.LUA_LONG_STRING.value -> LuaString(reader.readString()!!, LuaValueType.LUA_LONG_STRING)
                else -> TODO()
            }
        }

        f.k = luaValues.requireNoNulls()
    }

    private fun readUpValues(reader: BytecodeReader, proto: LuaProto) {
        // https://www.lua.org/source/5.4/lundump.c.html#loadUpvalues
        val n = reader.readInt()
        val result = arrayOfNulls<UpValue>(n)
        repeat(n) {
            val instack = reader.readByte()
            val idx = reader.readByte()
            val kind = reader.readByte()

            result[it] = UpValue(
                name = null,
                instack = instack == 0x01.toByte(),
                idx = idx.toInt(),
                kind = kind.toInt()
            )
        }
        proto.upValues = result.requireNoNulls()
    }

    fun decompile(bytecode: ByteArray): LuaClosure {
        val reader = BytecodeReader(bytecode)

        reader.readHeader()

        val nbUpbals = reader.readInt8()

        val cl = LuaClosure(nbUpbals)

        readFunction(reader, cl.p)

        return cl
    }
}
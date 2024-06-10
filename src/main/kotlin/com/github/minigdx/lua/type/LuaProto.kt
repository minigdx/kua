package com.github.minigdx.lua.type

import com.github.minigdx.lua.bytecode.LuaValueType

class LuaProto(
    val sizeupvalues: Int = -1,
    var source: String? = null,
) {
    var code: ByteArray = byteArrayOf()
    var sizecode: Int = -1
    var isVarArgs: Boolean = false
    var numparams: Int = -1
    var maxStackSize: Int = -1

    var upValues: Array<UpValue> = emptyArray()
    var k: Array<LuaValue> = emptyArray()
    var p: Array<LuaProto> = emptyArray()

    /** --- debug information --- **/
    var lineDefined: Int = -1
    var lastLineDefined: Int = -1

    var abslineinfo: Array<AbsoluteLineInfo> = emptyArray()
    var locvars: Array<LocVar> = emptyArray()
}

/**
 * Associates the absolute line source for a given instruction ('pc').
 * The array 'lineinfo' gives, for each instruction, the difference in
 * lines from the previous instruction. When that difference does not
 * fit into a byte, Lua saves the absolute line for that instruction.
 * (Lua also saves the absolute line periodically, to speed up the
 * computation of a line number: we can use binary search in the
 * absolute-line array, but we must traverse the 'lineinfo' array
 * linearly to compute a line.)
 */
// https://www.lua.org/source/5.4/lobject.h.html#AbsLineInfo
class AbsoluteLineInfo(val pc: Int, val line: Int)

interface LuaValue {
    val value: Any
    val type: LuaValueType
}

class LuaDouble(override val value: Number) : LuaValue {
    override val type: LuaValueType = LuaValueType.LUA_NUMBER_FLOAT
}

class LuaInteger(override val value: UInt) : LuaValue {
    override val type: LuaValueType = LuaValueType.LUA_NUMBER_INT
}

class LuaString(override val value: Any, override val type: LuaValueType) : LuaValue

object LuaNil : LuaValue {
    override val type: LuaValueType = LuaValueType.LUA_NILL
    override val value: Any = Unit
}

interface LuaBoolean : LuaValue

object LuaFalse : LuaBoolean {
    override val type: LuaValueType = LuaValueType.LUA_FALSE
    override val value: Boolean = false
}

object LuaTrue : LuaBoolean {
    override val type: LuaValueType = LuaValueType.LUA_TRUE
    override val value: Boolean = true
}


/*
** Description of a local variable for function prototypes
** (used for debug information)
*/
class LocVar(
    val varname: String?,
    val startpc: Int,  /* first point where variable is active */
    val endpc: Int,  /* first point where variable is dead */
)

class UpValue(
    var name: String?,  /* upvalue name (for debug information) */
    val instack: Boolean,  /* whether it is in stack (register) */
    val idx: Int,  /* index of upvalue (in stack or in outer function's list) */
    val kind: Int, /* kind of corresponding variable */ // FIXME: use LuaValue
)

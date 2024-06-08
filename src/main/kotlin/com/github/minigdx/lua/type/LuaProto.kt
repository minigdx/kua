package com.github.minigdx.lua.type

import com.github.minigdx.lua.bytecode.LuaValue
import kotlin.reflect.KClass

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
    var k: Array<Konstant> = emptyArray()
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

class Konstant(val value: Any, val type: LuaValue)
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
    val kind: Int, /* kind of corresponding variable */
)

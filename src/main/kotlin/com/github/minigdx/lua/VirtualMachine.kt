package com.github.minigdx.lua

import com.github.minigdx.lua.bytecode.LuaOpcode
import com.github.minigdx.lua.bytecode.Utils
import com.github.minigdx.lua.bytecode.Utils.RA
import com.github.minigdx.lua.bytecode.Utils.RB
import com.github.minigdx.lua.bytecode.Utils.RBx
import com.github.minigdx.lua.type.LuaClosure
import com.github.minigdx.lua.type.LuaFalse
import com.github.minigdx.lua.type.LuaNil
import com.github.minigdx.lua.type.LuaTrue
import com.github.minigdx.lua.type.LuaValue

interface DebugVirtualMachine {
    fun step() = Unit

    fun pause() = Unit

    fun resume() = Unit

}

typealias Instruction = Int

class VirtualMachine : DebugVirtualMachine {

    fun run() = Unit

    // https://www.lua.org/source/5.4/lvm.c.html#luaV_execute
    fun execute(luaClosure: LuaClosure) {
        val stack = arrayOfNulls<LuaValue>(luaClosure.maxStackSize)

        var pc = 0
        while(true) {
            val i: Instruction = luaClosure.p.code[pc++].toInt()
            when(LuaOpcode.fromInstruction(i)) {
                LuaOpcode.MOVE -> {
                    stack[RA(i)] = stack[RB(i)]
                    continue
                }
                LuaOpcode.LOADK -> {
                    stack[RA(i)] = luaClosure.p.k[RBx(i)]
                    continue
                }
                LuaOpcode.LOADNIL -> {
                    var ra = RA(i)
                    var b = RB(i)
                    do {
                        stack[ra++] = LuaNil
                    } while (b-- > 0)
                    continue
                }
                LuaOpcode.GETUPVAL -> {
                    val ra = RA(i)
                    val rb = RB(i)
                    stack[ra] = luaClosure.upvals[rb]
                    continue
                }
                LuaOpcode.GETTABLE -> TODO()
                LuaOpcode.SETUPVAL -> TODO()
                LuaOpcode.SETTABLE -> TODO()
                LuaOpcode.NEWTABLE -> TODO()
                LuaOpcode.SELF -> TODO()
                LuaOpcode.ADD -> TODO()
                LuaOpcode.SUB -> TODO()
                LuaOpcode.MUL -> TODO()
                LuaOpcode.DIV -> TODO()
                LuaOpcode.MOD -> TODO()
                LuaOpcode.POW -> TODO()
                LuaOpcode.UNM -> TODO()
                LuaOpcode.NOT -> TODO()
                LuaOpcode.LEN -> TODO()
                LuaOpcode.CONCAT -> TODO()
                LuaOpcode.JMP -> TODO()
                LuaOpcode.EQ -> TODO()
                LuaOpcode.LT -> TODO()
                LuaOpcode.LE -> TODO()
                LuaOpcode.TEST -> TODO()
                LuaOpcode.TESTSET -> TODO()
                LuaOpcode.CALL -> TODO()
                LuaOpcode.TAILCALL -> TODO()
                LuaOpcode.RETURN -> TODO()
                LuaOpcode.FORLOOP -> TODO()
                LuaOpcode.FORPREP -> TODO()
                LuaOpcode.TFORLOOP -> TODO()
                LuaOpcode.SETLIST -> TODO()
                LuaOpcode.CLOSE -> TODO()
                LuaOpcode.CLOSURE -> TODO()
                LuaOpcode.VARARG -> TODO()
                LuaOpcode.LOADI -> TODO()
                LuaOpcode.LOADF -> TODO()
                LuaOpcode.LOADKX -> TODO()
                LuaOpcode.LOADFALSE -> {
                    stack[RA(i)] = LuaFalse
                }
                LuaOpcode.LFALSESKIP -> TODO()
                LuaOpcode.LOADTRUE -> {
                    stack[RA(i)] = LuaTrue
                }
                LuaOpcode.GETTABUP -> TODO()
                LuaOpcode.GETI -> TODO()
                LuaOpcode.GETFIELD -> TODO()
                LuaOpcode.SETTABUP -> TODO()
                LuaOpcode.SETI -> TODO()
                LuaOpcode.SETFIELD -> TODO()
                LuaOpcode.ADDI -> TODO()
                LuaOpcode.ADDK -> TODO()
                LuaOpcode.SUBK -> TODO()
                LuaOpcode.MULK -> TODO()
                LuaOpcode.MODK -> TODO()
                LuaOpcode.POWK -> TODO()
                LuaOpcode.DIVK -> TODO()
                LuaOpcode.IDIVK -> TODO()
                LuaOpcode.BANDK -> TODO()
                LuaOpcode.BORK -> TODO()
                LuaOpcode.BXORK -> TODO()
                LuaOpcode.SHRI -> TODO()
                LuaOpcode.SHLI -> TODO()
                LuaOpcode.IDIV -> TODO()
                LuaOpcode.BAND -> TODO()
                LuaOpcode.BOR -> TODO()
                LuaOpcode.BXOR -> TODO()
                LuaOpcode.SHL -> TODO()
                LuaOpcode.SHR -> TODO()
                LuaOpcode.MMBIN -> TODO()
                LuaOpcode.MMBINI -> TODO()
                LuaOpcode.MMBINK -> TODO()
                LuaOpcode.BNOT -> TODO()
                LuaOpcode.TBC -> TODO()
                LuaOpcode.EQK -> TODO()
                LuaOpcode.EQI -> TODO()
                LuaOpcode.LTI -> TODO()
                LuaOpcode.LEI -> TODO()
                LuaOpcode.GTI -> TODO()
                LuaOpcode.GEI -> TODO()
                LuaOpcode.RETURN0 -> TODO()
                LuaOpcode.RETURN1 -> TODO()
                LuaOpcode.TFORPREP -> TODO()
                LuaOpcode.TFORCALL -> TODO()
                LuaOpcode.VARARGPREP -> TODO()
                LuaOpcode.EXTRAARG -> TODO()
            }
        }
    }
}
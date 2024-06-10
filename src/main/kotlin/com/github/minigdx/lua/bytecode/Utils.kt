package com.github.minigdx.lua.bytecode

import com.github.minigdx.lua.bytecode.LuaOpcodeType.ABC
import com.github.minigdx.lua.bytecode.LuaOpcodeType.ABx

object Utils {

    fun opcode(i: Int): Int {
        return getArg(i, POS_OP, SIZE_OP)
    }

    fun RA(i: Int): Int {
        return getArg(i, POS_A, SIZE_A)
    }

    fun RB(i: Int): Int {
        ABC.check(i)
        return getArg(i, POS_B, SIZE_B)
    }

    fun RBx(i: Int): Int {
        ABx.check(i)
        return getArg(i, POS_Bx, SIZE_Bx)
    }


    private fun getArg(i: Int, pos: Int, size: Int): Int {
        return (i shr pos) and mask1(size, 0)
    }

    private fun mask1(size: Int, pos: Int): Int {
        return ((1 shl size) - 1) shl pos
    }

    private const val SIZE_OP = 7
    private const val SIZE_A = 8
    private const val SIZE_B = 8
    private const val SIZE_C = 8
    private const val SIZE_Bx = SIZE_C + SIZE_B + 1
    private const val SIZE_Ax = SIZE_Bx + SIZE_A
    private const val SIZE_sJ = SIZE_Bx + SIZE_A

    private const val POS_OP = 0
    private const val POS_A = POS_OP + SIZE_OP
    private const val POS_K = POS_A + SIZE_A
    private const val POS_B = POS_K + 1
    private const val POS_C = POS_B + SIZE_B
    private const val POS_Bx = POS_K
    private const val POS_Ax = POS_A
    private const val POS_sJ = POS_A


}
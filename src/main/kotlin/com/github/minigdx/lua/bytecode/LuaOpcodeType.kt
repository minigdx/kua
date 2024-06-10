package com.github.minigdx.lua.bytecode

import com.github.minigdx.lua.Instruction

/*===========================================================================
  We assume that instructions are unsigned 32-bit integers.
  All instructions have an opcode in the first 7 bits.
  Instructions can have the following formats:

       3 3 2 2 2 2 2 2 2 2 2 2 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0
       1 0 9 8 7 6 5 4 3 2 1 0 9 8 7 6 5 4 3 2 1 0 9 8 7 6 5 4 3 2 1 0
ABC          C(8)     |      B(8)     |k|     A(8)      |   Op(7)     |
ABx                Bx(17)               |     A(8)      |   Op(7)     |
AsBx              sBx (signed)(17)      |     A(8)      |   Op(7)     |
Ax                           Ax(25)                     |   Op(7)     |
sJ                           sJ (signed)(25)            |   Op(7)     |

  A signed argument is represented in excess K: the represented value is
  the written unsigned value minus K, where K is half the maximum for the
  corresponding unsigned argument.
===========================================================================*/
// https://www.lua.org/source/5.4/lopcodes.h.html
enum class LuaOpcodeType {
    Ax, ABC, ABx, AsBx, sJ;

    fun check(i: Instruction) {
        assert(LuaOpcode.fromInstruction(Utils.opcode(i)).type == this)
    }
}
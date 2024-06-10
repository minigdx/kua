package com.github.minigdx.lua.bytecode

import com.github.minigdx.lua.Instruction

/// https://github.com/tgarm/flutter-luavm
// https://github.com/search?q=lua+vm+pushed%3A%3E2024-01-01&type=repositories
enum class LuaOpcode(val type: LuaOpcodeType) {
    /*----------------------------------------------------------------------
      name          args    description                              mode
    ------------------------------------------------------------------------*/
    MOVE(LuaOpcodeType.ABC),        // A B     R[A] := R[B]
    LOADI(LuaOpcodeType.AsBx),      // A sBx   R[A] := sBx
    LOADF(LuaOpcodeType.AsBx),      // A sBx   R[A] := (lua_Number)sBx
    LOADK(LuaOpcodeType.ABx),       // A Bx    R[A] := K[Bx]
    LOADKX(LuaOpcodeType.ABx),      // A       R[A] := K[extra arg]
    LOADFALSE(LuaOpcodeType.ABC),   // A       R[A] := false
    LFALSESKIP(LuaOpcodeType.ABC),  // A       R[A] := false; pc++     (*)
    LOADTRUE(LuaOpcodeType.ABC),    // A       R[A] := true
    LOADNIL(LuaOpcodeType.ABC),     // A B     R[A], R[A+1], ..., R[A+B] := nil
    GETUPVAL(LuaOpcodeType.ABC),    // A B     R[A] := UpValue[B]
    SETUPVAL(LuaOpcodeType.ABC),    // A B     UpValue[B] := R[A]

    GETTABUP(LuaOpcodeType.ABC),    // A B C   R[A] := UpValue[B][K[C]:string]
    GETTABLE(LuaOpcodeType.ABC),    // A B C   R[A] := R[B][R[C]]
    GETI(LuaOpcodeType.ABC),        // A B C   R[A] := R[B][C]
    GETFIELD(LuaOpcodeType.ABC),    // A B C   R[A] := R[B][K[C]:string]

    SETTABUP(LuaOpcodeType.ABC),    // A B C   UpValue[A][K[B]:string] := RK(C)
    SETTABLE(LuaOpcodeType.ABC),    // A B C   R[A][R[B]] := RK(C)
    SETI(LuaOpcodeType.ABC),        // A B C   R[A][B] := RK(C)
    SETFIELD(LuaOpcodeType.ABC),    // A B C   R[A][K[B]:string] := RK(C)

    NEWTABLE(LuaOpcodeType.ABC),    // A B C k R[A] := {}

    SELF(LuaOpcodeType.ABC),        // A B C   R[A+1] := R[B]; R[A] := R[B][RK(C):string]

    ADDI(LuaOpcodeType.ABC),        // A B sC  R[A] := R[B] + sC

    ADDK(LuaOpcodeType.ABC),        // A B C   R[A] := R[B] + K[C]:number
    SUBK(LuaOpcodeType.ABC),        // A B C   R[A] := R[B] - K[C]:number
    MULK(LuaOpcodeType.ABC),        // A B C   R[A] := R[B] * K[C]:number
    MODK(LuaOpcodeType.ABC),        // A B C   R[A] := R[B] % K[C]:number
    POWK(LuaOpcodeType.ABC),        // A B C   R[A] := R[B] ^ K[C]:number
    DIVK(LuaOpcodeType.ABC),        // A B C   R[A] := R[B] / K[C]:number
    IDIVK(LuaOpcodeType.ABC),       // A B C   R[A] := R[B] // K[C]:number

    BANDK(LuaOpcodeType.ABC),       // A B C   R[A] := R[B] & K[C]:integer
    BORK(LuaOpcodeType.ABC),        // A B C   R[A] := R[B] | K[C]:integer
    BXORK(LuaOpcodeType.ABC),       // A B C   R[A] := R[B] ~ K[C]:integer

    SHRI(LuaOpcodeType.ABC),        // A B sC  R[A] := R[B] >> sC
    SHLI(LuaOpcodeType.ABC),        // A B sC  R[A] := sC << R[B]

    ADD(LuaOpcodeType.ABC),         // A B C   R[A] := R[B] + R[C]
    SUB(LuaOpcodeType.ABC),         // A B C   R[A] := R[B] - R[C]
    MUL(LuaOpcodeType.ABC),         // A B C   R[A] := R[B] * R[C]
    MOD(LuaOpcodeType.ABC),         // A B C   R[A] := R[B] % R[C]
    POW(LuaOpcodeType.ABC),         // A B C   R[A] := R[B] ^ R[C]
    DIV(LuaOpcodeType.ABC),         // A B C   R[A] := R[B] / R[C]
    IDIV(LuaOpcodeType.ABC),        // A B C   R[A] := R[B] // R[C]

    BAND(LuaOpcodeType.ABC),        // A B C   R[A] := R[B] & R[C]
    BOR(LuaOpcodeType.ABC),         // A B C   R[A] := R[B] | R[C]
    BXOR(LuaOpcodeType.ABC),        // A B C   R[A] := R[B] ~ R[C]
    SHL(LuaOpcodeType.ABC),         // A B C   R[A] := R[B] << R[C]
    SHR(LuaOpcodeType.ABC),         // A B C   R[A] := R[B] >> R[C]

    MMBIN(LuaOpcodeType.ABC),       // A B C   call C metamethod over R[A] and R[B]    (*)
    MMBINI(LuaOpcodeType.ABC),      // A sB C k        call C metamethod over R[A] and sB
    MMBINK(LuaOpcodeType.ABC),      // A B C k         call C metamethod over R[A] and K[B]

    UNM(LuaOpcodeType.ABC),         // A B     R[A] := -R[B]
    BNOT(LuaOpcodeType.ABC),        // A B     R[A] := ~R[B]
    NOT(LuaOpcodeType.ABC),         // A B     R[A] := not R[B]
    LEN(LuaOpcodeType.ABC),         // A B     R[A] := #R[B] (length operator)

    CONCAT(LuaOpcodeType.ABC),      // A B     R[A] := R[A].. ... ..R[A + B - 1]

    CLOSE(LuaOpcodeType.ABC),       // A       close all upvalues >= R[A]
    TBC(LuaOpcodeType.ABC),         // A       mark variable A "to be closed"
    JMP(LuaOpcodeType.sJ),          // sJ      pc += sJ
    EQ(LuaOpcodeType.ABC),          // A B k   if ((R[A] == R[B]) ~= k) then pc++
    LT(LuaOpcodeType.ABC),          // A B k   if ((R[A] <  R[B]) ~= k) then pc++
    LE(LuaOpcodeType.ABC),          // A B k   if ((R[A] <= R[B]) ~= k) then pc++

    EQK(LuaOpcodeType.ABC),         // A B k   if ((R[A] == K[B]) ~= k) then pc++
    EQI(LuaOpcodeType.ABC),         // A sB k  if ((R[A] == sB) ~= k) then pc++
    LTI(LuaOpcodeType.ABC),         // A sB k  if ((R[A] < sB) ~= k) then pc++
    LEI(LuaOpcodeType.ABC),         // A sB k  if ((R[A] <= sB) ~= k) then pc++
    GTI(LuaOpcodeType.ABC),         // A sB k  if ((R[A] > sB) ~= k) then pc++
    GEI(LuaOpcodeType.ABC),         // A sB k  if ((R[A] >= sB) ~= k) then pc++

    TEST(LuaOpcodeType.ABC),        // A k     if (not R[A] == k) then pc++
    TESTSET(LuaOpcodeType.ABC),     // A B k   if (not R[B] == k) then pc++ else R[A] := R[B] (*)

    CALL(LuaOpcodeType.ABC),        // A B C   R[A], ... ,R[A+C-2] := R[A](R[A+1], ... ,R[A+B-1])
    TAILCALL(LuaOpcodeType.ABC),    // A B C k return R[A](R[A+1], ... ,R[A+B-1])

    RETURN(LuaOpcodeType.ABC),      // A B C k return R[A], ... ,R[A+B-2]      (see note)
    RETURN0(LuaOpcodeType.ABC),     //           return
    RETURN1(LuaOpcodeType.ABC),     // A         return R[A]

    FORLOOP(LuaOpcodeType.ABx),     // A Bx    R[A] += R[A+2];
    // if R[A] <?= R[A+1] then { pc+=Bx; R[A+3]=R[A] }

    FORPREP(LuaOpcodeType.ABx),     // A Bx    R[A] -= R[A+2]; pc+=Bx

    TFORPREP(LuaOpcodeType.ABx),    // A Bx    create upvalue to refer to base
    TFORCALL(LuaOpcodeType.ABC),    // A C     R[A+3], ... ,R[A+3+C-2] := R[A](R[A+1], R[A+2])
    TFORLOOP(LuaOpcodeType.ABx),    // A Bx    if R[A+1] ~= nil then { R[A]=R[A+1]; pc += Bx }

    SETLIST(LuaOpcodeType.ABC),     // A B C   R[A][C*FPF+i] := R[A+i], 1 <= i <= B

    CLOSURE(LuaOpcodeType.ABx),     // A Bx    R[A] := closure(KPROTO[Bx], R[A], ... ,R[A+n])

    VARARG(LuaOpcodeType.ABC),      // A C     R[A], R[A+1], ..., R[A+C-2] = vararg

    VARARGPREP(LuaOpcodeType.ABC),  // A       (adjust varargs)
    EXTRAARG(LuaOpcodeType.Ax)      // Ax      extra (larger) argument for previous opcode

    ;

    companion object {
        fun fromInstruction(i: Instruction): LuaOpcode {
            val opcodeValue = Utils.opcode(i)
            return entries.toTypedArray().get(opcodeValue)
        }
    }
}
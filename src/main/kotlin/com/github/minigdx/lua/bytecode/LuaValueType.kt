package com.github.minigdx.lua.bytecode

private fun makeVariant(t: Int, v: Int): Int {
    return (t or (v shl 4))
}

enum class LuaValueType(val value: Int) {
    LUA_NILL(makeVariant(LuaType.LUA_TNIL.value, 0)),
    LUA_FALSE(makeVariant(LuaType.LUA_TBOOLEAN.value, 0)),
    LUA_TRUE(makeVariant(LuaType.LUA_TBOOLEAN.value, 1)),
    LUA_NUMBER_INT(makeVariant(LuaType.LUA_TNUMBER.value, 0)),
    LUA_NUMBER_FLOAT(makeVariant(LuaType.LUA_TNUMBER.value, 1)),
    LUA_SHORT_STRING(makeVariant(LuaType.LUA_TSTRING.value, 0)),
    LUA_LONG_STRING(makeVariant(LuaType.LUA_TSTRING.value, 1)),
}
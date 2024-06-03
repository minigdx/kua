package com.github.minigdx.lua.type

class LuaTopState {
    var p: Int = 0
}

class LuaState {

    val top: LuaTopState = LuaTopState()
}
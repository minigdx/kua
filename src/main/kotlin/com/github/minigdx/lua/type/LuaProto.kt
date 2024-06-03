package com.github.minigdx.lua.type

class LuaProto(
    val sizeupvalues: Int = -1,
    var source: String? = null
) {
    var code: ByteArray = byteArrayOf()
    var sizecode: Int = -1
    var lineDefined: Int = -1
    var lastLineDefined: Int = -1
    var isVarArgs: Boolean = false
    var numparams: Int = -1
    var maxStackSize: Int = -1
}

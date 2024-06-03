package com.github.minigdx.lua.type

class LuaPrototype {
    companion object {
        fun of(): LuaPrototype = LuaPrototype()
    }
}

class LuaClosure(
    var sourceName: String? = null,
    nbUpVals: Int,
) {

    var lineDefined: Int = -1
    var lastLineDefined: Int = -1
    var isVarArgs: Boolean = false
    var nbParams: Int = -1
    var maxStackSize: Int = -1

    constructor(nbUpVals: Int) : this(
        sourceName = null,
        nbUpVals = nbUpVals
    )

    var p = LuaProto()
    val upvals: Array<Any?> = Array(nbUpVals) { null }
}


fun byteArrayToDoubleLittleEndian(bytes: ByteArray): Double {
    require(bytes.size == 8) { "Byte array must be exactly 8 bytes long to convert to a Double" }

    val longBits = (bytes[0].toLong() and 0xFF) or
            ((bytes[1].toLong() and 0xFF) shl 8) or
            ((bytes[2].toLong() and 0xFF) shl 16) or
            ((bytes[3].toLong() and 0xFF) shl 24) or
            ((bytes[4].toLong() and 0xFF) shl 32) or
            ((bytes[5].toLong() and 0xFF) shl 40) or
            ((bytes[6].toLong() and 0xFF) shl 48) or
            ((bytes[7].toLong() and 0xFF) shl 56)

    return Double.fromBits(longBits)
}

// Usage example
fun main() {
    val byteArray = byteArrayOf(0, 0, 0, 0, 0, 40, 119, 64) // Example byte array in little-endian format
    val doubleValue = byteArrayToDoubleLittleEndian(byteArray)
    println("Double value: $doubleValue") // Output: Double value: 370.5
}
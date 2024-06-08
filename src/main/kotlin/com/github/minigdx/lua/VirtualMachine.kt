package com.github.minigdx.lua

interface DebugVirtualMachine {
    fun step() = Unit

    fun pause() = Unit

    fun resume() = Unit

}

class VirtualMachine(private val ast: Chunk) : DebugVirtualMachine {

    fun run() = Unit
}
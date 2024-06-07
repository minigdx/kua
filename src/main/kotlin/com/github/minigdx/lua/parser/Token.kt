package com.github.minigdx.lua.parser

data class Token(
    val type: TokenType,
    /**
     * Value of the token ~ the text related to the token
     */
    val value: String,
    /**
     * Absolute position of the token in the text.
     */
    val position: Int,
    /**
     * Line number containing the token (starting to 1)
     */
    val line: Int,
    /**
     * Column number containing the token (starting to 1)
     */
    val column: Int
)
package com.github.minigdx.lua.parser

class SyntaxException(message: String, val line: Int, val column: Int) : RuntimeException(message) {
    constructor(currentToken: Token, expectedToken: TokenType): this(
        "Expected token $expectedToken but found ${currentToken.type}. " +
                "Line: ${currentToken.line}, Column: ${currentToken.column}",
        currentToken.line,
        currentToken.column
    )
}
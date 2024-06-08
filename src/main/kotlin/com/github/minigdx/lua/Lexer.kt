package com.github.minigdx.lua

import com.github.minigdx.lua.parser.Token
import com.github.minigdx.lua.parser.TokenType

class Lexer(private val str: String) {
    private var position = 0
    private var line = 1
    private var column = 1
    private val input = skipShellBang(str)

    private fun skipShellBang(str: String): String {
        if(str.startsWith("#!")) {
            line++
            return str.lines().drop(1).joinToString("\n")
        } else {
            return str
        }
    }

    private fun nextChar(): Char? {
        return if (position < input.length) {
            column++
            input[position++]
        } else null
    }

    private fun peekChar(): Char? = if (position < input.length) input[position] else null

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()
        var currentChar: Char?

        while (true) {
            currentChar = nextChar() ?: break
            when {
                currentChar.isWhitespace() -> {
                    if (currentChar == '\n') {
                        line++
                        column = 1
                    }
                    continue
                }
                currentChar.isDigit() -> {
                    val start = position - 1
                    while (peekChar()?.isDigit() == true) nextChar()
                    if(peekChar() == '.') {
                        nextChar()
                        while (peekChar()?.isDigit() == true) nextChar()
                    }
                    val value = input.substring(start, position)
                    tokens.add(Token(TokenType.NUMBER, value, start, line, column - value.length))
                }

                currentChar == '_' ||
                currentChar.isLetter() -> {
                    val start = position - 1
                    while (peekChar()?.isLetterOrDigit() == true) nextChar()
                    val text = input.substring(start, position)
                    val type = TokenType.fromKeyword(text) ?: TokenType.IDENTIFIER
                    tokens.add(Token(type, text, start, line, column- text.length))
                }

                currentChar == '\'' -> {
                    val start = position - 1
                    while (peekChar() != '\'' && peekChar() != null) nextChar()
                    nextChar() // consume closing quote
                    val value = input.substring(start, position)
                    tokens.add(Token(TokenType.STRING, value, start, line, column - value.length))
                }

                currentChar == '"' -> {
                    val start = position - 1
                    while (peekChar() != '"' && peekChar() != null) nextChar()
                    nextChar() // consume closing quote
                    val value = input.substring(start, position)
                    tokens.add(Token(TokenType.STRING, value, start, line, column - value.length))
                }

                else -> {
                    val start = position - 1
                    var text = currentChar.toString()
                    while (peekChar() != null && TokenType.fromSymbol(text + (peekChar())) != null) {
                        text += nextChar().toString()
                    }

                    val type = TokenType.fromSymbol(text) ?: TODO("Symbol not found for $text. $line:$column")
                    if(type == TokenType.COMMENT) {
                        while(peekChar() != '\n') { text += nextChar().toString() }
                    }
                    tokens.add(Token(type, text, start, line, column - text.length))
                }
            }
        }

        tokens.add(Token(TokenType.EOF, "", position, line, column))
        return tokens
    }
}

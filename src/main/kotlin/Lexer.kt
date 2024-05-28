import TokenType.Companion.KEYWORDS

enum class TokenType {
    AND, BREAK, DO, ELSE, ELSEIF, END,
    FALSE, FOR, FUNCTION, GOTO, IF, IN,
    LOCAL, NIL, NOT, OR, REPEAT, RETURN,
    THEN, TRUE, UNTIL, WHILE,
    IDENTIFIER,
    NUMBER,
    STRING,
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    MODULO,
    EXPONENT,
    LENGTH,
    BITWISE_AND,
    BITWISE_NOT,
    BITWISE_OR,
    SHIFT_LEFT,
    SHIFT_RIGHT,
    FLOOR_DIVIDE,
    EQUALS,
    NOT_EQUALS,
    LESS_THAN_EQUAL,
    GREATER_THAN_EQUAL,
    LESS_THAN,
    GREATER_THAN,
    ASSIGN,
    OPEN_PARENTHESIS,
    CLOSE_PARENTHESIS,
    OPEN_BRACE,
    CLOSE_BRACE,
    OPEN_BRACKET,
    CLOSE_BRACKET,
    DOUBLE_COLON,
    SEMICOLON,
    COLON,
    COMMA,
    DOT,
    CONCAT,
    VARARGS,
    EOF;

    companion object {

        private val KEYWORDS_TOKEN = setOf(
            AND, BREAK, DO, ELSE, ELSEIF, END,
            FALSE, FOR, FUNCTION, GOTO, IF, IN,
            LOCAL, NIL, NOT, OR, REPEAT, RETURN,
            THEN, TRUE, UNTIL, WHILE,
        )

        private val KEYWORD_TO_TOKEN = mapOf(
            "and" to AND,
            "break" to BREAK,
            "do" to DO,
            "else" to ELSE,
            "elseif" to ELSEIF,
            "end" to END,
            "false" to FALSE,
            "for" to FOR,
            "function" to FUNCTION,
            "goto" to GOTO,
            "if" to IF,
            "in" to IN,
            "local" to LOCAL,
            "nil" to NIL,
            "not" to NOT,
            "or" to OR,
            "repeat" to REPEAT,
            "return" to RETURN,
            "then" to THEN,
            "true" to TRUE,
            "until" to UNTIL,
            "while" to WHILE
        )

        private val SYMBOL_TO_TOKEN = mapOf(
            "+" to PLUS,
            "-" to MINUS,
            "*" to MULTIPLY,
            "/" to DIVIDE,
            "%" to MODULO,
            "^" to EXPONENT,
            "#" to LENGTH,
            "&" to BITWISE_AND,
            "~" to BITWISE_NOT,
            "|" to BITWISE_OR,
            "<<" to SHIFT_LEFT,
            ">>" to SHIFT_RIGHT,
            "//" to FLOOR_DIVIDE,
            "==" to EQUALS,
            "~=" to NOT_EQUALS,
            "<=" to LESS_THAN_EQUAL,
            ">=" to GREATER_THAN_EQUAL,
            "<" to LESS_THAN,
            ">" to GREATER_THAN,
            "=" to ASSIGN,
            "(" to OPEN_PARENTHESIS,
            ")" to CLOSE_PARENTHESIS,
            "{" to OPEN_BRACE,
            "}" to CLOSE_BRACE,
            "[" to OPEN_BRACKET,
            "]" to CLOSE_BRACKET,
            "::" to DOUBLE_COLON,
            ";" to SEMICOLON,
            ":" to COLON,
            "," to COMMA,
            "." to DOT,
            ".." to CONCAT,
            "..." to VARARGS
        )

        val KEYWORDS = KEYWORD_TO_TOKEN.keys

        fun fromKeyword(keyword: String): TokenType? {
            return KEYWORD_TO_TOKEN[keyword]
        }

        fun fromSymbol(symbol: String): TokenType? {
            return SYMBOL_TO_TOKEN[symbol]
        }
    }
}

data class Token(val type: TokenType, val value: String, val position: Int, val line: Int, val column: Int)

class Lexer(private val input: String) {
    private var position = 0
    private var line = 1
    private var column = 1

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
                    val value = input.substring(start, position)
                    tokens.add(Token(TokenType.NUMBER, value, start, line, column - value.length))
                }

                currentChar.isLetter() -> {
                    val start = position - 1
                    while (peekChar()?.isLetterOrDigit() == true) nextChar()
                    val text = input.substring(start, position)
                    val type = TokenType.fromKeyword(text) ?: TokenType.IDENTIFIER
                    tokens.add(Token(type, text, start, line, column- text.length))
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

                    tokens.add(Token(TokenType.fromSymbol(text)!!, text, start, line, column - text.length))
                }
            }
        }

        tokens.add(Token(TokenType.EOF, "", position, line, column))
        return tokens
    }
}

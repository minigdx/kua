import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ParserTest {

    @Test
    fun testSimpleExpression() {
        val input = ":: test ::"
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()
        ast.print()
    }

    @Test
    fun testSyntaxError() {
        val input = "function 321() end"
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        try {
            parser.parse()
            fail()
        } catch (ex: SyntaxException) {
            assertEquals(1, ex.line)
            assertEquals(10, ex.column)
        }
    }

    @Test
    fun testLabelAndGoto() {
        val input =
            """:: test ::
               goto test 
            """.trimIndent()
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()
        ast.print()
    }

    @Test
    fun testRepeat() {
        val input =
            """repeat
                ;
               until true 
            """.trimIndent()
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()
        ast.print()
    }

    @Test
    fun testFunction() {
        val input =
            """function test() 
                ;
                end
            """.trimIndent()
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()
        ast.print()
    }

    @Test
    fun testWhile() {
        val input =
            """while true do
                ;
               end
            """.trimIndent()
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()
        ast.print()
    }

    @Test
    fun testIf() {
        val input =
            """if true then
                ;
                end
            """.trimIndent()
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()
        ast.print()
    }

    @Test
    fun testIfElse() {
        val input =
            """if true then
                ;
                else
                ;
                end
            """.trimIndent()
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()
        ast.print()
    }

    @Test
    fun testIfElseifElse() {
        val input =
            """if true then
                ;
                elseif false then
                ;
                else
                ;
                end
            """.trimIndent()
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()
        ast.print()
    }

    @Test
    fun testFor() {
        val input =
            """for i=1,5 do
                ;
               end
            """.trimIndent()
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()
        ast.print()
    }
}

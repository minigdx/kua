import com.github.minigdx.lua.Lexer
import com.github.minigdx.lua.Parser
import com.github.minigdx.lua.parser.SyntaxException
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

    @Test
    fun testAsign() {
        val input = """a = 3"""
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()
        ast.print()
    }

    @Test
    fun testMultipleAsign() {
        val input = """a, b = 3, 2"""
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()
        ast.print()
    }

    @Test
    fun testFunctionCall() {
        val input = """print(3, 2)"""
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()
        ast.print()
    }

    @Test
    fun testVarByIndex() {
        val input = """toto[3] = 2"""
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()
        ast.print()
    }

    @Test
    fun testVarByField() {
        val input = """toto.tata = 2"""
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()
        ast.print()
    }

    @Test
    fun testVarByField2() {
        val input = """toto.tata.titi = 2"""
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()
        ast.print()
    }

    @Test
    fun testTable() {
        val input = """toto = {tata = "hello", [2] = 4, }"""
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()
        ast.print()
    }

    @Test
    fun testNumber() {
        val input = """a = 3.2"""
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()
        ast.print()
    }

    @Test
    fun testLocalAssign() {
        val input = """local a = 3.2"""
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()
        ast.print()
    }

    @Test
    fun testLocalAssignWithAttrib() {
        val input = """local a<read> = 3.2"""
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()
        ast.print()
    }

    @Test
    fun testLocalFunction() {
        val input = """local function a() end"""
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()
        ast.print()
    }
}

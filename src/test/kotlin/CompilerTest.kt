import kotlin.test.Test

class CompilerTest {

    @Test
    fun testSimpleExpression() {
        val input = "function toto() ; end"
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens)
        val ast = parser.parse()

        Compiler().compile(ast)
    }
}
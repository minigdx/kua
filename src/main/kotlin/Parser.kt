sealed interface ASTNode {
    fun print(prefix: String = "", isTail: Boolean = true, name: String = "") {
        val node = this
        val connector = if (isTail) "└── $name" else "├── $name"
        val childPrefix = if (isTail) "    " else "│   "


        when (node) {
            is Numeral -> println("$prefix$connector Number(${node.number})")
            is LiteralString -> println("$prefix$connector String(${node.string})")
            is IdentifierNode -> println("$prefix$connector Identifier(${node.name})")
            is BinaryOperationNode -> {
                println("$prefix$connector BinaryOperation(${node.operator})")
                node.left.print(prefix + childPrefix, false)
                node.right.print(prefix + childPrefix, true)
            }

            is Block -> {
                println("$prefix$connector Block(${node.statements.size} statements):")
                node.statements.forEachAndLast { it, isLast ->
                    it.print(prefix + childPrefix, isLast)
                }
            }

            is Chunk -> {
                println("$prefix$connector Chunk:")
                node.block.print(prefix + childPrefix, true)
            }

            Nop -> println("$prefix$connector ;")
            is Assignment -> TODO()
            is AttNameList -> TODO()
            Break -> TODO()
            is Do -> TODO()
            is For -> {
                println("$prefix$connector For")
                node.name.print(prefix + childPrefix, false, "Name:")
                node.init.print(prefix + childPrefix, false, "Init:")
                node.until.print(prefix + childPrefix, false, "Until:")
                node.step?.print(prefix + childPrefix, false, "Step:")
                node.block.print(prefix + childPrefix, true, "Block:")
            }

            is ForName -> {
                println("$prefix$connector ForName")
                node.names.print(prefix + childPrefix, false, "Name:")
                node.explist.print(prefix + childPrefix, true, "Name:")
            }

            is Func -> TODO()
            is Funcname -> {
                println("$prefix$connector Funcname")
                node.root.print(prefix + childPrefix, false)
                node.children.forEachAndLast { it, isLast ->
                    it.print(prefix + childPrefix, isLast && node.method == null)
                }
                node.method?.print(prefix + childPrefix, true)
            }
            is Goto -> println("$prefix$connector Goto(${node.name})")
            is If -> {
                println("$prefix$connector If")
                val hasElseIf = node.elif.isNotEmpty()
                val hasElse = node.or != null

                node.exp.print(prefix + childPrefix, false, "Exp:")
                node.block.print(prefix + childPrefix, !hasElseIf && !hasElse, "Block:")

                node.elif.forEachAndLast { (exp, block), isLast ->
                    exp.print(prefix + childPrefix, false, "(Elseif) Exp:")
                    block.print(prefix + childPrefix, isLast && !hasElse, "(Elseif) Block:")
                }

                node.or?.print(prefix + childPrefix, true, "(Else) Block:")
            }

            is LocalAssigment -> TODO()
            is LocalFunc -> TODO()
            is Repeat -> {
                println("$prefix$connector Repeat Until")
                node.block.print(prefix + childPrefix, false)
                node.exp.print(prefix + childPrefix, true)
            }

            is Retstat -> TODO()
            is While -> {
                println("$prefix$connector While Do End")
                node.exp.print(prefix + childPrefix, false)
                node.block.print(prefix + childPrefix, true)
            }

            EmptyName -> TODO()
            is StrName -> println("$prefix$connector ${node.value}")
            is Label -> println("$prefix$connector Label(${node.name})")
            False -> println("$prefix$connector False")
            Nil -> println("$prefix$connector Nil")
            ParVarArgs -> TODO()
            True -> println("$prefix$connector True")
            is NameList -> TODO()
            is ExpList -> {
                println("$prefix$connector ExpList")
                node.expList.forEachAndLast { exp, isLast ->
                    exp.print(prefix + childPrefix, isLast)
                }
            }

            is Funcbody -> {
                println("$prefix$connector Funcbody")
                node.args?.print(prefix + childPrefix, false)
                node.block.print(prefix + childPrefix, true)
            }
            is FunctionDef -> {
                println("$prefix$connector FunctionDef")
                node.name.print(prefix + childPrefix, false, "Name")
                node.body.print(prefix + childPrefix, true, "Body")
            }
            is Function.FunctionCall -> TODO()
            is Function.MethodCall -> TODO()
            is ParNamelist -> {
                node.namelist.nameList.forEachAndLast { n, isLast ->
                    n.print(prefix + childPrefix, isLast && node.vargs == null)
                }
                node.vargs?.print(prefix + childPrefix, true)
            }
        }
    }

    private fun <T> List<T>.forEachAndLast(block: (T, Boolean) -> Unit) {
        this.dropLast(1).forEach {
            block(it, false)
        }
        this.lastOrNull()?.let { block(it, true) }
    }
}

class SyntaxException( message: String, val line: Int, val column: Int) : RuntimeException(message)
class Parser(private val tokens: List<Token>) {
    private var position = 0

    private fun currentToken(): Token = tokens[position]

    private fun nextToken(): Token = tokens[++position]

    private fun peekToken(): Token = tokens[position + 1]

    private fun expectToken(type: TokenType): Token {
        val token = currentToken()
        if (token.type != type) throw SyntaxException(
            "Expected token $type but found ${token.type}. Line: ${token.line}, Column: ${token.column}",
            token.line,
            token.column
        )
        nextToken()
        return token
    }

    fun parse(): Chunk {

        return Chunk(expectBlock())
    }

    // exp ::=  nil | false | true | Numeral | LiteralString | ‘...’ | functiondef |
    //		 prefixexp | tableconstructor | exp binop exp | unop exp
    fun expectExp(): Exp {
        val token = currentToken()
        return when (token.type) {
            TokenType.NIL -> Nil
            TokenType.FALSE -> False
            TokenType.TRUE -> True
            TokenType.NUMBER -> Numeral(token.value)
            TokenType.STRING -> LiteralString(token.value)
            TokenType.VARARGS -> ParVarArgs
            else -> TODO("${token.type} with ${token.value} is not supported yet")
        }.also {
            nextToken()
        }
    }

    fun parseStatement(token: Token): Statement? {
        return null
    }

    fun expectBlock(): Block {

        val statements = mutableListOf<Statement>()
        while (true) {
            val token = currentToken()
            val statement = when (token.type) {
                TokenType.BREAK -> {
                    expectToken(TokenType.BREAK)
                    Break
                }

                TokenType.DO -> {
                    expectToken(TokenType.DO)
                    val block = expectBlock()
                    expectToken(TokenType.END)
                    Do(block)
                }

                TokenType.FOR -> {
                    expectToken(TokenType.FOR)
                    val name = expectToken(TokenType.IDENTIFIER)
                    // for Name ‘=’ exp ‘,’ exp [‘,’ exp] do block end |
                    if (currentToken().type == TokenType.ASSIGN) {
                        expectToken(TokenType.ASSIGN)
                        val init = expectExp()
                        expectToken(TokenType.COMMA)
                        val until = expectExp()
                        val step = if (currentToken().type == TokenType.COMMA) {
                            expectToken(TokenType.COMMA)
                            expectExp()
                        } else {
                            null
                        }
                        expectToken(TokenType.DO)
                        val block = expectBlock()
                        expectToken(TokenType.END)
                        For(StrName(name.value), init, until, step, block)
                    } else {
                        val names = mutableListOf(name)
                        // for namelist in explist do block end |
                        while (currentToken().type != TokenType.IN) {
                            expectToken(TokenType.COMMA)
                            names.add(expectToken(TokenType.IDENTIFIER))
                        }
                        expectToken(TokenType.IN)
                        val expList = expectExplist()
                        expectToken(TokenType.DO)
                        val block = expectBlock()
                        expectToken(TokenType.END)
                        ForName(NameList(names.map { StrName(it.value) }), expList, block)
                    }
                }

                TokenType.FUNCTION -> {
                    // function funcname funcbody
                    expectToken(TokenType.FUNCTION)
                    val funcname = expectFuncname()
                    val funcbody = expectFuncbody()
                    FunctionDef(funcname, funcbody)
                }

                TokenType.GOTO -> {
                    expectToken(TokenType.GOTO)
                    val id = expectToken(TokenType.IDENTIFIER)
                    Goto(StrName(id.value))
                }

                TokenType.IF -> {
                    // if exp then block {elseif exp then block} [else block] end
                    expectToken(TokenType.IF)
                    val exp = expectExp()
                    expectToken(TokenType.THEN)
                    val block = expectBlock()
                    val elif = mutableListOf<Pair<Exp, Block>>()
                    while (currentToken().type == TokenType.ELSEIF) {
                        expectToken(TokenType.ELSEIF)
                        val elifExp = expectExp()
                        expectToken(TokenType.THEN)
                        val elifBlock = expectBlock()
                        elif.add(elifExp to elifBlock)
                    }
                    val or = if (currentToken().type == TokenType.ELSE) {
                        expectToken(TokenType.ELSE)
                        expectBlock()
                    } else {
                        null
                    }
                    expectToken(TokenType.END)
                    If(exp, block, elif, or)
                }

                TokenType.LOCAL -> {
                    // local function Name funcbody |
                    //	local attnamelist [‘=’ explist]
                    expectToken(TokenType.LOCAL)
                    if (currentToken().type == TokenType.FUNCTION) {
                        expectToken(TokenType.FUNCTION)
                        val name = expectToken(TokenType.IDENTIFIER)
                        val funcbody = expectFuncbody()
                        TODO()
                    } else {
                        TODO()
                    }
                }

                TokenType.REPEAT -> {
                    expectToken(TokenType.REPEAT)
                    val block = expectBlock()
                    expectToken(TokenType.UNTIL)
                    val exp = expectExp()
                    Repeat(block, exp)
                }

                TokenType.RETURN -> {
                    val expList = expectExplist()
                    Retstat(expList)
                }

                TokenType.WHILE -> {
                    expectToken(TokenType.WHILE)
                    val exp = expectExp()
                    expectToken(TokenType.DO)
                    val block = expectBlock()
                    expectToken(TokenType.END)
                    While(exp, block)
                }

                TokenType.DOUBLE_COLON -> {
                    expectToken(TokenType.DOUBLE_COLON)
                    val id = expectToken(TokenType.IDENTIFIER)
                    expectToken(TokenType.DOUBLE_COLON)
                    Label(StrName(id.value))
                }

                TokenType.SEMICOLON -> {
                    expectToken(TokenType.SEMICOLON)
                    Nop
                }

                else -> null
            }

            when (statement) {
                is Statement -> statements.add(statement)
                is Retstat -> return Block(statements, statement)
                else -> return Block(statements)
            }
        }
    }


    // funcname ::= Name {‘.’ Name} [‘:’ Name]
    private fun expectFuncname(): Funcname {
        val id = StrName(expectToken(TokenType.IDENTIFIER).value)

        val children = mutableListOf<Name>()
        var nextToken = currentToken()
        while (nextToken.type == TokenType.DOT) {
            expectToken(TokenType.DOT)
            children.add(StrName(expectToken(TokenType.IDENTIFIER).value))
            nextToken = currentToken()
        }
        val method = if (currentToken().type == TokenType.DOUBLE_COLON) {
            expectToken(TokenType.DOUBLE_COLON)
            StrName(expectToken(TokenType.IDENTIFIER).value)
        } else {
            null
        }

        return Funcname(id, children, method)
    }

    // funcbody ::= ‘(’ [parlist] ‘)’ block end
    private fun expectFuncbody(): Funcbody {
        expectToken(TokenType.OPEN_PARENTHESIS)
        val parList = if(currentToken().type != TokenType.CLOSE_PARENTHESIS) {
             expectParList()
        } else {
            null
        }
        expectToken(TokenType.CLOSE_PARENTHESIS)
        val block = expectBlock()
        expectToken(TokenType.END)
        return Funcbody(parList, block)
    }

    // parlist ::= namelist [‘,’ ‘...’] | ‘...’
    private fun expectParList(): Parlist {
        return if (currentToken().type == TokenType.VARARGS) {
            expectToken(TokenType.VARARGS)
            ParVarArgs
        } else {
            val nameList = expectNamelist()
            val varargs = if (currentToken().type == TokenType.COMMA) {
                expectToken(TokenType.VARARGS)
                ParVarArgs
            } else {
                null
            }
            return ParNamelist(nameList, varargs)
        }
    }

    private fun expectExplist(): ExpList {
        TODO()
    }

    // namelist ::= Name {‘,’ Name}
    private fun expectNamelist(): NameList {
        val first = StrName(expectToken(TokenType.IDENTIFIER).value)
        val tail = mutableListOf<Name>()
        while (currentToken().type == TokenType.COMMA && peekToken().type == TokenType.IDENTIFIER) {
            expectToken(TokenType.COMMA)
            tail.add(StrName(expectToken(TokenType.IDENTIFIER).value))
        }
        return NameList(listOf(first) + tail)
    }

}

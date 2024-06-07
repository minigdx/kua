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
            is Assignment -> {
                println("$prefix$connector =")
                node.varlist.print(prefix + childPrefix, false)
                node.explist.print(prefix + childPrefix, true)
            }

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

            is FunctionDefinition -> {
                println("$prefix$connector FunctionDef")
                node.name.print(prefix + childPrefix, false, "Name")
                node.body.print(prefix + childPrefix, true, "Body")
            }

            is FunctionCall -> {
                println("$prefix$connector FunctionCall")
                node.prefixexp.print(prefix + childPrefix, false)
                node.args.print(prefix + childPrefix, true)
            }

            is MethodCall -> TODO()
            is ParNamelist -> {
                node.namelist.nameList.forEachAndLast { n, isLast ->
                    n.print(prefix + childPrefix, isLast && node.vargs == null)
                }
                node.vargs?.print(prefix + childPrefix, true)
            }

            is FieldVarExpression -> {
                println("$prefix$connector IndexVarExpression")
                node.prefixexp.print(prefix + childPrefix, false)
                node.name.print(prefix + childPrefix, true)
            }

            is IndexVarExpression -> {
                println("$prefix$connector IndexVarExpression")
                node.prefixexp.print(prefix + childPrefix, false)
                node.exp.print(prefix + childPrefix, true)
            }

            is NameVarExpression -> {
                println("$prefix$connector NameVarExpression(${node.name})")
            }

            is VarList -> {
                node.varList.forEachAndLast { n, isLast ->
                    n.print(prefix + childPrefix, isLast)
                }
            }

            is FunctionDef -> TODO()
            is UnopExp -> TODO()
            is Unop -> TODO()
            is PrefixExp -> TODO()
            is FieldList -> TODO()
            is FieldByExp -> TODO()
            is FieldByIndex -> {
                println("$prefix$connector FieldByIndex")
                node.index.print(prefix + childPrefix, false, "index")
                node.value.print(prefix + childPrefix, true, "value")
            }
            is FieldByName -> {
                println("$prefix$connector FieldByName")
               node.name.print(prefix + childPrefix, false, "name")
               node.value.print(prefix + childPrefix, true, "value")
            }
            is TableConstructor -> {
                node.fieldList?.field?.forEachAndLast { n, isLast ->
                    n.print(prefix + childPrefix, isLast)
                }
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

class SyntaxException(message: String, val line: Int, val column: Int) : RuntimeException(message)
class Parser(private val tokens: List<Token>) {
    private var position = 0

    private fun currentToken(): Token = tokens[position]

    private fun nextToken(): Token = tokens[++position]

    private fun peekToken(): Token = tokens[position + 1]

    private fun expectToken(type: TokenType): Token {
        val token = currentToken()
        if (token.type != type) {
            throw SyntaxException(
                "Expected token $type but found ${token.type}. Line: ${token.line}, Column: ${token.column}",
                token.line,
                token.column
            )
        }
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
            TokenType.NIL -> {
                expectToken(TokenType.NIL)
                Nil
            }

            TokenType.FALSE -> {
                expectToken(TokenType.FALSE)
                False
            }

            TokenType.TRUE -> {

                expectToken(TokenType.TRUE)
                True
            }

            TokenType.NUMBER -> {
                expectToken(TokenType.NUMBER)
                Numeral(token.value)
            }

            TokenType.STRING -> {
                expectToken(TokenType.STRING)
                LiteralString(token.value)
            }

            TokenType.VARARGS -> {
                expectToken(TokenType.VARARGS)
                ParVarArgs
            }

            // ‘(’ exp ‘)’
            TokenType.OPEN_PARENTHESIS -> {
                expectToken(TokenType.OPEN_PARENTHESIS)
                val exp = expectExp()
                expectToken(TokenType.CLOSE_PARENTHESIS)
                exp
            }

            // functiondef ::= function funcbody
            TokenType.FUNCTION -> {
                expectToken(TokenType.FUNCTION)
                val body = expectFuncbody()
                FunctionDef(body)
            }

            // tableconstructor
            TokenType.OPEN_BRACE -> {
                expectToken(TokenType.OPEN_BRACE)
                val fieldList = expectFieldList()
                expectToken(TokenType.CLOSE_BRACE)
                TableConstructor(fieldList)
            }
            // unop exp
            TokenType.MINUS -> {
                expectToken(TokenType.MINUS)
                val exp = expectExp()
                UnopExp(Unop(TokenType.MINUS), exp)
            }
            // unop exp
            TokenType.NOT -> {
                expectToken(TokenType.NOT)
                val exp = expectExp()
                UnopExp(Unop(TokenType.NOT), exp)
            }
            // unop exp
            TokenType.LENGTH -> {
                expectToken(TokenType.LENGTH)
                val exp = expectExp()
                UnopExp(Unop(TokenType.LENGTH), exp)
            }
            // unop exp
            TokenType.BITWISE_NOT -> {
                expectToken(TokenType.BITWISE_NOT)
                val exp = expectExp()
                UnopExp(Unop(TokenType.BITWISE_NOT), exp)
            }

            // prefixexp
            TokenType.IDENTIFIER -> {
                PrefixExp(expectPrefixexp())
            }
            // prefixexp | tableconstructor | exp binop exp | unop exp
            else -> {
                TODO("${token.type} with ${token.value} is not supported yet")
            }
        }
    }

    private fun expectField(): Field {
        return when (currentToken().type) {
            TokenType.OPEN_BRACKET -> {
                expectToken(TokenType.OPEN_BRACKET)
                val exp = expectExp()
                expectToken(TokenType.CLOSE_BRACKET)
                expectToken(TokenType.ASSIGN)
                val exp2 = expectExp()
                FieldByIndex(exp, exp2)
            }

            TokenType.IDENTIFIER -> {
                val name = expectToken(TokenType.IDENTIFIER)
                expectToken(TokenType.ASSIGN)
                val exp = expectExp()
                FieldByName(StrName(name.value), exp)
            }

            else -> FieldByExp(expectExp())
        }
    }

    private fun expectFieldList(): FieldList {
        val result = mutableListOf<Field>()
        result.add(expectField())

        var hasNext: Boolean
        do {
            hasNext = when (currentToken().type) {
                TokenType.COMMA, TokenType.SEMICOLON -> {
                    expectToken(currentToken().type)
                    // si }, ne pas faire expectField, hasNext -> false
                    if(currentToken().type == TokenType.CLOSE_BRACE) {
                        false
                    } else {
                        result.add(expectField())
                    }
                }

                else -> false
            }
        } while (hasNext)

        return FieldList(result)
    }

    private fun expectPrefixexp(previous: MutableList<ASTNode> = mutableListOf()): PrefixExpression {
        return when (val token = currentToken().type) {
            TokenType.IDENTIFIER -> {
                val name = expectToken(TokenType.IDENTIFIER)
                previous.add(NameVarExpression(name.value))
                expectPrefixexp(previous)
            }

            TokenType.OPEN_BRACKET -> {
                expectToken(TokenType.OPEN_BRACKET)
                val exp = expectExp()
                expectToken(TokenType.CLOSE_BRACKET)
                val prev = previous.last() as PrefixExpression
                IndexVarExpression(prev, exp).also {
                    previous.add(it)
                }
                expectPrefixexp(previous)
            }

            TokenType.DOT -> {
                expectToken(TokenType.DOT)
                val name = expectToken(TokenType.IDENTIFIER)
                val prev = previous.last() as PrefixExpression
                FieldVarExpression(prev, StrName(name.value)).also {
                    previous.add(it)
                }
                expectPrefixexp(previous)
            }

            else -> previous.last() as? PrefixExpression ?: TODO("$token not expected")
        }
    }

    /**
     *
     * block ::= {stat} [retstat]
     *
     * stat ::=  ‘;’ |
     * 		 varlist ‘=’ explist |
     * 		 functioncall |
     * 		 label |
     * 		 break |
     * 		 goto Name |
     * 		 do block end |
     * 		 while exp do block end |
     * 		 repeat block until exp |
     * 		 if exp then block {elseif exp then block} [else block] end |
     * 		 for Name ‘=’ exp ‘,’ exp [‘,’ exp] do block end |
     * 		 for namelist in explist do block end |
     * 		 function funcname funcbody |
     * 		 local function Name funcbody |
     * 		 local attnamelist [‘=’ explist]
     */
    fun expectBlock(): Block {
        val statements = mutableListOf<Statement>()
        while (true) {
            val token = currentToken()
            val statement = when (token.type) {

                TokenType.BREAK -> {
                    // break |
                    expectToken(TokenType.BREAK)
                    Break
                }

                TokenType.DO -> {
                    // do block end |
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
                    FunctionDefinition(funcname, funcbody)
                }

                TokenType.GOTO -> {
                    // goto Name |
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
                    expectToken(TokenType.LOCAL)
                    if (currentToken().type == TokenType.FUNCTION) {
                        // local function Name funcbody |
                        expectToken(TokenType.FUNCTION)
                        val name = expectToken(TokenType.IDENTIFIER)
                        val funcbody = expectFuncbody()
                        TODO()
                    } else {
                        //	local attnamelist [‘=’ explist]
                        TODO()
                    }
                }

                TokenType.REPEAT -> {
                    // repeat block until exp |
                    expectToken(TokenType.REPEAT)
                    val block = expectBlock()
                    expectToken(TokenType.UNTIL)
                    val exp = expectExp()
                    Repeat(block, exp)
                }

                TokenType.RETURN -> {
                    // retstat ::= return [explist] [‘;’]
                    val expList = expectExplist()
                    Retstat(expList)
                }

                TokenType.WHILE -> {
                    // while exp do block end
                    expectToken(TokenType.WHILE)
                    val exp = expectExp()
                    expectToken(TokenType.DO)
                    val block = expectBlock()
                    expectToken(TokenType.END)
                    While(exp, block)
                }

                TokenType.DOUBLE_COLON -> {
                    // label
                    expectToken(TokenType.DOUBLE_COLON)
                    val id = expectToken(TokenType.IDENTIFIER)
                    expectToken(TokenType.DOUBLE_COLON)
                    Label(StrName(id.value))
                }

                TokenType.SEMICOLON -> {
                    // ‘;’
                    expectToken(TokenType.SEMICOLON)
                    Nop
                }

                /**
                 * varlist or functioncall
                 */
                TokenType.OPEN_PARENTHESIS, TokenType.IDENTIFIER -> constructStatement()

                else -> null
            }

            when (statement) {
                is Statement -> statements.add(statement)
                is Retstat -> return Block(statements, statement)
                else -> return Block(statements)
            }
        }
    }

    private val nameLimiter = setOf(
        TokenType.OPEN_PARENTHESIS,
        TokenType.OPEN_BRACKET,
        TokenType.OPEN_BRACE,
        TokenType.STRING,
        TokenType.DOUBLE_COLON,
        TokenType.DOT
    )

    private fun tryNameVar(): NameVarExpression? {
        return if (currentToken().type == TokenType.IDENTIFIER && !nameLimiter.contains(peekToken().type)) {
            val name = expectToken(TokenType.IDENTIFIER)
            NameVarExpression(name.value)
        } else {
            null
        }
    }

    // ‘(’ exp ‘)’
    private fun tryExpInParenthesis(): Exp? {
        if (currentToken().type != TokenType.OPEN_PARENTHESIS) {
            return null
        }
        expectToken(TokenType.OPEN_PARENTHESIS)
        val exp = expectExp()
        expectToken(TokenType.CLOSE_PARENTHESIS)
        return exp
    }

    // prefixexp ::= var | functioncall | ‘(’ exp ‘)’
    private fun tryPrefixexpWithoutVar(): PrefixExpression? {
        return tryFunctionCall() ?: tryExpInParenthesis()
    }

    // prefixexp ‘[’ exp ‘]’
    // FIXME: ça va pas marcher. je devrais tryPrefix et selon [ ou (, faire un autre call
    private fun tryIndexVar(): IndexVarExpression? {
        val prefixexp = tryPrefixexpWithoutVar() ?: return null
        expectToken(TokenType.OPEN_BRACKET)
        val exp = expectExp()
        expectToken(TokenType.CLOSE_BRACKET)
        return IndexVarExpression(prefixexp, exp)
    }

    //  prefixexp ‘.’ Name
    private fun tryFieldVar(): FieldVarExpression? {
        val prefixexp = tryPrefixexpWithoutVar() ?: return null
        expectToken(TokenType.DOT)
        val name = expectToken(TokenType.IDENTIFIER)
        return FieldVarExpression(prefixexp, StrName(name.value))
    }

    // var ::=  Name | prefixexp ‘[’ exp ‘]’ | prefixexp ‘.’ Name
    private fun tryVar(): Var? {
        return tryNameVar() ?: tryIndexVar() ?: tryFieldVar()
    }

    private fun expectVar(): Var {
        return tryVar()!!
    }

    // varlist ::= var {‘,’ var}
    private fun tryVarList(): VarList? {
        val v = tryVar() ?: return null

        val result = mutableListOf(v)
        while (currentToken().type == TokenType.COMMA) {
            expectToken(TokenType.COMMA)
            result.add(expectVar())
        }
        return VarList(result)
    }

    // functioncall ::=  prefixexp args | prefixexp ‘:’ Name args
    // --> prefixexp ::= Name | prefixexp ‘[’ exp ‘]’ | prefixexp ‘.’ Name | prefixexp args | prefixexp ‘:’ Name args | ‘(’ exp ‘)’
    private fun tryFunctionCall(): Function? {
        TODO()
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
        val parList = if (currentToken().type != TokenType.CLOSE_PARENTHESIS) {
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

    // explist ::= exp {‘,’ exp}
    private fun expectExplist(): ExpList {
        val head = expectExp()
        val result = mutableListOf(head)
        while (currentToken().type == TokenType.COMMA) {
            expectToken(TokenType.COMMA)
            result.add(expectExp())
        }
        return ExpList(result)
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


    private fun constructStatement(
        current: MutableList<ASTNode> = mutableListOf(), previous: MutableList<ASTNode> = mutableListOf()
    ): Statement {
        fun createFunctionStatement(
            stack: MutableList<ASTNode>, previous: MutableList<ASTNode>, args: Args
        ): Statement {
            return when (val prev = stack.last()) {
                is PrefixExpression -> {
                    stack.removeLast()
                    FunctionCall(prev, args)
                }

                is Name -> {
                    val prefix = stack[stack.size - 1] as PrefixExpression
                    stack.removeAt(stack.size - 2)
                    stack.removeAt(stack.size - 1)
                    MethodCall(prefix, prev, args)
                }

                else -> TODO() // ne correspond pas au pattern
            }
        }

        when (currentToken().type) {
            // args ::=  ‘(’ [explist] ‘)’
            TokenType.OPEN_PARENTHESIS -> {
                expectToken(TokenType.OPEN_PARENTHESIS)
                val expList = expectExplist()
                expectToken(TokenType.CLOSE_PARENTHESIS)

                return createFunctionStatement(previous, previous, expList)
            }
            // args ::=  tableconstructor
            TokenType.OPEN_BRACE -> {
                expectToken(TokenType.OPEN_BRACE)
                TODO()
                // expectToken(TokenType.CLOSE_BRACE)
                // createFunctionStatement(stack, statements, expList)
                // return tryStuff(emptyList(), statements)
            }
            // args ::=  LiteralString
            TokenType.STRING -> {
                val str = expectToken(TokenType.STRING)
                previous.add(createFunctionStatement(current, previous, LiteralString(str.value)))
                return constructStatement(current, previous)
            }
            // varlist ::= var {‘,’ var}
            TokenType.COMMA -> {
                expectToken(TokenType.COMMA)
                return constructStatement(current, previous)
            }
            // var ::=  prefixexp ‘[’ exp ‘]’
            TokenType.OPEN_BRACKET -> {
                expectToken(TokenType.OPEN_BRACKET)
                val exp = expectExp()
                expectToken(TokenType.CLOSE_BRACKET)
                val prefixexp = previous.last() as PrefixExpression
                previous.removeLast()

                previous.add(IndexVarExpression(prefixexp, exp))
                return constructStatement(current, previous)
            }
            // var ::= prefixexp ‘.’ Name
            TokenType.DOT -> {
                expectToken(TokenType.DOT)
                val name = expectToken(TokenType.IDENTIFIER)
                val prefixexp = previous.last() as PrefixExpression
                previous.removeLast()
                previous.add(FieldVarExpression(prefixexp, StrName(name.value)))
                return constructStatement(current, previous)
            }
            // var ::=  Name
            TokenType.IDENTIFIER -> {
                val name = expectToken(TokenType.IDENTIFIER)
                val nameVar = NameVarExpression(name.value)
                when (val last = previous.lastOrNull()) {
                    is Var -> {
                        // replace var with varlist
                        previous.removeLast()
                        previous.add(VarList(listOf(last, nameVar)))
                    }

                    is VarList -> {
                        // append to var list
                        previous.removeLast()
                        previous.add(VarList(last.varList + nameVar))
                    }

                    else -> {
                        // create var
                        previous.add(nameVar)
                    }
                }

                return constructStatement(current = current, previous = previous)
            }
            // varlist ‘=’ explist |
            TokenType.ASSIGN -> {
                expectToken(TokenType.ASSIGN)
                when (val prev = previous.last()) {
                    is Var -> {
                        val expList = expectExplist()
                        return Assignment(VarList(listOf(prev)), expList)
                    }

                    is VarList -> {
                        val expList = expectExplist()
                        return Assignment(prev, expList)
                    }

                    else -> TODO()
                }
            }

            else -> {
                TODO("${currentToken().type} is not expected.}")
            }
        }
    }
}

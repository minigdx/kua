package com.github.minigdx.lua

import com.github.minigdx.lua.parser.SyntaxException
import com.github.minigdx.lua.parser.Token
import com.github.minigdx.lua.parser.TokenType

class Parser(private val tokens: List<Token>) {
    private var position = 0

    private fun currentToken(): Token = tokens[position]

    private fun nextToken(): Token = tokens[++position]

    private fun peekToken(): Token = tokens[position + 1]

    private fun expectToken(type: TokenType): Token {
        val token = currentToken()
        if (token.type != type) {
            throw SyntaxException(token, type)
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
            else -> throw SyntaxException(
                "${token.type} with ${token.value} is not supported yet",
                token.line, token.column
            )
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
                FieldByName(Name(name.value), exp)
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
                    // if the table is closed, there will not be a new field.
                    if (currentToken().type == TokenType.CLOSE_BRACE) {
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
        val currentToken = currentToken()
        return when (currentToken.type) {
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
                FieldVarExpression(prev, Name(name.value)).also {
                    previous.add(it)
                }
                expectPrefixexp(previous)
            }

            else -> previous.last() as? PrefixExpression ?: throw SyntaxException(
                "${currentToken.type} with ${currentToken.value} is not supported yet",
                currentToken.line, currentToken.column
            )
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
                        For(Name(name.value), init, until, step, block)
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
                        ForName(NameList(names.map { Name(it.value) }), expList, block)
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
                    Goto(Name(id.value))
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
                        LocalFunc(Name(name.value), funcbody)
                    } else {
                        //	local attnamelist [‘=’ explist]
                        val attnamelist = expectAttNameList()
                        val expList = if(currentToken().type == TokenType.ASSIGN) {
                            expectToken(TokenType.ASSIGN)
                            expectExplist()
                        } else {
                            null
                        }
                        LocalAssigment(attnamelist, expList)
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
                    Label(Name(id.value))
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

    // funcname ::= Name {‘.’ Name} [‘:’ Name]
    private fun expectFuncname(): Funcname {
        val id = Name(expectToken(TokenType.IDENTIFIER).value)

        val children = mutableListOf<Name>()
        var nextToken = currentToken()
        while (nextToken.type == TokenType.DOT) {
            expectToken(TokenType.DOT)
            children.add(Name(expectToken(TokenType.IDENTIFIER).value))
            nextToken = currentToken()
        }
        val method = if (currentToken().type == TokenType.DOUBLE_COLON) {
            expectToken(TokenType.DOUBLE_COLON)
            Name(expectToken(TokenType.IDENTIFIER).value)
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
        val first = Name(expectToken(TokenType.IDENTIFIER).value)
        val tail = mutableListOf<Name>()
        while (currentToken().type == TokenType.COMMA && peekToken().type == TokenType.IDENTIFIER) {
            expectToken(TokenType.COMMA)
            tail.add(Name(expectToken(TokenType.IDENTIFIER).value))
        }
        return NameList(listOf(first) + tail)
    }

    // attnamelist ::=  Name attrib {‘,’ Name attrib}
    private fun expectAttNameList(): AttNameList {
        val result = mutableListOf<AttName>()
        do {
            val name = expectToken(TokenType.IDENTIFIER)
            // attrib ::= [‘<’ Name ‘>’]
            val attrib = if (currentToken().type == TokenType.LESS_THAN) {
                expectToken(TokenType.LESS_THAN)
                val att = Name(expectToken(TokenType.IDENTIFIER).value).also {
                    expectToken(TokenType.GREATER_THAN)
                }
                Attrib(att)
            } else {
                null
            }
            result.add(AttName(Name(name.value), attrib))
        } while (currentToken().type == TokenType.COMMA)

        return AttNameList(result)
    }


    private fun constructStatement(
        current: MutableList<ASTNode> = mutableListOf(), previous: MutableList<ASTNode> = mutableListOf()
    ): Statement {
        fun createFunctionStatement(
            stack: MutableList<ASTNode>, args: Args
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

                else -> {
                    val token = currentToken()
                    throw SyntaxException(
                        "${token.type} with ${token.value} is not supported yet",
                        token.line, token.line
                    )
                }
            }
        }

        when (currentToken().type) {
            // args ::=  ‘(’ [explist] ‘)’
            TokenType.OPEN_PARENTHESIS -> {
                expectToken(TokenType.OPEN_PARENTHESIS)
                val expList = expectExplist()
                expectToken(TokenType.CLOSE_PARENTHESIS)

                return createFunctionStatement(previous, expList)
            }
            // args ::=  tableconstructor
            TokenType.OPEN_BRACE -> {
                expectToken(TokenType.OPEN_BRACE)
                TODO()
                // expectToken(com.github.minigdx.lua.parser.TokenType.CLOSE_BRACE)
                // createFunctionStatement(stack, statements, expList)
                // return tryStuff(emptyList(), statements)
            }
            // args ::=  LiteralString
            TokenType.STRING -> {
                val str = expectToken(TokenType.STRING)
                previous.add(createFunctionStatement(current, LiteralString(str.value)))
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
                previous.add(FieldVarExpression(prefixexp, Name(name.value)))
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
                val token = currentToken()
                throw SyntaxException(
                    "${token.type} with ${token.value} is not supported yet",
                    token.line, token.line
                )
            }
        }
    }
}

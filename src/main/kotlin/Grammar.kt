import com.github.minigdx.lua.parser.TokenType

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

            is AttNameList -> {
                node.attribs.forEachAndLast { attName, isLast ->
                    attName.print(prefix + childPrefix, isLast)
                }
            }
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

            is LocalAssigment -> {
                println("$prefix$connector LocalAssigment")
                node.attnamelist.print(prefix + childPrefix, node.explist == null)
                node.explist?.print(prefix + childPrefix, true)
            }
            is LocalFunc -> {
                println("$prefix$connector LocalFunc")
                node.name.print(prefix + childPrefix, false)
                node.funcbody.print(prefix + childPrefix, true)
            }
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

            is Name -> println("$prefix$connector ${node.value}")
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

            is AttName -> {
                node.name.print(prefix + childPrefix, node.attrib == null)
                node.attrib?.print(prefix + childPrefix, true)
            }

            is Attrib -> {
                println("$prefix$connector Attrib")
                node.name.print(prefix + childPrefix, true)
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


class Name(val value: String) : ASTNode {
    override fun toString(): String = value
}

// prefixexp ::= var | functioncall | ‘(’ exp ‘)’
sealed interface PrefixExpression : ASTNode

// args ::=  ‘(’ [explist] ‘)’ | tableconstructor | LiteralString
sealed interface Args : ASTNode


// explist ::= exp {‘,’ exp}
class ExpList(val expList: List<Exp>) : Args

// varlist ::= var {‘,’ var}
class VarList(val varList: List<Var>) : ASTNode

// namelist ::= Name {‘,’ Name}
class NameList(val nameList: List<Name>) : ASTNode

// attrib ::= [‘<’ Name ‘>’]
class Attrib(val name: Name) : ASTNode

// functioncall ::=  prefixexp args | prefixexp ‘:’ Name args
sealed interface Function : PrefixExpression, Statement

// prefixexp args
class FunctionCall(val prefixexp: PrefixExpression, val args: Args) : Function

// prefixexp ‘:’ Name args
class MethodCall(val prefixexp: PrefixExpression, val name: Name, val args: Args) : Function

// var ::=  Name | prefixexp ‘[’ exp ‘]’ | prefixexp ‘.’ Name
sealed interface Var : PrefixExpression, Statement
class NameVarExpression(val name: String) : Var
class IndexVarExpression(val prefixexp: PrefixExpression, val exp: Exp) : Var
class FieldVarExpression(val prefixexp: PrefixExpression, val name: Name) : Var

// exp ::=  nil | false | true | Numeral | LiteralString | ‘...’ | functiondef |
//		 prefixexp | tableconstructor | exp binop exp | unop exp
sealed interface Exp : PrefixExpression, Statement

class PrefixExp(val prefixexp: PrefixExpression) : Exp

data object Nil : Exp
data object False : Exp
data object True : Exp
data class Numeral(val number: String) : Exp
data class LiteralString(val string: String) : Exp, Args
class UnopExp(val unop: Unop, val exp: Exp) : Exp

class TableConstructor(val fieldList: FieldList?) : Exp

sealed interface Field : Exp
class FieldByIndex(val index: Exp, val value: Exp) : Field
class FieldByName(val name: Name, val value: Exp) : Field
class FieldByExp(val exp: Exp) : Field

class FieldList(val field: List<Field>) : ASTNode

class Unop(val token: TokenType) : ASTNode

// chunk ::= block
data class Chunk(val block: Block) : ASTNode

// block ::= {stat} [retstat]
data class Block(val statements: List<Statement>, val retstat: Retstat? = null) : ASTNode

// label ::= ‘::’ Name ‘::’
class Label(val name: Name) : Statement

// retstat ::= return [explist] [‘;’]
class Retstat(val explist: ExpList) : ASTNode

// parlist ::= namelist [‘,’ ‘...’] | ‘...’
sealed interface Parlist : ASTNode

// namelist [‘,’ ‘...’]
class ParNamelist(val namelist: NameList, var vargs: ParVarArgs?) : Parlist

// ‘...’
data object ParVarArgs : Parlist, Exp

data class FunctionDefinition(val name: Funcname, val body: Funcbody) : Statement

class FunctionDef(val body: Funcbody) : Exp

// funcname ::= Name {‘.’ Name} [‘:’ Name]
class Funcname(val root: Name, val children: List<Name>, val method: Name?) : ASTNode

// funcbody ::= ‘(’ [parlist] ‘)’ block end
class Funcbody(val args: Parlist?, val block: Block) : ASTNode

/**
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
sealed interface Statement : ASTNode

// ‘;’
data object Nop : Statement

// break
data object Break : Statement

// goto Name
data class Goto(val name: Name) : Statement

// do block end |
data class Do(val block: Block) : Statement

// while exp do block end |
data class While(val exp: Exp, val block: Block) : Statement

// repeat block until exp |
data class Repeat(val block: Block, val exp: Exp) : Statement

// if exp then block {elseif exp then block} [else block] end
data class If(val exp: Exp, val block: Block, val elif: List<Pair<Exp, Block>>, val or: Block?) : Statement

// for Name ‘=’ exp ‘,’ exp [‘,’ exp] do block end |
data class For(val name: Name, val init: Exp, val until: Exp, val step: Exp?, val block: Block) : Statement

// for namelist in explist do block end |
data class ForName(val names: NameList, val explist: ExpList, val block: Block) : Statement

// function funcname funcbody |
data class Func(val funcname: Funcname, val funcbody: Funcbody) : Statement

// local function Name funcbody
data class LocalFunc(val name: Name, val funcbody: Funcbody) : Statement

// local attnamelist [‘=’ explist]
data class LocalAssigment(val attnamelist: AttNameList, val explist: ExpList?) : Statement

// varlist ‘=’ explist |
class Assignment(val varlist: VarList, val explist: ExpList) : Statement

class AttName(val name: Name, val attrib: Attrib?): ASTNode
// attnamelist ::=  Name attrib {‘,’ Name attrib}
class AttNameList(val attribs: List<AttName>) : ASTNode

data class IdentifierNode(val name: String) : ASTNode
data class BinaryOperationNode(val left: ASTNode, val operator: String, val right: ASTNode) : ASTNode
// Add more node types as needed

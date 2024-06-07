sealed class Name : ASTNode

data class StrName(val value: String) : Name() {
    override fun toString(): String = value
}

data object EmptyName : Name()


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
sealed class Attrib(val name: Name)
data object EmptyAttrib : Attrib(name = EmptyName)

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
class Retstat(explist: ExpList) : ASTNode

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
data class LocalAssigment(val attnamelist: AttNameList, val explist: ExpList) : Statement

// varlist ‘=’ explist |
class Assignment(val varlist: VarList, val explist: ExpList) : Statement

// attnamelist ::=  Name attrib {‘,’ Name attrib}
class AttNameList(val name: Name, val attrib: Attrib, val others: List<Pair<Name, Attrib>>) : ASTNode

data class IdentifierNode(val name: String) : ASTNode
data class BinaryOperationNode(val left: ASTNode, val operator: String, val right: ASTNode) : ASTNode
// Add more node types as needed

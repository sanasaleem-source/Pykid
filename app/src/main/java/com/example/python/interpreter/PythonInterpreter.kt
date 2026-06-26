package com.example.python.interpreter

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.random.Random

class PythonInterpreter(
    private val onPrint: (String) -> Unit,
    private val onInputRequired: (String, CompletableDeferred<String>) -> Unit,
    private val phoneModule: PhoneModule,
    private val turtleState: TurtleState,
    private val onFinished: () -> Unit,
    private val onError: (String, Int) -> Unit // message, line number (1-based)
) {
    private val variables = mutableMapOf<String, Any>()
    private var isRunning = false

    data class CustomFunction(
        val name: String,
        val args: List<String>,
        val bodyLines: List<CodeLine>
    )
    private val customFunctions = mutableMapOf<String, CustomFunction>()

    data class CodeLine(
        val originalLineNumber: Int, // 1-based
        val indentLevel: Int,
        val content: String
    )

    suspend fun execute(code: String) {
        isRunning = true
        variables.clear()
        customFunctions.clear()
        turtleState.reset()
        
        // Populate math and random module values
        variables["math.pi"] = Math.PI
        variables["True"] = true
        variables["False"] = false

        val lines = preprocess(code)
        if (lines.isEmpty()) {
            onFinished()
            return
        }

        try {
            executeBlock(lines, 0, lines.size)
        } catch (e: InterpreterException) {
            onError(e.message ?: "Oops! Something went wrong.", e.lineNumber)
        } catch (e: Exception) {
            onError("Execution Error: ${e.localizedMessage ?: "Something went wrong"}", 1)
        } finally {
            isRunning = false
            onFinished()
        }
    }

    private fun preprocess(code: String): List<CodeLine> {
        val rawLines = code.split("\n")
        val result = mutableListOf<CodeLine>()
        
        for (i in rawLines.indices) {
            val originalLine = rawLines[i]
            val trimmed = originalLine.trim()
            
            // Skip empty lines and comment-only lines
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue
            }
            
            // Calculate indentation level (count leading spaces)
            var spaces = 0
            for (char in originalLine) {
                if (char == ' ') spaces++
                else if (char == '\t') spaces += 4
                else break
            }
            
            // Remove inline comments unless they are inside quotes
            val contentWithoutComment = stripComment(trimmed)
            if (contentWithoutComment.isEmpty()) continue

            result.add(CodeLine(i + 1, spaces, contentWithoutComment))
        }
        return result
    }

    private fun stripComment(line: String): String {
        var inSingleQuote = false
        var inDoubleQuote = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\'' && !inDoubleQuote) inSingleQuote = !inSingleQuote
            else if (c == '"' && !inSingleQuote) inDoubleQuote = !inDoubleQuote
            else if (c == '#' && !inSingleQuote && !inDoubleQuote) {
                return line.substring(0, i).trim()
            }
            i++
        }
        return line.trim()
    }

    private suspend fun executeBlock(lines: List<CodeLine>, startIdx: Int, endIdx: Int) {
        var pc = startIdx
        while (pc < endIdx && isRunning) {
            val line = lines[pc]
            val content = line.content

            if (content.startsWith("def ") || content.startsWith("def(")) {
                pc = handleDefBlock(lines, pc, endIdx)
            } else if (content.startsWith("if ") || content.startsWith("if(")) {
                pc = handleIfBlock(lines, pc, endIdx)
            } else if (content.startsWith("for ") || content.startsWith("for(")) {
                pc = handleForBlock(lines, pc, endIdx)
            } else if (content.startsWith("while ") || content.startsWith("while(")) {
                pc = handleWhileBlock(lines, pc, endIdx)
            } else {
                executeStatement(line)
                pc++
            }
            // Add a micro delay to keep UI responsive and allow cancellation
            delay(1)
        }
    }

    private fun handleDefBlock(lines: List<CodeLine>, pc: Int, endIdx: Int): Int {
        val currentLine = lines[pc]
        val header = currentLine.content

        if (!header.endsWith(":")) {
            throw InterpreterException("Missing a colon ':' at the end of 'def' statement!", currentLine.originalLineNumber)
        }

        // Format: def name(args):
        val withoutDefAndColon = header.substring(3, header.length - 1).trim()
        val parenIdx = withoutDefAndColon.indexOf("(")
        if (parenIdx == -1 || !withoutDefAndColon.endsWith(")")) {
            throw InterpreterException("Function definition must have parentheses! Example: 'def my_function(x):'", currentLine.originalLineNumber)
        }

        val funcName = withoutDefAndColon.substring(0, parenIdx).trim()
        val argsStr = withoutDefAndColon.substring(parenIdx + 1, withoutDefAndColon.length - 1).trim()
        val argsList = if (argsStr.isEmpty()) emptyList() else argsStr.split(",").map { it.trim() }

        val bodyRange = findIndentedBlock(lines, pc + 1, endIdx, currentLine.indentLevel)
        val bodyLines = lines.subList(bodyRange.first, bodyRange.second).map { it.copy() }

        customFunctions[funcName] = CustomFunction(funcName, argsList, bodyLines)
        onPrint("[Simulator] ✨ Defined custom magic command: $funcName()")
        return bodyRange.second
    }

    private suspend fun handleIfBlock(lines: List<CodeLine>, pc: Int, endIdx: Int): Int {
        val currentLine = lines[pc]
        val header = currentLine.content
        
        // Extract condition: "if condition:" or "if (condition):"
        if (!header.endsWith(":")) {
            throw InterpreterException("Missing a colon ':' at the end of 'if' statement!", currentLine.originalLineNumber)
        }
        val condStr = header.substring(2, header.length - 1).trim().removeSurrounding("(", ")")
        
        // Find the block of indented lines
        val bodyRange = findIndentedBlock(lines, pc + 1, endIdx, currentLine.indentLevel)
        val conditionValue = evaluateBoolean(condStr, currentLine.originalLineNumber)

        var blockExecuted = false
        if (conditionValue) {
            executeBlock(lines, bodyRange.first, bodyRange.second)
            blockExecuted = true
        }

        // Search for elif or else statements following this block
        var nextPc = bodyRange.second
        while (nextPc < endIdx) {
            val nextLine = lines[nextPc]
            if (nextLine.indentLevel == currentLine.indentLevel) {
                val nextContent = nextLine.content
                if (nextContent.startsWith("elif ") || nextContent.startsWith("elif(")) {
                    if (!nextContent.endsWith(":")) {
                        throw InterpreterException("Missing a colon ':' at the end of 'elif' statement!", nextLine.originalLineNumber)
                    }
                    if (!blockExecuted) {
                        val elifCond = nextContent.substring(4, nextContent.length - 1).trim().removeSurrounding("(", ")")
                        val elifRange = findIndentedBlock(lines, nextPc + 1, endIdx, nextLine.indentLevel)
                        val elifValue = evaluateBoolean(elifCond, nextLine.originalLineNumber)
                        if (elifValue) {
                            executeBlock(lines, elifRange.first, elifRange.second)
                            blockExecuted = true
                        }
                        nextPc = elifRange.second
                    } else {
                        // Already executed a branch, skip this block
                        val elifRange = findIndentedBlock(lines, nextPc + 1, endIdx, nextLine.indentLevel)
                        nextPc = elifRange.second
                    }
                } else if (nextContent == "else:") {
                    val elseRange = findIndentedBlock(lines, nextPc + 1, endIdx, nextLine.indentLevel)
                    if (!blockExecuted) {
                        executeBlock(lines, elseRange.first, elseRange.second)
                    }
                    nextPc = elseRange.second
                    break // 'else' terminates the chain
                } else {
                    break // Different statement, exit if-chain
                }
            } else {
                break
            }
        }
        return nextPc
    }

    private suspend fun handleForBlock(lines: List<CodeLine>, pc: Int, endIdx: Int): Int {
        val currentLine = lines[pc]
        val header = currentLine.content

        if (!header.endsWith(":")) {
            throw InterpreterException("Missing a colon ':' at the end of 'for' statement!", currentLine.originalLineNumber)
        }

        // Format: for var in range(...):
        val withoutForAndColon = header.substring(3, header.length - 1).trim()
        val inIdx = withoutForAndColon.indexOf(" in ")
        if (inIdx == -1) {
            throw InterpreterException("For loop should look like: 'for i in range(5):'", currentLine.originalLineNumber)
        }

        val loopVar = withoutForAndColon.substring(0, inIdx).trim()
        val iterableExpr = withoutForAndColon.substring(inIdx + 4).trim()

        val rangeValues = parseRange(iterableExpr, currentLine.originalLineNumber)
        val bodyRange = findIndentedBlock(lines, pc + 1, endIdx, currentLine.indentLevel)

        if (bodyRange.first == bodyRange.second) {
            throw InterpreterException("Your 'for' loop is empty! Indent the lines inside it.", currentLine.originalLineNumber)
        }

        for (valItem in rangeValues) {
            if (!isRunning) break
            variables[loopVar] = valItem
            executeBlock(lines, bodyRange.first, bodyRange.second)
        }

        return bodyRange.second
    }

    private suspend fun handleWhileBlock(lines: List<CodeLine>, pc: Int, endIdx: Int): Int {
        val currentLine = lines[pc]
        val header = currentLine.content

        if (!header.endsWith(":")) {
            throw InterpreterException("Missing a colon ':' at the end of 'while' statement!", currentLine.originalLineNumber)
        }

        val condStr = header.substring(5, header.length - 1).trim().removeSurrounding("(", ")")
        val bodyRange = findIndentedBlock(lines, pc + 1, endIdx, currentLine.indentLevel)

        if (bodyRange.first == bodyRange.second) {
            throw InterpreterException("Your 'while' loop is empty! Indent the lines inside it.", currentLine.originalLineNumber)
        }

        var safetyCounter = 0
        while (evaluateBoolean(condStr, currentLine.originalLineNumber) && isRunning) {
            safetyCounter++
            if (safetyCounter > 1500) {
                throw InterpreterException("Infinite loop detected! 🚨 I stopped the program to save your phone's battery. Double check your loop condition!", currentLine.originalLineNumber)
            }
            executeBlock(lines, bodyRange.first, bodyRange.second)
            delay(1) // Keep UI alive
        }

        return bodyRange.second
    }

    private fun findIndentedBlock(lines: List<CodeLine>, startPc: Int, endIdx: Int, parentIndent: Int): Pair<Int, Int> {
        var end = startPc
        while (end < endIdx && lines[end].indentLevel > parentIndent) {
            end++
        }
        return Pair(startPc, end)
    }

    private suspend fun executeStatement(line: CodeLine) {
        val content = line.content
        if (content.startsWith("import ") || content.startsWith("from ")) {
            val moduleName = if (content.startsWith("import ")) {
                content.substring(7).trim()
            } else {
                content.substringAfter("import").trim()
            }
            onPrint("[Simulator] 📦 Loaded pre-installed '$moduleName' library!")
            return
        }
        val eqIdx = findAssignmentEquals(content)

        if (eqIdx != -1) {
            // Assignment: var = expression
            val varName = content.substring(0, eqIdx).trim()
            val expr = content.substring(eqIdx + 1).trim()
            
            if (varName.endsWith("]") && !varName.startsWith("[")) {
                val openIdx = findMatchingOpenBracket(varName, varName.length - 1)
                if (openIdx != -1) {
                    val listExpr = varName.substring(0, openIdx).trim()
                    val indexExpr = varName.substring(openIdx + 1, varName.length - 1).trim()
                    
                    val listObj = evaluateExpression(listExpr, line.originalLineNumber)
                    val indexVal = evaluateExpression(indexExpr, line.originalLineNumber)
                    val valueToAssign = evaluateExpression(expr, line.originalLineNumber)
                    
                    if (listObj is MutableList<*>) {
                        @Suppress("UNCHECKED_CAST")
                        val mList = listObj as MutableList<Any>
                        val idx = indexVal.toString().toDoubleOrNull()?.toInt()
                            ?: throw InterpreterException("List index must be a number, but you gave '$indexExpr'!", line.originalLineNumber)
                        if (idx in 0 until mList.size) {
                            mList[idx] = valueToAssign
                            return
                        } else if (idx < 0 && idx >= -mList.size) {
                            mList[mList.size + idx] = valueToAssign
                            return
                        } else {
                            throw InterpreterException("List index $idx is out of range! (list size is ${mList.size})", line.originalLineNumber)
                        }
                    } else {
                        throw InterpreterException("Can only assign to a list index, but '$listExpr' is a ${listObj.javaClass.simpleName}!", line.originalLineNumber)
                    }
                }
            }

            if (!isValidVariableName(varName)) {
                throw InterpreterException("'$varName' is not a valid box (variable) name. Box names should start with letters!", line.originalLineNumber)
            }
            
            val value = evaluateExpression(expr, line.originalLineNumber)
            variables[varName] = value
        } else {
            // Standalone expression / Function call
            evaluateExpression(content, line.originalLineNumber)
        }
    }

    private fun findAssignmentEquals(line: String): Int {
        var inSingleQuote = false
        var inDoubleQuote = false
        var parenLevel = 0
        var braceLevel = 0
        
        for (i in line.indices) {
            val c = line[i]
            if (c == '\'' && !inDoubleQuote) inSingleQuote = !inSingleQuote
            else if (c == '"' && !inSingleQuote) inDoubleQuote = !inDoubleQuote
            else if (inSingleQuote || inDoubleQuote) continue
            else if (c == '(') parenLevel++
            else if (c == ')') parenLevel--
            else if (c == '[') braceLevel++
            else if (c == ']') braceLevel--
            else if (c == '=' && parenLevel == 0 && braceLevel == 0) {
                // Check if it is ==, !=, <=, >=
                if (i > 0 && (line[i-1] == '=' || line[i-1] == '!' || line[i-1] == '<' || line[i-1] == '>')) {
                    continue
                }
                if (i < line.length - 1 && line[i+1] == '=') {
                    continue
                }
                return i
            }
        }
        return -1
    }

    private fun isValidVariableName(name: String): Boolean {
        if (name.isEmpty()) return false
        val first = name[0]
        if (!first.isLetter() && first != '_') return false
        for (i in 1 until name.length) {
            val c = name[i]
            if (!c.isLetterOrDigit() && c != '_' && c != '.') return false
        }
        return true
    }

    private suspend fun parseRange(expr: String, lineNum: Int): List<Int> {
        if (!expr.startsWith("range(") || !expr.endsWith(")")) {
            throw InterpreterException("For loops must iterate over a range, like 'range(5)'!", lineNum)
        }
        val inner = expr.substring(6, expr.length - 1).trim()
        val parts = splitArgs(inner)
        try {
            return when (parts.size) {
                1 -> {
                    val stop = evaluateExpression(parts[0], lineNum).toString().toDouble().toInt()
                    (0 until stop).toList()
                }
                2 -> {
                    val start = evaluateExpression(parts[0], lineNum).toString().toDouble().toInt()
                    val stop = evaluateExpression(parts[1], lineNum).toString().toDouble().toInt()
                    (start until stop).toList()
                }
                3 -> {
                    val start = evaluateExpression(parts[0], lineNum).toString().toDouble().toInt()
                    val stop = evaluateExpression(parts[1], lineNum).toString().toDouble().toInt()
                    val step = evaluateExpression(parts[2], lineNum).toString().toDouble().toInt()
                    if (step == 0) throw InterpreterException("range() step cannot be zero!", lineNum)
                    
                    val list = mutableListOf<Int>()
                    if (step > 0) {
                        var i = start
                        while (i < stop) {
                            list.add(i)
                            i += step
                        }
                    } else {
                        var i = start
                        while (i > stop) {
                            list.add(i)
                            i += step
                        }
                    }
                    list
                }
                else -> throw InterpreterException("range() expects 1 to 3 numbers inside!", lineNum)
            }
        } catch (e: NumberFormatException) {
            throw InterpreterException("range() values must be numbers!", lineNum)
        }
    }

    private suspend fun evaluateBoolean(expr: String, lineNum: Int): Boolean {
        val trimmed = expr.trim()
        if (trimmed.isEmpty()) return false

        // 1. Check for "or" outside parens/quotes (right to left)
        val orIdx = findLogicalOperatorOutsideParens(trimmed, "or")
        if (orIdx != -1) {
            val lhs = trimmed.substring(0, orIdx).trim()
            val rhs = trimmed.substring(orIdx + 2).trim()
            return evaluateBoolean(lhs, lineNum) || evaluateBoolean(rhs, lineNum)
        }

        // 2. Check for "and" outside parens/quotes (right to left)
        val andIdx = findLogicalOperatorOutsideParens(trimmed, "and")
        if (andIdx != -1) {
            val lhs = trimmed.substring(0, andIdx).trim()
            val rhs = trimmed.substring(andIdx + 3).trim()
            return evaluateBoolean(lhs, lineNum) && evaluateBoolean(rhs, lineNum)
        }

        // 3. Check for "not" prefix
        if (trimmed.startsWith("not ") || trimmed.startsWith("not(")) {
            val inner = if (trimmed.startsWith("not ")) {
                trimmed.substring(4).trim()
            } else {
                trimmed.substring(3).trim().removeSurrounding("(", ")")
            }
            return !evaluateBoolean(inner, lineNum)
        }

        // 4. Check if surrounded by outer parentheses
        if (trimmed.startsWith("(") && trimmed.endsWith(")")) {
            val closeIdx = trimmed.length - 1
            var matchFound = true
            var level = 0
            for (i in 0 until trimmed.length) {
                if (trimmed[i] == '(') level++
                else if (trimmed[i] == ')') level--
                if (level == 0 && i < closeIdx) {
                    matchFound = false
                    break
                }
            }
            if (matchFound) {
                return evaluateBoolean(trimmed.substring(1, trimmed.length - 1).trim(), lineNum)
            }
        }

        // 5. Look for comparison operators
        val ops = listOf("==", "!=", "<=", ">=", "<", ">")
        for (op in ops) {
            val idx = findOperatorIndex(trimmed, op)
            if (idx != -1) {
                val lhs = trimmed.substring(0, idx).trim()
                val rhs = trimmed.substring(idx + op.length).trim()
                
                val lVal = evaluateExpression(lhs, lineNum)
                val rVal = evaluateExpression(rhs, lineNum)

                return compareValues(lVal, rVal, op, lineNum)
            }
        }

        // 6. Check if single word boolean or expression
        val evaluated = evaluateExpression(trimmed, lineNum)
        if (evaluated is Boolean) return evaluated
        if (evaluated is Number) return evaluated.toDouble() != 0.0
        if (evaluated is String) return evaluated.isNotEmpty()
        if (evaluated is List<*>) return evaluated.isNotEmpty()
        return false
    }

    private fun compareValues(l: Any, r: Any, op: String, lineNum: Int): Boolean {
        if (l is Number && r is Number) {
            val ld = l.toDouble()
            val rd = r.toDouble()
            return when (op) {
                "==" -> ld == rd
                "!=" -> ld != rd
                "<" -> ld < rd
                ">" -> ld > rd
                "<=" -> ld <= rd
                ">=" -> ld >= rd
                else -> false
            }
        }
        val ls = l.toString()
        val rs = r.toString()
        return when (op) {
            "==" -> ls == rs
            "!=" -> ls != rs
            "<" -> ls < rs
            ">" -> ls > rs
            "<=" -> ls <= rs
            ">=" -> ls >= rs
            else -> false
        }
    }

    private fun findOperatorIndex(expr: String, op: String): Int {
        var inSingleQuote = false
        var inDoubleQuote = false
        var parenLevel = 0
        var i = 0
        while (i <= expr.length - op.length) {
            val c = expr[i]
            if (c == '\'' && !inDoubleQuote) inSingleQuote = !inSingleQuote
            else if (c == '"' && !inSingleQuote) inDoubleQuote = !inDoubleQuote
            else if (inSingleQuote || inDoubleQuote) {
                i++
                continue
            } else if (c == '(') {
                parenLevel++
            } else if (c == ')') {
                parenLevel--
            } else if (parenLevel == 0 && expr.substring(i, i + op.length) == op) {
                return i
            }
            i++
        }
        return -1
    }

    private suspend fun evaluateExpression(expr: String, lineNum: Int): Any {
        val trimmed = expr.trim()
        if (trimmed.isEmpty()) return ""

        // 1. Literal Strings
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            return trimmed.substring(1, trimmed.length - 1)
        }

        // 2. Lists
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            val inner = trimmed.substring(1, trimmed.length - 1).trim()
            if (inner.isEmpty()) return mutableListOf<Any>()
            val items = splitArgs(inner).map { evaluateExpression(it, lineNum) }
            return items.toMutableList()
        }

        // 2.5 List/String index access lookup (e.g. colors[0], colors[i], name[2])
        if (trimmed.endsWith("]") && !trimmed.startsWith("[")) {
            val openIdx = findMatchingOpenBracket(trimmed, trimmed.length - 1)
            if (openIdx != -1) {
                val listExpr = trimmed.substring(0, openIdx).trim()
                val indexExpr = trimmed.substring(openIdx + 1, trimmed.length - 1).trim()
                val listObj = evaluateExpression(listExpr, lineNum)
                val indexVal = evaluateExpression(indexExpr, lineNum)
                val idx = indexVal.toString().toDoubleOrNull()?.toInt()
                    ?: throw InterpreterException("List/String index must be a number, but you gave '$indexExpr'!", lineNum)
                
                if (listObj is List<*>) {
                    if (idx in 0 until listObj.size) {
                        return listObj[idx]!!
                    } else if (idx < 0 && idx >= -listObj.size) {
                        return listObj[listObj.size + idx]!!
                    } else {
                        throw InterpreterException("List index $idx is out of range! (size is ${listObj.size})", lineNum)
                    }
                } else if (listObj is String) {
                    if (idx in 0 until listObj.length) {
                        return listObj[idx].toString()
                    } else if (idx < 0 && idx >= -listObj.length) {
                        return listObj[listObj.length + idx].toString()
                    } else {
                        throw InterpreterException("String index $idx is out of range! (length is ${listObj.length})", lineNum)
                    }
                }
            }
        }

        // 3. Simple function calls: print(), input(), phone.vibrate(), math.sqrt(), etc.
        val parenIdx = findFunctionParen(trimmed)
        if (parenIdx != -1 && trimmed.endsWith(")")) {
            val funcName = trimmed.substring(0, parenIdx).trim()
            val argsStr = trimmed.substring(parenIdx + 1, trimmed.length - 1).trim()
            val args = splitArgs(argsStr)
            
            return executeFunctionCall(funcName, args, lineNum)
        }

        // 4. Variables Lookup (direct match)
        if (variables.containsKey(trimmed)) {
            return variables[trimmed]!!
        }

        // 5. Arithmetic / String Concatenation: Expression containing + - * / %
        val addSubIdx = findOperatorIndexOutsideParens(trimmed, listOf("+", "-"))
        if (addSubIdx != -1) {
            val op = trimmed[addSubIdx].toString()
            val lhs = trimmed.substring(0, addSubIdx).trim()
            val rhs = trimmed.substring(addSubIdx + 1).trim()
            val lVal = evaluateExpression(lhs, lineNum)
            val rVal = evaluateExpression(rhs, lineNum)

            return if (op == "+") {
                if (lVal is String || rVal is String) {
                    lVal.toString() + rVal.toString()
                } else if (lVal is List<*> && rVal is List<*>) {
                    (lVal + rVal).toMutableList()
                } else if (lVal is Number && rVal is Number) {
                    if (lVal is Double || rVal is Double) lVal.toDouble() + rVal.toDouble()
                    else lVal.toLong() + rVal.toLong()
                } else {
                    lVal.toString() + rVal.toString()
                }
            } else { // "-"
                if (lVal is Number && rVal is Number) {
                    if (lVal is Double || rVal is Double) lVal.toDouble() - rVal.toDouble()
                    else lVal.toLong() - rVal.toLong()
                } else {
                    throw InterpreterException("Oops! You can't subtract non-numbers!", lineNum)
                }
            }
        }

        val mulDivIdx = findOperatorIndexOutsideParens(trimmed, listOf("*", "/", "%"))
        if (mulDivIdx != -1) {
            val op = trimmed[mulDivIdx].toString()
            val lhs = trimmed.substring(0, mulDivIdx).trim()
            val rhs = trimmed.substring(mulDivIdx + 1).trim()
            val lVal = evaluateExpression(lhs, lineNum)
            val rVal = evaluateExpression(rhs, lineNum)

            if (lVal is Number && rVal is Number) {
                val ld = lVal.toDouble()
                val rd = rVal.toDouble()
                return when (op) {
                    "*" -> {
                        if (lVal is Double || rVal is Double) ld * rd
                        else lVal.toLong() * rVal.toLong()
                    }
                    "/" -> {
                        if (rd == 0.0) throw InterpreterException("Division by zero! 🚨", lineNum)
                        ld / rd
                    }
                    "%" -> {
                        if (rd == 0.0) throw InterpreterException("Modulo by zero! 🚨", lineNum)
                        if (lVal is Double || rVal is Double) ld % rd
                        else lVal.toLong() % rVal.toLong()
                    }
                    else -> 0
                }
            } else if (op == "*" && (lVal is String && rVal is Number)) {
                return lVal.repeat(rVal.toDouble().toInt())
            } else if (op == "*" && (lVal is Number && rVal is String)) {
                return rVal.repeat(lVal.toDouble().toInt())
            } else {
                throw InterpreterException("Can't multiply these together!", lineNum)
            }
        }

        // Try parsing number
        trimmed.toLongOrNull()?.let { return it }
        trimmed.toDoubleOrNull()?.let { return it }

        // If nothing matches, and it looks like a variable but isn't found
        if (isValidVariableName(trimmed)) {
            throw InterpreterException("Oops! I couldn't find a variable named '$trimmed' 🦊. Did you type it correctly or forget to give it a value?", lineNum)
        }

        throw InterpreterException("I don't understand how to read this: '$trimmed'", lineNum)
    }

    private suspend fun executeFunctionCall(name: String, args: List<String>, lineNum: Int): Any {
        val evaluatedArgs = args.map { evaluateExpression(it, lineNum) }
        val cleanName = name.trim()

        if (customFunctions.containsKey(cleanName)) {
            val customFunc = customFunctions[cleanName]!!
            if (evaluatedArgs.size < customFunc.args.size) {
                throw InterpreterException("Custom command '${customFunc.name}' expects ${customFunc.args.size} inputs, but you only gave ${evaluatedArgs.size}!", lineNum)
            }
            val backupVars = HashMap(variables)
            customFunc.args.forEachIndexed { index, argName ->
                variables[argName] = evaluatedArgs[index]
            }
            executeBlock(customFunc.bodyLines, 0, customFunc.bodyLines.size)
            customFunc.args.forEach { argName ->
                if (backupVars.containsKey(argName)) {
                    variables[argName] = backupVars[argName]!!
                } else {
                    variables.remove(argName)
                }
            }
            return ""
        }

        if (cleanName.startsWith("pygame.")) {
            val subMethod = cleanName.substringAfter("pygame.")
            when (subMethod) {
                "init" -> {
                    onPrint("[Simulator] 🎮 Pygame: Initializing system hardware & sound mixers.")
                    phoneModule.playSound("success")
                }
                "display.set_mode" -> {
                    onPrint("[Simulator] 🖥️ Pygame: Setup active game window of size ${evaluatedArgs.joinToString("x")}")
                    phoneModule.vibrate(100)
                }
                "display.set_caption" -> {
                    onPrint("[Simulator] 🏷️ Pygame: Set Window Caption to \"${evaluatedArgs.firstOrNull() ?: ""}\"")
                }
                "display.flip", "display.update" -> {
                    delay(30)
                }
                "quit" -> {
                    onPrint("[Simulator] 🔌 Pygame closed.")
                    phoneModule.playSound("chime")
                }
                else -> {
                    onPrint("[Simulator] 🎮 Pygame: Executed $subMethod(${evaluatedArgs.joinToString(", ")})")
                }
            }
            return ""
        }

        if (cleanName.startsWith("kivy.") || cleanName.startsWith("kivymd.")) {
            onPrint("[Simulator] 📱 Kivy Framework: Executed $cleanName(${evaluatedArgs.joinToString(", ")})")
            return ""
        }
        
        when (cleanName.lowercase(Locale.ROOT)) {
            "print" -> {
                val output = evaluatedArgs.joinToString(" ") { it.toString() }
                onPrint(output)
                return ""
            }
            "input" -> {
                val prompt = evaluatedArgs.firstOrNull()?.toString() ?: ""
                val deferred = CompletableDeferred<String>()
                onInputRequired(prompt, deferred)
                // Wait for the user to type in the console and hit Enter
                val result = deferred.await()
                return result
            }
            "len" -> {
                val arg = evaluatedArgs.firstOrNull() ?: return 0
                return when (arg) {
                    is String -> arg.length
                    is List<*> -> arg.size
                    else -> 1
                }
            }
            "str" -> return evaluatedArgs.firstOrNull()?.toString() ?: ""
            "int" -> {
                val arg = evaluatedArgs.firstOrNull() ?: return 0
                return when (arg) {
                    is Number -> arg.toInt()
                    is String -> arg.toDoubleOrNull()?.toInt() ?: throw InterpreterException("Cannot turn '$arg' into an integer!", lineNum)
                    else -> 0
                }
            }
            "float" -> {
                val arg = evaluatedArgs.firstOrNull() ?: return 0.0
                return when (arg) {
                    is Number -> arg.toDouble()
                    is String -> arg.toDoubleOrNull() ?: throw InterpreterException("Cannot turn '$arg' into a decimal!", lineNum)
                    else -> 0.0
                }
            }
            "type" -> {
                val arg = evaluatedArgs.firstOrNull() ?: return "None"
                return when (arg) {
                    is String -> "<class 'str'>"
                    is Integer, is Long -> "<class 'int'>"
                    is Double, is Float -> "<class 'float'>"
                    is List<*> -> "<class 'list'>"
                    is Boolean -> "<class 'bool'>"
                    else -> "<class '${arg.javaClass.simpleName}'>"
                }
            }
            
            // phone module
            "phone.vibrate" -> {
                val duration = evaluatedArgs.firstOrNull()?.toString()?.toDoubleOrNull()?.toLong() ?: 200L
                phoneModule.vibrate(duration)
                return ""
            }
            "phone.speak" -> {
                val text = evaluatedArgs.firstOrNull()?.toString() ?: ""
                phoneModule.speak(text)
                return ""
            }
            "phone.toast" -> {
                val text = evaluatedArgs.firstOrNull()?.toString() ?: ""
                phoneModule.toast(text)
                return ""
            }
            "phone.play_sound" -> {
                val snd = evaluatedArgs.firstOrNull()?.toString() ?: "beep"
                phoneModule.playSound(snd)
                return ""
            }
            "phone.get_battery" -> {
                return (40..100).random() // Mock offline safe battery
            }
            
            // random module
            "random.randint" -> {
                if (evaluatedArgs.size < 2) throw InterpreterException("random.randint(min, max) needs two numbers!", lineNum)
                val min = evaluatedArgs[0].toString().toDouble().toInt()
                val max = evaluatedArgs[1].toString().toDouble().toInt()
                if (min > max) return min
                return Random.nextInt(min, max + 1)
            }
            "random.choice" -> {
                val arg = evaluatedArgs.firstOrNull() ?: throw InterpreterException("random.choice() needs a list!", lineNum)
                if (arg is List<*>) {
                    if (arg.isEmpty()) throw InterpreterException("Can't choose from an empty list!", lineNum)
                    return arg.random()!!
                }
                throw InterpreterException("random.choice() only works with lists!", lineNum)
            }
            "random.random" -> return Random.nextDouble()

            // math module
            "math.sqrt" -> {
                val arg = evaluatedArgs.firstOrNull()?.toString()?.toDoubleOrNull() ?: 0.0
                return kotlin.math.sqrt(arg)
            }
            "math.sin" -> {
                val arg = evaluatedArgs.firstOrNull()?.toString()?.toDoubleOrNull() ?: 0.0
                return kotlin.math.sin(arg)
            }
            "math.cos" -> {
                val arg = evaluatedArgs.firstOrNull()?.toString()?.toDoubleOrNull() ?: 0.0
                return kotlin.math.cos(arg)
            }

            // turtle module
            "turtle.forward", "turtle.fd" -> {
                val dist = evaluatedArgs.firstOrNull()?.toString()?.toFloatOrNull() ?: 50f
                turtleState.forward(dist)
                delay(120) // Animation step delay!
                return ""
            }
            "turtle.backward", "turtle.bk" -> {
                val dist = evaluatedArgs.firstOrNull()?.toString()?.toFloatOrNull() ?: 50f
                turtleState.backward(dist)
                delay(120)
                return ""
            }
            "turtle.right", "turtle.rt" -> {
                val deg = evaluatedArgs.firstOrNull()?.toString()?.toFloatOrNull() ?: 90f
                turtleState.right(deg)
                delay(80)
                return ""
            }
            "turtle.left", "turtle.lt" -> {
                val deg = evaluatedArgs.firstOrNull()?.toString()?.toFloatOrNull() ?: 90f
                turtleState.left(deg)
                delay(80)
                return ""
            }
            "turtle.color" -> {
                val col = evaluatedArgs.firstOrNull()?.toString() ?: "cyan"
                turtleState.setColorByName(col)
                return ""
            }
            "turtle.width", "turtle.pensize" -> {
                val w = evaluatedArgs.firstOrNull()?.toString()?.toFloatOrNull() ?: 4f
                turtleState.penWidth = w
                return ""
            }
            "turtle.penup", "turtle.pu" -> {
                turtleState.setPenState(false)
                return ""
            }
            "turtle.pendown", "turtle.pd" -> {
                turtleState.setPenState(true)
                return ""
            }
            "turtle.circle" -> {
                val r = evaluatedArgs.firstOrNull()?.toString()?.toFloatOrNull() ?: 20f
                turtleState.circle(r)
                delay(150)
                return ""
            }
            "turtle.clear" -> {
                turtleState.lines.clear()
                turtleState.circles.clear()
                turtleState.onUpdate?.invoke()
                return ""
            }
            "turtle.reset" -> {
                turtleState.reset()
                return ""
            }
        }

        // Custom list functions like my_list.append(val)
        if (name.contains(".")) {
            val dotIdx = name.lastIndexOf(".")
            val listObjName = name.substring(0, dotIdx)
            val listMethod = name.substring(dotIdx + 1).lowercase().trim()

            if (variables.containsKey(listObjName)) {
                val listObj = variables[listObjName]
                if (listObj is MutableList<*>) {
                    @Suppress("UNCHECKED_CAST")
                    val mList = listObj as MutableList<Any>
                    when (listMethod) {
                        "append" -> {
                            val arg = evaluatedArgs.firstOrNull() ?: throw InterpreterException("append() expects a value to add!", lineNum)
                            mList.add(arg)
                            return ""
                        }
                        "remove" -> {
                            val arg = evaluatedArgs.firstOrNull() ?: throw InterpreterException("remove() expects a value to remove!", lineNum)
                            mList.remove(arg)
                            return ""
                        }
                        "pop" -> {
                            if (mList.isEmpty()) throw InterpreterException("Can't pop from an empty list!", lineNum)
                            return mList.removeAt(mList.size - 1)
                        }
                        "clear" -> {
                            mList.clear()
                            return ""
                        }
                    }
                }
            }
        }

        throw InterpreterException("Oops! I don't know any function named '$name' 🤖", lineNum)
    }

    private fun findFunctionParen(expr: String): Int {
        var inSingleQuote = false
        var inDoubleQuote = false
        for (i in expr.indices) {
            val c = expr[i]
            if (c == '\'' && !inDoubleQuote) inSingleQuote = !inSingleQuote
            else if (c == '"' && !inSingleQuote) inDoubleQuote = !inDoubleQuote
            else if (inSingleQuote || inDoubleQuote) continue
            else if (c == '(') {
                return i
            }
        }
        return -1
    }

    private fun splitArgs(argsStr: String): List<String> {
        if (argsStr.isEmpty()) return emptyList()
        val result = mutableListOf<String>()
        var inSingleQuote = false
        var inDoubleQuote = false
        var parenLevel = 0
        var braceLevel = 0
        var current = StringBuilder()
        
        for (i in argsStr.indices) {
            val c = argsStr[i]
            if (c == '\'' && !inDoubleQuote) inSingleQuote = !inSingleQuote
            else if (c == '"' && !inSingleQuote) inDoubleQuote = !inDoubleQuote
            else if (inSingleQuote || inDoubleQuote) {
                current.append(c)
                continue
            }
            else if (c == '(') parenLevel++
            else if (c == ')') parenLevel--
            else if (c == '[') braceLevel++
            else if (c == ']') braceLevel--
            
            if (c == ',' && parenLevel == 0 && braceLevel == 0) {
                result.add(current.toString().trim())
                current = StringBuilder()
            } else {
                current.append(c)
            }
        }
        if (current.isNotEmpty()) {
            result.add(current.toString().trim())
        }
        return result
    }

    private fun findOperatorIndexOutsideParens(expr: String, ops: List<String>): Int {
        var inSingleQuote = false
        var inDoubleQuote = false
        var parenLevel = 0
        var braceLevel = 0
        
        // Scan right to left for + and - to enforce left associativity
        for (i in expr.length - 1 downTo 0) {
            val c = expr[i]
            if (c == '\'' && !inDoubleQuote) inSingleQuote = !inSingleQuote
            else if (c == '"' && !inSingleQuote) inDoubleQuote = !inDoubleQuote
            else if (inSingleQuote || inDoubleQuote) continue
            else if (c == ')') parenLevel++
            else if (c == '(') parenLevel--
            else if (c == ']') braceLevel++
            else if (c == '[') braceLevel--
            else if (parenLevel == 0 && braceLevel == 0 && ops.contains(c.toString())) {
                // Confirm it is not a negative number indicator
                if (c == '-' && (i == 0 || expr[i-1] == '+' || expr[i-1] == '-' || expr[i-1] == '*' || expr[i-1] == '/' || expr[i-1] == '(')) {
                    continue
                }
                return i
            }
        }
        return -1
    }

    private fun findMatchingOpenBracket(str: String, closeIdx: Int): Int {
        var level = 0
        var inSingleQuote = false
        var inDoubleQuote = false
        for (i in closeIdx downTo 0) {
            val c = str[i]
            if (c == '\'' && !inDoubleQuote) inSingleQuote = !inSingleQuote
            else if (c == '"' && !inSingleQuote) inDoubleQuote = !inDoubleQuote
            else if (inSingleQuote || inDoubleQuote) continue
            else if (c == ']') level++
            else if (c == '[') {
                level--
                if (level == 0) return i
            }
        }
        return -1
    }

    private fun findLogicalOperatorOutsideParens(expr: String, op: String): Int {
        var inSingleQuote = false
        var inDoubleQuote = false
        var parenLevel = 0
        var braceLevel = 0
        val len = op.length
        
        for (i in expr.length - len - 1 downTo 0) {
            val c = expr[i]
            if (c == '\'' && !inDoubleQuote) inSingleQuote = !inSingleQuote
            else if (c == '"' && !inSingleQuote) inDoubleQuote = !inDoubleQuote
            else if (inSingleQuote || inDoubleQuote) continue
            else if (c == ')') parenLevel++
            else if (c == '(') parenLevel--
            else if (c == ']') braceLevel++
            else if (c == '[') braceLevel--
            else if (parenLevel == 0 && braceLevel == 0) {
                if (expr.substring(i, i + len) == op) {
                    val prevCharOk = i == 0 || !expr[i - 1].isLetterOrDigit()
                    val nextCharOk = i + len >= expr.length || !expr[i + len].isLetterOrDigit()
                    if (prevCharOk && nextCharOk) {
                        return i
                    }
                }
            }
        }
        return -1
    }
}

class InterpreterException(message: String, val lineNumber: Int) : Exception(message)

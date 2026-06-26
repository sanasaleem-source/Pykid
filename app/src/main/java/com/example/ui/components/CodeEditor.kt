package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CodeEditor(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    fileName: String = "code.py"
) {
    val scrollState = rememberScrollState()
    
    val speedKeys = listOf(
        "tab" to "    ",
        "print()" to "print(\"\")",
        "speak()" to "phone.speak(\"\")",
        "vibrate()" to "phone.vibrate(200)",
        "sound()" to "phone.play_sound(\"beep\")",
        "for" to "for i in range(5):\n    ",
        "if" to "if x > 5:\n    ",
        "else" to "else:\n    ",
        "turtle" to "turtle.forward(100)",
        "left()" to "turtle.left(90)",
        "right()" to "turtle.right(90)",
        "col()" to "turtle.color(\"cyan\")",
        ":" to ":",
        "==" to " == ",
        "=" to " = ",
        "\"" to "\"\""
    )

    // Maintain local TextFieldValue state to accurately track cursor selection
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = code, selection = TextRange(code.length)))
    }

    // Keep state in sync with updates from external triggers (like loading a lesson)
    LaunchedEffect(code) {
        if (code != textFieldValue.text) {
            textFieldValue = TextFieldValue(text = code, selection = TextRange(code.length))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F), shape = RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF49454F), shape = RoundedCornerShape(16.dp))
    ) {
        // macOS-style Window Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2B2930), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .border(
                    width = 1.dp, 
                    color = Color(0xFF49454F), 
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(10.dp).background(Color(0xFFFF5F56), CircleShape))
                Box(modifier = Modifier.size(10.dp).background(Color(0xFFFFBD2E), CircleShape))
                Box(modifier = Modifier.size(10.dp).background(Color(0xFF27C93F), CircleShape))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = fileName,
                color = Color(0xFF938F99),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Editor Area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Line number + Text Field Row
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val linesCount = textFieldValue.text.split("\n").size.coerceAtLeast(1)
                Row(modifier = Modifier.fillMaxSize()) {
                    // Line numbers gutter
                    Column(
                        modifier = Modifier
                            .width(36.dp)
                            .fillMaxHeight()
                            .padding(top = 8.dp, end = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        for (i in 1..linesCount) {
                            Text(
                                text = i.toString(),
                                color = Color(0xFF938F99),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                    
                    // Active Editor Field
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            textFieldValue = newValue
                            onCodeChange(newValue.text)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp, start = 8.dp)
                            .testTag("python_code_input"),
                        textStyle = TextStyle(
                            color = Color(0xFFE6E1E5),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(Color(0xFFD0BCFF)),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Default,
                            autoCorrectEnabled = false
                        ),
                        visualTransformation = PythonSyntaxHighlighter()
                    )
                }
            }

            // Horizontal Quick Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                speedKeys.forEach { (label, insertion) ->
                    Button(
                        onClick = {
                            val selection = textFieldValue.selection
                            val currentText = textFieldValue.text
                            val newText = currentText.substring(0, selection.start) + 
                                          insertion + 
                                          currentText.substring(selection.end)
                            
                            val nextCursor = selection.start + insertion.length
                            val updatedValue = TextFieldValue(
                                text = newText,
                                selection = TextRange(nextCursor)
                            )
                            textFieldValue = updatedValue
                            onCodeChange(newText)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2B2930),
                            contentColor = Color(0xFFD0BCFF)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("speed_key_$label")
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

class PythonSyntaxHighlighter : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            highlightPythonCode(text.text),
            OffsetMapping.Identity
        )
    }
}

fun highlightPythonCode(code: String): AnnotatedString {
    return buildAnnotatedString {
        append(code)
        
        // Define colors
        val keywordColor = Color(0xFFE57373) // Light Coral / Orange Red
        val functionColor = Color(0xFF64B5F6) // Cornflower Blue
        val stringColor = Color(0xFFAED581) // Soft Green
        val commentColor = Color(0xFF90A4AE) // Slate Blue Gray
        val numberColor = Color(0xFFBA68C8) // Lavender Purple
        val phoneModuleColor = Color(0xFFFFB74D) // Bright Orange

        // Match python comments: # ...
        val commentRegex = Regex("#.*")
        commentRegex.findAll(code).forEach { match ->
            addStyle(SpanStyle(color = commentColor, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), match.range.first, match.range.last + 1)
        }

        // Match string literals
        val stringRegex1 = Regex("\"[^\"]*\"")
        stringRegex1.findAll(code).forEach { match ->
            addStyle(SpanStyle(color = stringColor), match.range.first, match.range.last + 1)
        }
        val stringRegex2 = Regex("'[^']*'")
        stringRegex2.findAll(code).forEach { match ->
            addStyle(SpanStyle(color = stringColor), match.range.first, match.range.last + 1)
        }

        // Match Keywords
        val keywords = listOf(
            "def", "class", "for", "in", "while", "if", "elif", "else", "import", "return", 
            "and", "or", "not", "True", "False", "None", "as", "from"
        )
        keywords.forEach { word ->
            val wordRegex = Regex("\\b$word\\b")
            wordRegex.findAll(code).forEach { match ->
                // Check if match is inside comments or strings
                if (!isPositionStyled(match.range.first, code)) {
                    addStyle(SpanStyle(color = keywordColor, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
                }
            }
        }

        // Match built-in functions
        val builtins = listOf(
            "print", "input", "range", "len", "str", "int", "float", "abs", "type"
        )
        builtins.forEach { word ->
            val wordRegex = Regex("\\b$word\\b")
            wordRegex.findAll(code).forEach { match ->
                if (!isPositionStyled(match.range.first, code)) {
                    addStyle(SpanStyle(color = functionColor), match.range.first, match.range.last + 1)
                }
            }
        }

        // Match phone and turtle custom modules
        val modules = listOf(
            "phone", "turtle", "random", "math", "device", "sys"
        )
        modules.forEach { word ->
            val wordRegex = Regex("\\b$word\\b")
            wordRegex.findAll(code).forEach { match ->
                if (!isPositionStyled(match.range.first, code)) {
                    addStyle(SpanStyle(color = phoneModuleColor, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
                }
            }
        }

        // Match Numbers
        val numberRegex = Regex("\\b\\d+(\\.\\d+)?\\b")
        numberRegex.findAll(code).forEach { match ->
            if (!isPositionStyled(match.range.first, code)) {
                addStyle(SpanStyle(color = numberColor), match.range.first, match.range.last + 1)
            }
        }
    }
}

// Simple sanity check to avoid styling words nested in comment/string blocks
private fun isPositionStyled(pos: Int, code: String): Boolean {
    // Check if there is an active quote or comment prefixing this position on the same line
    val lineStart = code.lastIndexOf('\n', pos).coerceAtLeast(0)
    val lineSub = code.substring(lineStart, pos)
    if (lineSub.contains("#")) return true
    
    // Check quotes count
    val singleQuotesCount = lineSub.count { it == '\'' }
    val doubleQuotesCount = lineSub.count { it == '"' }
    return (singleQuotesCount % 2 != 0) || (doubleQuotesCount % 2 != 0)
}

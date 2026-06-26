package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class TerminalLine {
    data class Output(val text: String) : TerminalLine()
    data class Error(val text: String, val lineNum: Int) : TerminalLine()
    data class InputText(val prompt: String, val entered: String) : TerminalLine()
}

@Composable
fun TerminalView(
    terminalLines: List<TerminalLine>,
    inputPrompt: String?, // If not null, we need input
    onSendInput: (String) -> Unit,
    onClearTerminal: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Auto-scroll to bottom when new lines are printed
    LaunchedEffect(terminalLines.size, inputPrompt) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F), shape = RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF49454F), shape = RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        // Console Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Terminal",
                    tint = Color(0xFF66BB6A),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CONSOLE TERMINAL",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            TextButton(
                onClick = onClearTerminal,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF5350))
            ) {
                Text("CLEAR", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        // Scrollable logs area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (terminalLines.isEmpty() && inputPrompt == null) {
                    Text(
                        text = ">>> Press RUN 🚀 to start your Python magic spell!",
                        color = Color.DarkGray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }

                terminalLines.forEach { line ->
                    when (line) {
                        is TerminalLine.Output -> {
                            Text(
                                text = line.text,
                                color = Color(0xFF81C784), // Friendly Mint Green
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                modifier = Modifier.testTag("terminal_output_line")
                            )
                        }
                        is TerminalLine.Error -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x1AEF5350), shape = RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0x33EF5350), shape = RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "🚨 Oops! Python found an issue at line ${line.lineNum}:",
                                    color = Color(0xFFEF5350), // Red
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = line.text,
                                    color = Color(0xFFFFCDD2), // Light Red
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    modifier = Modifier.testTag("terminal_error_line")
                                )
                            }
                        }
                        is TerminalLine.InputText -> {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "${line.prompt} ",
                                    color = Color(0xFF64B5F6), // Blue prompt
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = line.entered,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // If currently waiting for python input()
                if (inputPrompt != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$inputPrompt ",
                            color = Color(0xFF64B5F6),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(16.dp)
                                .background(Color(0xFF64B5F6))
                        )
                    }
                }
            }
        }

        // Interactive Input Panel
        if (inputPrompt != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2B2930), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Type here...", color = Color.Gray, fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("terminal_interactive_input"),
                    textStyle = TextStyle(
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (textInput.isNotEmpty()) {
                                onSendInput(textInput)
                                textInput = ""
                            }
                        }
                    ),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        if (textInput.isNotEmpty()) {
                            onSendInput(textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier.testTag("terminal_input_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color(0xFFD0BCFF)
                    )
                }
            }
        }
    }
}

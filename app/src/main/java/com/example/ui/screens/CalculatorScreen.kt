package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CalculatorView()
        }
    }
}

@Composable
fun CalculatorView(modifier: Modifier = Modifier) {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("0") }

    val buttons = listOf(
        listOf("AC", "⌫", "+/-", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("%", "0", ".", "=")
    )

    fun updateResult(expr: String) {
        if (expr.isEmpty()) {
            result = "0"
            return
        }
        try {
            val formattedExpr = expr.replace("÷", "/").replace("×", "*")
            val evalRes = evaluateExpression(formattedExpr)
            result = evalRes
        } catch (e: Exception) {
            // Keep previous or show error during live typing if invalid
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Display Screen
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = expression.ifEmpty { "0" },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = result,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                textAlign = TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Keypad Grid (5x4)
        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { btn ->
                    val isOperator = btn in listOf("÷", "×", "-", "+")
                    val isEquals = btn == "="
                    val isFunction = btn in listOf("AC", "⌫", "+/-")
                    val isPercent = btn == "%"

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isEquals -> MaterialTheme.colorScheme.primary
                                    isOperator -> MaterialTheme.colorScheme.primaryContainer
                                    isFunction -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            )
                            .clickable {
                                when (btn) {
                                    "AC" -> {
                                        expression = ""
                                        result = "0"
                                    }
                                    "⌫" -> {
                                        if (expression.isNotEmpty()) {
                                            expression = expression.dropLast(1)
                                            updateResult(expression)
                                        }
                                    }
                                    "+/-" -> {
                                        if (expression.isNotEmpty()) {
                                            expression = if (expression.startsWith("-")) {
                                                expression.removePrefix("-")
                                            } else {
                                                "-$expression"
                                            }
                                            updateResult(expression)
                                        }
                                    }
                                    "%" -> {
                                        try {
                                            val currentVal = expression.toDoubleOrNull() ?: result.toDoubleOrNull() ?: 0.0
                                            expression = (currentVal / 100.0).toString()
                                            result = expression
                                        } catch (e: Exception) {
                                            result = "Error"
                                        }
                                    }
                                    "=" -> {
                                        try {
                                            val formattedExpr = expression.replace("÷", "/").replace("×", "*")
                                            val evalRes = evaluateExpression(formattedExpr)
                                            result = evalRes
                                            expression = evalRes
                                        } catch (e: Exception) {
                                            result = "Error"
                                        }
                                    }
                                    else -> {
                                        expression += btn
                                        updateResult(expression)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = btn,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isEquals -> MaterialTheme.colorScheme.onPrimary
                                isOperator -> MaterialTheme.colorScheme.onPrimaryContainer
                                isFunction -> MaterialTheme.colorScheme.onSurfaceVariant
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
    }
}

fun evaluateExpression(expr: String): String {
    try {
        var str = expr.replace(" ", "")
        val result = object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < str.length) str[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm()
                    else if (eat('-'.code)) x -= parseTerm()
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor()
                    else if (eat('/'.code)) x /= parseFactor()
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()

                var x: Double
                val startPos = this.pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) {
                    while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                    x = str.substring(startPos, this.pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }
                return x
            }
        }.parse()
        val format = DecimalFormat("0.######")
        return format.format(result)
    } catch (e: Exception) {
        return "Error"
    }
}

package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.python.interpreter.TurtleState

@Composable
fun TurtleCanvas(
    turtleState: TurtleState,
    modifier: Modifier = Modifier
) {
    // Force recomposition when lines are added by registering a state check
    val triggerUpdate = turtleState.lines.size + turtleState.circles.size + turtleState.x.toInt() + turtleState.y.toInt() + turtleState.angle.toInt()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A), shape = RoundedCornerShape(12.dp)) // Deep slate blue space
            .border(2.dp, Color(0xFF334155), shape = RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        // Canvas Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Drawing Board",
                    tint = Color(0xFF06B6D4), // Cyan
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TURTLE CANVAS SCREEN",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            IconButton(
                onClick = { turtleState.reset() },
                modifier = Modifier
                    .size(28.dp)
                    .testTag("reset_canvas_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Board",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Active Graphics Painter
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("turtle_drawing_canvas")
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2f
            val centerY = canvasHeight / 2f

            // Drawing grid dots for alignment (kid-friendly guide!)
            val dotSpacing = 40f
            val rows = (canvasHeight / dotSpacing).toInt()
            val cols = (canvasWidth / dotSpacing).toInt()
            for (r in 0..rows) {
                for (c in 0..cols) {
                    drawCircle(
                        color = Color(0x0F94A3B8),
                        radius = 2f,
                        center = Offset(c * dotSpacing, r * dotSpacing)
                    )
                }
            }

            // Draw completed lines
            turtleState.lines.forEach { line ->
                drawLine(
                    color = line.color,
                    start = Offset(centerX + line.x1, centerY - line.y1),
                    end = Offset(centerX + line.x2, centerY - line.y2),
                    strokeWidth = line.width
                )
            }

            // Draw completed circles
            turtleState.circles.forEach { c ->
                drawCircle(
                    color = c.color,
                    radius = c.radius,
                    center = Offset(centerX + c.cx, centerY - c.cy),
                    style = Stroke(width = c.width)
                )
            }

            // Draw Turtle Cursor if triggerUpdate is bound
            if (triggerUpdate != -99999) {
                val turtleScrX = centerX + turtleState.x
                val turtleScrY = centerY - turtleState.y

                // Rotate turtle cursor in direction of angle (Note: canvas rotation uses degrees clockwise, Turtle is degrees counterclockwise)
                // Turtle 0 degrees is facing right (along +X), canvas rotation 0 is facing right.
                // Since Y-axis is inverted on canvas, let's calculate rotation:
                // Heading angle of 90 is UP, which translates to canvas angle -90 degrees.
                // Hence canvas rotation angle = -turtleState.angle
                rotate(degrees = -turtleState.angle, pivot = Offset(turtleScrX, turtleScrY)) {
                    // Draw a highly charming cartoon turtle!
                    // Shell (green circle)
                    drawCircle(
                        color = Color(0xFF4ADE80), // Pastel Lime Green
                        radius = 12f,
                        center = Offset(turtleScrX, turtleScrY)
                    )
                    // Head (smaller green circle in front, facing right)
                    drawCircle(
                        color = Color(0xFF22C55E),
                        radius = 6f,
                        center = Offset(turtleScrX + 13f, turtleScrY)
                    )
                    // Tiny cute eyes on the head
                    drawCircle(
                        color = Color.Black,
                        radius = 1.2f,
                        center = Offset(turtleScrX + 15f, turtleScrY - 2f)
                    )
                    drawCircle(
                        color = Color.Black,
                        radius = 1.2f,
                        center = Offset(turtleScrX + 15f, turtleScrY + 2f)
                    )
                    
                    // Shell pattern details
                    drawCircle(
                        color = Color(0xFF15803D),
                        radius = 8f,
                        center = Offset(turtleScrX, turtleScrY),
                        style = Stroke(width = 2f)
                    )

                    // Direction indicator arrow inside the shell
                    val arrowPath = Path().apply {
                        moveTo(turtleScrX - 4f, turtleScrY - 4f)
                        lineTo(turtleScrX + 4f, turtleScrY)
                        lineTo(turtleScrX - 4f, turtleScrY + 4f)
                    }
                    drawPath(
                        path = arrowPath,
                        color = Color(0xFF166534),
                        style = Stroke(width = 2f)
                    )
                }
            }
        }
    }
}

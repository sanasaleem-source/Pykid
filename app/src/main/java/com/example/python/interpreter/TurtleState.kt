package com.example.python.interpreter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class TurtleLine(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val color: Color,
    val width: Float
)

data class TurtleCircle(
    val cx: Float,
    val cy: Float,
    val radius: Float,
    val color: Color,
    val width: Float
)

class TurtleState {
    var x by mutableStateOf(0f)
    var y by mutableStateOf(0f)
    var angle by mutableStateOf(0f) // in degrees (0 = right, 90 = up)
    var isPenDown by mutableStateOf(true)
    var penColor by mutableStateOf(Color.Cyan)
    var penWidth by mutableStateOf(4f)
    
    val lines = mutableListOf<TurtleLine>()
    val circles = mutableListOf<TurtleCircle>()
    
    // Callback to trigger re-composition or canvas updates
    var onUpdate: (() -> Unit)? = null

    fun reset() {
        x = 0f
        y = 0f
        angle = 0f
        isPenDown = true
        penColor = Color.Cyan
        penWidth = 4f
        lines.clear()
        circles.clear()
        onUpdate?.invoke()
    }

    fun forward(distance: Float) {
        val rad = angle * PI / 180.0
        val nextX = x + (distance * cos(rad)).toFloat()
        val nextY = y + (distance * sin(rad)).toFloat()

        if (isPenDown) {
            lines.add(TurtleLine(x, y, nextX, nextY, penColor, penWidth))
        }
        x = nextX
        y = nextY
        onUpdate?.invoke()
    }

    fun backward(distance: Float) {
        forward(-distance)
    }

    fun right(degrees: Float) {
        angle = (angle - degrees) % 360f
        onUpdate?.invoke()
    }

    fun left(degrees: Float) {
        angle = (angle + degrees) % 360f
        onUpdate?.invoke()
    }

    fun circle(radius: Float) {
        if (isPenDown) {
            // Draw a circle. For the canvas, the turtle remains at current position or orbits.
            // In standard Python turtle, circle draws a circle tangent to turtle heading.
            // Let's draw it centered to the left/right of the turtle for simplicity
            val rad = (angle + 90) * PI / 180.0
            val cx = x + (radius * cos(rad)).toFloat()
            val cy = y + (radius * sin(rad)).toFloat()
            circles.add(TurtleCircle(cx, cy, radius, penColor, penWidth))
        }
        onUpdate?.invoke()
    }

    fun setPenState(down: Boolean) {
        isPenDown = down
    }

    fun setColorByName(colorName: String) {
        penColor = when (colorName.lowercase().trim()) {
            "red" -> Color(0xFFEF5350)
            "green" -> Color(0xFF66BB6A)
            "blue" -> Color(0xFF42A5F5)
            "yellow" -> Color(0xFFFFEE58)
            "orange" -> Color(0xFFFFA726)
            "purple" -> Color(0xFFAB47BC)
            "pink" -> Color(0xFFEC407A)
            "cyan" -> Color(0xFF26C6DA)
            "white" -> Color.White
            "black" -> Color.Black
            "gray", "grey" -> Color.Gray
            else -> Color.Cyan
        }
    }
}

package com.jv8.utils

import org.lwjgl.glfw.GLFW.*
import org.joml.Vector2d
import com.jv8.engine.WindowContext

object MouseUtils {
    fun getMouseX(): Float {
        val xPos = DoubleArray(1)
        glfwGetCursorPos(WindowContext.windowHandle, xPos, null)
        return xPos[0].toFloat()
    }

    fun getMouseY(): Float {
        val yPos = DoubleArray(1)
        glfwGetCursorPos(WindowContext.windowHandle, null, yPos)
        return yPos[0].toFloat()
    }

    fun getMouseXY(): Vector2d {
        val xPos = DoubleArray(1);
        val yPos = DoubleArray(1);
        glfwGetCursorPos(WindowContext.windowHandle, xPos, yPos)
        return Vector2d(xPos[0], yPos[0])
    }

    fun isMouseButtonPressed(button: Int): Boolean {
        return glfwGetMouseButton(WindowContext.windowHandle, button) == GLFW_PRESS
    }
}

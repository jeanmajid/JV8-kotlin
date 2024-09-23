package com.jv8.utils

import org.lwjgl.glfw.GLFW.*
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

    fun isMouseButtonPressed(button: Int): Boolean {
        return glfwGetMouseButton(WindowContext.windowHandle, button) == GLFW_PRESS
    }
}

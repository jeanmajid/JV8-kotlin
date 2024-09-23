package com.jv8.engine

import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWVidMode
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryUtil

class Window(private val width: Int, private val height: Int, private val title: String) {
    var windowHandle: Long = 0

    fun create() {
        windowHandle = glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL)
        if (windowHandle == MemoryUtil.NULL) {
            throw RuntimeException("Failed to create GLFW window")
        }
        WindowContext.windowHandle = windowHandle
    }

    fun shouldClose(): Boolean {
        return glfwWindowShouldClose(windowHandle)
    }

    fun pollEvents() {
        glfwPollEvents()
    }

    fun clear() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
    }

    fun swapBuffers() {
        glfwSwapBuffers(windowHandle)
    }

    fun destroy() {
        glfwDestroyWindow(windowHandle)
    }
}
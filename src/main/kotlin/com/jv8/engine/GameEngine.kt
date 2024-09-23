package com.jv8.engine

import com.jv8.constants.FontPaths
import com.jv8.renderers.FontRenderer
import com.jv8.ui.components.UIButton
import com.jv8.ui.components.UILabel
import com.jv8.ui.components.UIComponent
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*

class GameEngine {
    private var isRunning = true
    private lateinit var window: Window
    private val uiComponents = mutableListOf<UIComponent>()

    companion object {
        var WINDOW_SIZE = Pair(800, 600)
    }

    fun run() {
        init()
        loop()
        cleanup()
    }

    private fun init() {
        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err))

        if (!glfwInit()) {
            throw IllegalStateException("Unable to initialize GLFW")
        }

        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

        window = Window(WINDOW_SIZE.first, WINDOW_SIZE.second, "JV8")
        window.create()

        glfwMakeContextCurrent(window.windowHandle)
        glfwSwapInterval(1)
        glfwShowWindow(window.windowHandle)

        // Center window
        val vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())!!
        glfwSetWindowPos(
            window.windowHandle,
            (vidMode.width() - WINDOW_SIZE.first) / 2,
            (vidMode.height() - WINDOW_SIZE.second) / 2
        )


        glfwSetWindowSizeCallback(WindowContext.windowHandle) { _, newWidth, newHeight ->
            glViewport(0, 0, newWidth, newHeight)
            glMatrixMode(GL_PROJECTION)
            glLoadIdentity()
            glOrtho(0.0, newWidth.toDouble(), newHeight.toDouble(), 0.0, -1.0, 1.0)
            glMatrixMode(GL_MODELVIEW)
            glLoadIdentity()

            WINDOW_SIZE = Pair(newWidth, newHeight)
            updateUIComponents(newWidth, newHeight)
        }

        // UI components
        val button = UIButton(100f, 100f, 200f, 50f, "Start") {
            println("Button clicked!")
        }
        val label = UILabel(150f, 200f, "Hello, World!", FontPaths.ROBOTO.path)
        val label2 = UILabel(350f, 500f, "Hello, World!", FontPaths.ROBOTO.path)
        uiComponents.add(button)
        uiComponents.add(label)
        uiComponents.add(label2)
    }

    private fun loop() {
        GL.createCapabilities()

        glMatrixMode(GL_PROJECTION)
        glLoadIdentity()
        glOrtho(0.0, GameEngine.WINDOW_SIZE.first.toDouble(), GameEngine.WINDOW_SIZE.second.toDouble(), 0.0, -1.0, 1.0)
        glMatrixMode(GL_MODELVIEW)
        glLoadIdentity()

        while (isRunning && !window.shouldClose()) {
            window.clear()

            uiComponents.forEach { it.handleInput() }
            uiComponents.forEach { it.render() }

            window.pollEvents()
            window.swapBuffers()
        }
    }

    private fun cleanup() {
        window.destroy()
        glfwTerminate()
    }

    private fun updateUIComponents(newWidth: Int, newHeight: Int) {
        uiComponents.forEach { component ->
            component.refreshLayout(newWidth, newHeight);
        }
    }
}

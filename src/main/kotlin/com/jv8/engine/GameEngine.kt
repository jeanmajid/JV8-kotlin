package com.jv8.engine

import com.jv8.audio.SoundPlayer
import com.jv8.ui.components.UIComponent
import com.jv8.ui.components.UILabel
import com.jv8.ui.UIParser
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*

class GameEngine {
    private var isRunning = true
    private lateinit var window: Window
    private val uiComponents = mutableListOf<UIComponent>()

    private var lastTime = System.currentTimeMillis()
    private var frames = 0
    private var fps = 0
    private var fpsCounter = UILabel(10f, 40f, "FPS: 0", "fonts/roboto.json")

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

        val components = UIParser.loadUI("ui/creationMenu.json")
        for (component in components) {
            uiComponents.add(component)
        }

        uiComponents.add(fpsCounter)

        // val soundPlayer = SoundPlayer()

        // soundPlayer.init()
        // soundPlayer.load("sounds/funko.wav")
        // soundPlayer.play()
    }

    private fun loop() {
        GL.createCapabilities()

        glMatrixMode(GL_PROJECTION)
        glLoadIdentity()
        glOrtho(0.0, WINDOW_SIZE.first.toDouble(), WINDOW_SIZE.second.toDouble(), 0.0, -1.0, 1.0)
        glMatrixMode(GL_MODELVIEW)
        glLoadIdentity()

        while (isRunning && !window.shouldClose()) {
            window.clear()

            uiComponents.forEach { it.handleInput() }
            uiComponents.forEach { it.render() }

            frames++
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTime >= 1000) {
                fps = frames
                frames = 0
                lastTime = currentTime
                fpsCounter.text = "FPS: $fps"
            }

            window.pollEvents()
            window.swapBuffers()
        }
    }

    private fun cleanup() {
        window.destroy()
        glfwTerminate()
    }

    private fun updateUIComponents(newWidth: Int, newHeight: Int) {
        uiComponents.forEach { component -> component.refreshLayout(newWidth, newHeight) }
    }
}

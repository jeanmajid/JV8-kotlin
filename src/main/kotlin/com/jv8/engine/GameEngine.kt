package com.jv8.engine

import com.jv8.ui.components.UIComponent
import com.jv8.ui.components.UILabel
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*

import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*
// import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import com.jv8.TD.shader.Shader
import com.jv8.TD.geometry.Cube
import com.jv8.TD.camera.Camera

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
        // glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err))

        // if (!glfwInit()) {
        //     throw IllegalStateException("Unable to initialize GLFW")
        // }

        // glfwDefaultWindowHints()
        // glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        // glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

        // window = Window(WINDOW_SIZE.first, WINDOW_SIZE.second, "JV8")
        // window.create()

        // glfwMakeContextCurrent(window.windowHandle)
        // glfwSwapInterval(1)
        // glfwShowWindow(window.windowHandle)

        // val vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())!!
        // glfwSetWindowPos(
        //         window.windowHandle,
        //         (vidMode.width() - WINDOW_SIZE.first) / 2,
        //         (vidMode.height() - WINDOW_SIZE.second) / 2
        // )

        // glfwSetWindowSizeCallback(WindowContext.windowHandle) { _, newWidth, newHeight ->
        //     glViewport(0, 0, newWidth, newHeight)
        //     glMatrixMode(GL_PROJECTION)
        //     glLoadIdentity()
        //     glOrtho(0.0, newWidth.toDouble(), newHeight.toDouble(), 0.0, -1.0, 1.0)
        //     glMatrixMode(GL_MODELVIEW)
        //     glLoadIdentity()

        //     WINDOW_SIZE = Pair(newWidth, newHeight)
        //     updateUIComponents(newWidth, newHeight)
        // }

        // glfwSetWindowCloseCallback(window.windowHandle) { _ ->
        //     cleanup()
        // }

        // val components = UIParser.loadUI("ui/creationMenu.json")
        // for (component in components) {
        //     uiComponents.add(component)
        // }

        // uiComponents.add(fpsCounter)

        // val soundPlayer = SoundPlayer()

        // soundPlayer.init()
        // soundPlayer.load("sounds/funko.wav")
        // soundPlayer.play()

        if (!glfwInit()) throw IllegalStateException("Unable to initialize GLFW")
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

        val width = 800
        val height = 600
        val window = glfwCreateWindow(width, height, "JV8", 0, 0)
        if (window == 0L) throw RuntimeException("Failed to create GLFW window")

        glfwMakeContextCurrent(window)
        glfwSwapInterval(1)
        glfwShowWindow(window)
        GL.createCapabilities()
        glEnable(GL_DEPTH_TEST)

        val shader = Shader("shaders/vertex.glsl", "shaders/fragment.glsl")

        val cube = Cube()

        val camera = Camera(position = Vector3f(0f, 0f, 3f), target = Vector3f(0f, 0f, 0f))
        val projection =
                Matrix4f()
                        .perspective(
                                Math.toRadians(60.0).toFloat(),
                                width.toFloat() / height,
                                0.1f,
                                100f
                        )

        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents()
            camera.update(window)

            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            shader.bind()
            shader.setUniform("projection", projection)
            shader.setUniform("view", camera.viewMatrix)
            shader.setUniform("model", Matrix4f().identity())

            cube.render()

            shader.unbind()
            glfwSwapBuffers(window)
        }

        // Cleanup resources
        cube.cleanup()
        shader.cleanup()
        glfwDestroyWindow(window)
        glfwTerminate()
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
        System.exit(0)
    }

    private fun updateUIComponents(newWidth: Int, newHeight: Int) {
        uiComponents.forEach { component -> component.refreshLayout(newWidth, newHeight) }
    }
}

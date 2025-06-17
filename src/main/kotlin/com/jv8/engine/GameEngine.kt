package com.jv8.engine

import com.jv8.TD.camera.Camera
import com.jv8.TD.geometry.ObjModel
import com.jv8.TD.shader.Shader
import com.jv8.TD.texture.Texture
import com.jv8.ui.components.UIComponent
import com.jv8.ui.components.UILabel
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*

class GameEngine {
    private var isRunning = true
    private var window: Long = 0L
    private val uiComponents = mutableListOf<UIComponent>()

    private var lastTime = System.currentTimeMillis()
    private var frames = 0
    private var fps = 0
    private var fpsCounter = UILabel(10f, 40f, "FPS: 0", "fonts/roboto.json")
    private var renderDistance = 10000f

    companion object {
        var WINDOW_SIZE = Pair(800, 600)
    }
    private lateinit var shader: Shader
    private lateinit var defaultShader: Shader
    private lateinit var uiShader: Shader
    private lateinit var texture: Texture
    private lateinit var testModel: ObjModel
    private lateinit var camera: Camera
    private lateinit var projection: Matrix4f

    fun run() {
        init()
        mainLoop()
        cleanup()
    }

    private fun init() {
        if (!glfwInit()) throw IllegalStateException("Unable to initialize GLFW")
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_ANY_PROFILE)

        val width = WINDOW_SIZE.first
        val height = WINDOW_SIZE.second
        window = glfwCreateWindow(width, height, "JV8", 0, 0)
        if (window == 0L) throw RuntimeException("Failed to create GLFW window")

        WindowContext.windowHandle = window
        glfwMakeContextCurrent(window)
        glfwSwapInterval(1)
        glfwShowWindow(window)
        GL.createCapabilities()
        glEnable(GL_DEPTH_TEST)

        // // Set clear color to light blue/gray so transparency is visible
        // glClearColor(0.3f, 0.5f, 0.8f, 1.0f)

        // Disable back-face culling for transparency
        glDisable(GL_CULL_FACE)

        // Enable blending for transparency
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        glfwSetWindowSizeCallback(window) { _, newWidth, newHeight ->
            WINDOW_SIZE = Pair(newWidth, newHeight)
            glViewport(0, 0, newWidth, newHeight)

            projection =
                    Matrix4f()
                            .perspective(
                                    Math.toRadians(60.0).toFloat(),
                                    newWidth.toFloat() / newHeight,
                                    0.1f,
                                    renderDistance
                            )

            updateUIComponents(newWidth, newHeight)
        }

        uiShader = Shader("shaders/ui_vertex.glsl", "shaders/ui_fragment.glsl")

        texture = Texture("textures/ravine-cliff_normal-ogl.png")

        fpsCounter.setShader(uiShader) // Create both shader versions
        shader = Shader("shaders/vertex.glsl", "shaders/fragment.glsl")
        defaultShader = Shader("shaders/default_vertex.glsl", "shaders/default_fragment.glsl")

        testModel = ObjModel.load("models/bugatti.obj")

        // Position camera further back to see the whole model
        camera = Camera(position = Vector3f(0f, 0f, 5f), target = Vector3f(0f, 0f, 0f))

        projection =
                Matrix4f()
                        .perspective(
                                Math.toRadians(60.0).toFloat(),
                                width.toFloat() / height,
                                0.1f,
                                renderDistance
                        )

        uiComponents.add(fpsCounter)
    }

    private fun mainLoop() {
        while (!glfwWindowShouldClose(window)) {
            val currentTime = System.currentTimeMillis()
            frames++

            if (currentTime - lastTime >= 1000) {
                fps = frames
                frames = 0
                lastTime = currentTime
                fpsCounter.text = "FPS: $fps"
            }

            glfwPollEvents()

            camera.update(window)

            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            // Choose the appropriate shader based on whether model has materials
            val activeShader = if (testModel.hasMaterials) shader else defaultShader
            activeShader.bind()

            glEnable(GL_DEPTH_TEST)
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

            activeShader.setUniform("projection", projection)
            activeShader.setUniform("view", camera.viewMatrix)
            activeShader.setUniform("model", Matrix4f().identity()) // Set lighting uniforms
            if (testModel.hasMaterials) {
                // Use the complex lighting system for models with materials
                activeShader.setUniform("lights[0].position", Vector3f(0.5f, 1.0f, 0.8f))
                activeShader.setUniform("lights[0].color", Vector3f(1.0f, 1.0f, 1.0f))
                activeShader.setUniform("lights[0].intensity", 2.0f)
                activeShader.setUniform("numLights", 1)
                activeShader.setUniform("viewPos", camera.position)
                testModel.render(activeShader)
            } else {
                // Use enhanced lighting for models without materials
                activeShader.setUniform(
                        "lightPos",
                        Vector3f(2.0f, 3.0f, 4.0f)
                ) // Position light for better angle
                activeShader.setUniform(
                        "lightColor",
                        Vector3f(1.2f, 1.1f, 1.0f)
                )
                activeShader.setUniform("viewPos", camera.position)
                testModel.renderWithDefaultMaterial(activeShader)
            }

            activeShader.unbind()

            uiShader.bind()
            glDisable(GL_DEPTH_TEST)

            val orthoMatrix =
                    Matrix4f()
                            .ortho(
                                    0f,
                                    WINDOW_SIZE.first.toFloat(),
                                    WINDOW_SIZE.second.toFloat(),
                                    0f,
                                    -1f,
                                    1f
                            )
            uiShader.setUniform("projection", orthoMatrix)

            uiComponents.forEach { it.render() }

            uiShader.unbind()

            glfwSwapBuffers(window)
        }
    }
    private fun cleanup() {
        shader.cleanup()
        defaultShader.cleanup()
        uiShader.cleanup()
        testModel.cleanup()
        texture.cleanup()

        glfwTerminate()
        System.exit(0)
    }

    private fun updateUIComponents(newWidth: Int, newHeight: Int) {
        uiComponents.forEach { component -> component.refreshLayout(newWidth, newHeight) }
    }
}

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

    companion object {
        var WINDOW_SIZE = Pair(800, 600)
    }

    private lateinit var shader: Shader
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

        glfwSetWindowSizeCallback(window) { _, newWidth, newHeight ->
            WINDOW_SIZE = Pair(newWidth, newHeight)
            glViewport(0, 0, newWidth, newHeight)

            projection =
                    Matrix4f()
                            .perspective(
                                    Math.toRadians(60.0).toFloat(),
                                    newWidth.toFloat() / newHeight,
                                    0.1f,
                                    100f
                            )

            updateUIComponents(newWidth, newHeight)
        }

        uiShader = Shader("shaders/ui_vertex.glsl", "shaders/ui_fragment.glsl")

        texture = Texture("textures/ravine-cliff_normal-ogl.png")

        fpsCounter.setShader(uiShader)

        shader = Shader("shaders/vertex.glsl", "shaders/fragment.glsl")
        shader.bind()
        shader.setUniform("ambientColor", Vector3f(0.2f, 0.2f, 0.2f))
        shader.setUniform("diffuseColor", Vector3f(0.8f, 0.8f, 0.8f))
        shader.setUniform("specularColor", Vector3f(1.0f, 1.0f, 1.0f))
        shader.setUniform("shininess", 32.0f)
        shader.setUniform("hasDiffuseMap", true)
        shader.setUniform("diffuseMap", 0)
        shader.setUniform("hasNormalMap", false)
        shader.setUniform("hasSpecularMap", false)
        shader.unbind()

        testModel = ObjModel("models/bugatti.obj", true)

        camera = Camera(position = Vector3f(0f, 3f, 10f), target = Vector3f(0f, 0f, 0f))
        projection =
                Matrix4f()
                        .perspective(
                                Math.toRadians(60.0).toFloat(),
                                width.toFloat() / height,
                                0.1f,
                                100f
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

            shader.bind()

            glEnable(GL_DEPTH_TEST)

            shader.setUniform("projection", projection)

            shader.setUniform("view", camera.viewMatrix)

            shader.setUniform("model", Matrix4f().identity())

            shader.setUniform("lightPos", Vector3f(5.0f, 5.0f, 5.0f))
            shader.setUniform("lightColor", Vector3f(1.0f, 1.0f, 1.0f))
            shader.setUniform("viewPos", camera.position)

            texture.bind(0)

            testModel.render(shader)

            shader.unbind()

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

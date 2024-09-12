import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL
import kotlin.random.Random

import ui.UIComponent

fun main() {
    if (!glfwInit()) {
        throw IllegalStateException("Unable to initialize GLFW")
    }

    glfwDefaultWindowHints()
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

    val window = glfwCreateWindow(800, 600, "JV8", NULL, NULL)
    if (window == NULL) {
        throw RuntimeException("Failed to create the GLFW window")
    }

    glfwMakeContextCurrent(window)
    glfwSwapInterval(1)
    glfwShowWindow(window)
    GL.createCapabilities()
    val random = Random(412);

    // Main loop
    while (!glfwWindowShouldClose(window)) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        glBegin(GL_TRIANGLES)
        glColor3f(random.nextFloat(), random.nextFloat(), random.nextFloat())
        glVertex2f(random.nextFloat(), -random.nextFloat())
        glColor3f(random.nextFloat(), random.nextFloat(), random.nextFloat())
        glVertex2f(random.nextFloat(), -random.nextFloat())
        glColor3f(random.nextFloat(), random.nextFloat(), random.nextFloat())
        glVertex2f(random.nextFloat(), -random.nextFloat())
        glEnd()

        glfwSwapBuffers(window)
        glfwPollEvents()
    }

    glfwDestroyWindow(window)
    glfwTerminate()
}

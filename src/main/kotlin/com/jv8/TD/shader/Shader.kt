package com.jv8.TD.shader

import com.jv8.utils.Resource
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL20.*
import org.lwjgl.system.MemoryStack

class Shader(vertexPath: String, fragmentPath: String) {
    val programId: Int

    init {
        val vertexSource = Resource.readFile(vertexPath)
        val fragmentSource = Resource.readFile(fragmentPath)
        val vertexId =
                glCreateShader(GL_VERTEX_SHADER).also {
                    glShaderSource(it, vertexSource)
                    glCompileShader(it)
                }
        val fragmentId =
                glCreateShader(GL_FRAGMENT_SHADER).also {
                    glShaderSource(it, fragmentSource)
                    glCompileShader(it)
                }
        programId = glCreateProgram()
        glAttachShader(programId, vertexId)
        glAttachShader(programId, fragmentId)
        glLinkProgram(programId)
        glDeleteShader(vertexId)
        glDeleteShader(fragmentId)
    }

    fun bind() {
        glUseProgram(programId)
    }
    fun unbind() {
        glUseProgram(0)
    }

    fun setUniform(name: String, matrix: Matrix4f) {
        MemoryStack.stackPush().use { stack ->
            val buffer = stack.mallocFloat(16)
            matrix.get(buffer)
            val location = glGetUniformLocation(programId, name)
            glUniformMatrix4fv(location, false, buffer)
        }
    }

    fun setUniform(name: String, vector: Vector3f) {
        val location = glGetUniformLocation(programId, name)
        glUniform3f(location, vector.x, vector.y, vector.z)
    }

    fun setUniform(name: String, vector: Vector4f) {
        val location = glGetUniformLocation(programId, name)
        glUniform4f(location, vector.x, vector.y, vector.z, vector.w)
    }

    fun setUniform(name: String, value: Float) {
        val location = glGetUniformLocation(programId, name)
        glUniform1f(location, value)
    }

    fun setUniform(name: String, value: Int) {
        val location = glGetUniformLocation(programId, name)
        glUniform1i(location, value)
    }

    fun setUniform(name: String, value: Boolean) {
        val location = glGetUniformLocation(programId, name)
        glUniform1i(location, if (value) 1 else 0)
    }

    fun cleanup() {
        glDeleteProgram(programId)
    }
}

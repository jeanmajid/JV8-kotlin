package com.jv8.TD.shader

import org.lwjgl.opengl.GL20.*
import org.lwjgl.system.MemoryStack
import org.joml.Matrix4f
import com.jv8.utils.Resource


class Shader(vertexPath: String, fragmentPath: String) {
    private val programId: Int

    init {
        val vertexSource = Resource.readFile(vertexPath)
        val fragmentSource = Resource.readFile(fragmentPath)
        val vertexId = glCreateShader(GL_VERTEX_SHADER).also { glShaderSource(it, vertexSource); glCompileShader(it) }
        val fragmentId = glCreateShader(GL_FRAGMENT_SHADER).also { glShaderSource(it, fragmentSource); glCompileShader(it) }
        programId = glCreateProgram()
        glAttachShader(programId, vertexId)
        glAttachShader(programId, fragmentId)
        glLinkProgram(programId)
        glDeleteShader(vertexId)
        glDeleteShader(fragmentId)
    }

    fun bind() { glUseProgram(programId) }
    fun unbind() { glUseProgram(0) }
    
    fun setUniform(name: String, matrix: Matrix4f) {
        MemoryStack.stackPush().use { stack ->
            val buffer = stack.mallocFloat(16)
            matrix.get(buffer)
            val location = glGetUniformLocation(programId, name)
            glUniformMatrix4fv(location, false, buffer)
        }
    }
    
    fun cleanup() { glDeleteProgram(programId) }
}

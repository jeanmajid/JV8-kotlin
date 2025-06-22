package com.jv8.TD.geometry

import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil

class Cube {
    private val vaoId: Int
    private val vertexCount: Int

    init {
        // Define vertices for a cube (positions only for brevity)
        val vertices =
                floatArrayOf(
                        // Front face
                        -0.5f,
                        -0.5f,
                        0.5f,
                        0.5f,
                        -0.5f,
                        0.5f,
                        0.5f,
                        0.5f,
                        0.5f,
                        -0.5f,
                        0.5f,
                        0.5f,
                        // Back face
                        -0.5f,
                        -0.5f,
                        -0.5f,
                        0.5f,
                        -0.5f,
                        -0.5f,
                        0.5f,
                        0.5f,
                        -0.5f,
                        -0.5f,
                        0.5f,
                        -0.5f
                )

        // Define indices for drawing cube with triangles
        val indices =
                intArrayOf(
                        0,
                        1,
                        2,
                        2,
                        3,
                        0, // front
                        4,
                        5,
                        6,
                        6,
                        7,
                        4, // back
                        0,
                        1,
                        5,
                        5,
                        4,
                        0, // bottom
                        2,
                        3,
                        7,
                        7,
                        6,
                        2, // top
                        0,
                        3,
                        7,
                        7,
                        4,
                        0, // left
                        1,
                        2,
                        6,
                        6,
                        5,
                        1 // right
                )
        vertexCount = indices.size

        vaoId = glGenVertexArrays()
        glBindVertexArray(vaoId)

        // Upload vertex data
        val vboId = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vboId)
        val vertexBuffer =
                MemoryUtil.memAllocFloat(vertices.size).put(vertices).flip() as java.nio.FloatBuffer
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * 4, 0)
        glEnableVertexAttribArray(0)
        MemoryUtil.memFree(vertexBuffer)

        // Upload indices
        val eboId = glGenBuffers()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId)
        val indexBuffer =
                MemoryUtil.memAllocInt(indices.size).put(indices).flip() as java.nio.IntBuffer
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(indexBuffer)

        glBindVertexArray(0)
    }

    fun render() {
        glBindVertexArray(vaoId)
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0)
        glBindVertexArray(0)
    }

    fun cleanup() {
        glDeleteVertexArrays(vaoId)
    }
}

package com.jv8.TD.geometry

import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil
import kotlin.math.cos
import kotlin.math.sin

class Circle2D {
    private val vaoId: Int
    private val vertexCount: Int

    init {
        // Number of segments used to approximate the circle
        val segments = 32
        // Total vertices: center + segments + duplicate of first segment for closing the fan
        val totalVertices = segments + 2
        // Radius of the circle
        val radius = 0.5f

        // Create the vertex data (center vertex + edge vertices)
        val vertices = FloatArray(totalVertices * 3)
        // Center vertex
        vertices[0] = 0.0f
        vertices[1] = 0.0f
        vertices[2] = 0.0f

        // Compute edge vertices with an extra vertex at the end to close the fan
        for (i in 0..segments) {
            val angle = i * 2.0 * Math.PI / segments
            val x = (cos(angle) * radius).toFloat()
            val y = (sin(angle) * radius).toFloat()
            val offset = (i + 1) * 3
            vertices[offset] = x
            vertices[offset + 1] = y
            vertices[offset + 2] = 0.0f
        }

        // Create indices for triangle fan: simply 0, 1, 2, ... totalVertices-1
        vertexCount = totalVertices
        val indices = IntArray(totalVertices) { it }

        // Create Vertex Array Object and upload vertex and index data
        vaoId = glGenVertexArrays()
        glBindVertexArray(vaoId)

        // Vertex buffer object
        val vboId = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vboId)
        val vertexBuffer = MemoryUtil.memAllocFloat(vertices.size)
            .put(vertices)
            .flip() as java.nio.FloatBuffer
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)
        // Position attribute pointer layout(location = 0, size = 3)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * 4, 0)
        glEnableVertexAttribArray(0)
        MemoryUtil.memFree(vertexBuffer)

        // Element buffer object
        val eboId = glGenBuffers()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId)
        val indexBuffer = MemoryUtil.memAllocInt(indices.size)
            .put(indices)
            .flip() as java.nio.IntBuffer
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(indexBuffer)

        // Unbind VAO (the EBO remains bound to the VAO)
        glBindVertexArray(0)
    }

    fun render() {
        glBindVertexArray(vaoId)
        glDrawElements(GL_TRIANGLE_FAN, vertexCount, GL_UNSIGNED_INT, 0)
        glBindVertexArray(0)
    }

    fun cleanup() {
        glDeleteVertexArrays(vaoId)
    }
}
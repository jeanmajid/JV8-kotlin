package com.jv8.TD.geometry

import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil

class House {
    private val vaoId: Int
    private val vertexCount: Int

    init {
        // Define vertices for a house
        val vertices = floatArrayOf(
            // Base (walls) - front face
            -0.5f, -0.5f, 0.5f,    // 0: bottom left
             0.5f, -0.5f, 0.5f,    // 1: bottom right
             0.5f,  0.2f, 0.5f,    // 2: top right
            -0.5f,  0.2f, 0.5f,    // 3: top left
            
            // Base (walls) - back face
            -0.5f, -0.5f, -0.5f,   // 4: bottom left
             0.5f, -0.5f, -0.5f,   // 5: bottom right
             0.5f,  0.2f, -0.5f,   // 6: top right
            -0.5f,  0.2f, -0.5f,   // 7: top left
            
            // Roof ridge
             0.0f,  0.7f,  0.5f,   // 8: front roof peak
             0.0f,  0.7f, -0.5f    // 9: back roof peak
        )

        // Define indices for drawing house with triangles
        val indices = intArrayOf(
            // Front wall
            0, 1, 2, 2, 3, 0,
            // Back wall
            4, 5, 6, 6, 7, 4,
            // Left wall
            0, 3, 7, 7, 4, 0,
            // Right wall
            1, 5, 6, 6, 2, 1,
            // Floor
            0, 1, 5, 5, 4, 0,
            
            // Front roof 
            3, 8, 2,
            // Back roof 
            7, 9, 6,
            // Left roof
            3, 7, 9, 9, 8, 3,
            // Right roof
            2, 8, 9, 9, 6, 2
        )
        vertexCount = indices.size

        vaoId = glGenVertexArrays()
        glBindVertexArray(vaoId)

        // Upload vertex data
        val vboId = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vboId)
        val vertexBuffer = MemoryUtil.memAllocFloat(vertices.size).put(vertices).flip() as java.nio.FloatBuffer
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * 4, 0)
        glEnableVertexAttribArray(0)
        MemoryUtil.memFree(vertexBuffer)

        // Upload indices
        val eboId = glGenBuffers()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId)
        val indexBuffer = MemoryUtil.memAllocInt(indices.size).put(indices).flip() as java.nio.IntBuffer
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
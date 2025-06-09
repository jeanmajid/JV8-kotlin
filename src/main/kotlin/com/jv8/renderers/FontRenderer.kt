package com.jv8.renderers

import com.jv8.TD.shader.Shader
import com.jv8.utils.Resource
import org.json.JSONObject
import org.joml.Vector4f
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL33.*
import org.lwjgl.stb.STBTTAlignedQuad
import org.lwjgl.stb.STBTTBakedChar
import org.lwjgl.stb.STBTruetype.*
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer

class FontRenderer {
    private var fontTextureID = -1
    private lateinit var cdata: STBTTBakedChar.Buffer
    private var vao = -1
    private var vbo = -1

    private var fontSize = 48f
    private var bitmapWidth = 512
    private var bitmapHeight = 512
    private lateinit var filePath: String

    init {
        // Create VAO and VBO for text rendering
        vao = glGenVertexArrays()
        vbo = glGenBuffers()
        
        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.SIZE_BYTES, 0L)
        glEnableVertexAttribArray(0)
        
        // Fix: use a long value for the offset
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.SIZE_BYTES, 2L * Float.SIZE_BYTES)
        glEnableVertexAttribArray(1)
        
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    fun loadFontConfig(configPath: String) {
        val configData = Resource.readFile(configPath)
        val jsonConfig = JSONObject(configData)
        fontSize = jsonConfig.getFloat("fontSize")
        bitmapWidth = jsonConfig.getInt("bitmapWidth")
        bitmapHeight = jsonConfig.getInt("bitmapHeight")
        filePath = jsonConfig.getString("filePath")

        loadFont(filePath)
    }

    private fun loadFont(filePath: String) {
        val fontData = Resource.readFileAsByteBuffer(filePath) ?: throw Error("Failed to load font $filePath")
        val bitmap = ByteBuffer.allocateDirect(bitmapWidth * bitmapHeight)
    
        cdata = STBTTBakedChar.malloc(96)
    
        stbtt_BakeFontBitmap(fontData, fontSize, bitmap, bitmapWidth, bitmapHeight, 32, cdata)
    
        fontTextureID = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, fontTextureID)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, bitmapWidth, bitmapHeight, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap)
    
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
     
        val swizzleMask = intArrayOf(GL_RED, GL_RED, GL_RED, GL_RED)
        glTexParameteriv(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_RGBA, swizzleMask)
    }

    fun renderText(x: Float, y: Float, text: String, shader: Shader, color: Vector4f = Vector4f(1f, 1f, 1f, 1f)) {
        if (fontTextureID == -1) {
            throw IllegalStateException("Failed to generate font texture.")
        }

        // Set texture and shader uniforms
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, fontTextureID)
        
        shader.setUniform("texSampler", 0)
        shader.setUniform("color", color)
        shader.setUniform("hasTexture", true)
        
        glBindVertexArray(vao)

        MemoryStack.stackPush().use { stack ->
            val quad = STBTTAlignedQuad.calloc()
            val xPosBuffer = stack.floats(x)
            val yPosBuffer = stack.floats(y)

            for (c in text) {
                if (c.code in 32..126) {
                    stbtt_GetBakedQuad(cdata, bitmapWidth, bitmapHeight, c.code - 32, xPosBuffer, yPosBuffer, quad, true)
                    
                    // Prepare vertex data: 6 vertices (2 triangles) for a quad
                    val vertices = floatArrayOf(
                        quad.x0(), quad.y0(), quad.s0(), quad.t0(),
                        quad.x1(), quad.y0(), quad.s1(), quad.t0(),
                        quad.x0(), quad.y1(), quad.s0(), quad.t1(),
                        
                        quad.x1(), quad.y0(), quad.s1(), quad.t0(),
                        quad.x1(), quad.y1(), quad.s1(), quad.t1(),
                        quad.x0(), quad.y1(), quad.s0(), quad.t1()
                    )
                    
                    // Upload vertex data
                    val vertexBuffer = stack.mallocFloat(vertices.size)
                    vertexBuffer.put(vertices)
                    vertexBuffer.flip()
                    
                    glBindBuffer(GL_ARRAY_BUFFER, vbo)
                    glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW)
                    
                    // Draw the character
                    glDrawArrays(GL_TRIANGLES, 0, 6)
                }
            }
        }

        glBindVertexArray(0)
        glBindTexture(GL_TEXTURE_2D, 0)
    }
    
    fun cleanup() {
        if (::cdata.isInitialized) {
            cdata.free()
        }
        if (vbo != -1) {
            glDeleteBuffers(vbo)
        }
        if (vao != -1) {
            glDeleteVertexArrays(vao)
        }
        if (fontTextureID != -1) {
            glDeleteTextures(fontTextureID)
        }
    }
}
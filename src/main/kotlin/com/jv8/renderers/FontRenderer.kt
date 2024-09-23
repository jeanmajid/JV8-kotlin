package com.jv8.renderers

import com.jv8.utils.Resource
import org.json.JSONObject
import org.lwjgl.opengl.GL11.*
import org.lwjgl.stb.STBTTAlignedQuad
import org.lwjgl.stb.STBTTBakedChar
import org.lwjgl.stb.STBTruetype.*
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer

class FontRenderer {
    private var fontTextureID = -1
    private lateinit var cdata: STBTTBakedChar.Buffer

    private var fontSize = 48f
    private var bitmapWidth = 512
    private var bitmapHeight = 512
    private lateinit var filePath: String

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
    }

    fun renderText(x: Float, y: Float, text: String) {
        if (fontTextureID == -1) {
            throw IllegalStateException("Failed to generate font texture.")
        }

        glBindTexture(GL_TEXTURE_2D, fontTextureID)
        glEnable(GL_TEXTURE_2D)

        MemoryStack.stackPush().use { stack ->
            val quad = STBTTAlignedQuad.mallocStack(stack)
            val xPosBuffer = stack.floats(x)
            val yPosBuffer = stack.floats(y)

            for (c in text) {
                if (c.code in 32..126) {
                    stbtt_GetBakedQuad(cdata, bitmapWidth, bitmapHeight, c.code - 32, xPosBuffer, yPosBuffer, quad, true)

                    glBegin(GL_QUADS)
                    glTexCoord2f(quad.s0(), quad.t0())
                    glVertex2f(quad.x0(), quad.y0())
                    glTexCoord2f(quad.s1(), quad.t0())
                    glVertex2f(quad.x1(), quad.y0())
                    glTexCoord2f(quad.s1(), quad.t1())
                    glVertex2f(quad.x1(), quad.y1())
                    glTexCoord2f(quad.s0(), quad.t1())
                    glVertex2f(quad.x0(), quad.y1())
                    glEnd()
                }
            }
        }

        glDisable(GL_TEXTURE_2D)
    }
}

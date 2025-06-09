package com.jv8.TD.texture

import java.nio.IntBuffer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL30.glGenerateMipmap
import org.lwjgl.stb.STBImage.*

class Texture(private val filepath: String) {
    private var id: Int = 0
    private var width: Int = 0
    private var height: Int = 0

    init {
        id = glGenTextures()

        bind()

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        val widthBuffer: IntBuffer = BufferUtils.createIntBuffer(1)
        val heightBuffer: IntBuffer = BufferUtils.createIntBuffer(1)
        val channelsBuffer: IntBuffer = BufferUtils.createIntBuffer(1)

        stbi_set_flip_vertically_on_load(true)

        val resourceStream =
                javaClass.classLoader.getResourceAsStream(filepath)
                        ?: throw RuntimeException("Failed to find resource: $filepath")
        val bytes = resourceStream.readBytes()
        val nioBuffer = BufferUtils.createByteBuffer(bytes.size)
        nioBuffer.put(bytes)
        nioBuffer.flip()
        val buffer =
                stbi_load_from_memory(nioBuffer, widthBuffer, heightBuffer, channelsBuffer, 4)
                        ?: throw RuntimeException("Failed to load texture from memory: $filepath")

        width = widthBuffer.get(0)
        height = heightBuffer.get(0)

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)

        glGenerateMipmap(GL_TEXTURE_2D)

        stbi_image_free(buffer)

        unbind()
    }

    fun bind(unit: Int = 0) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit)
        glBindTexture(GL_TEXTURE_2D, id)
    }

    fun unbind() {
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    fun cleanup() {
        glDeleteTextures(id)
    }

    fun getId(): Int = id
    fun getWidth(): Int = width
    fun getHeight(): Int = height
}

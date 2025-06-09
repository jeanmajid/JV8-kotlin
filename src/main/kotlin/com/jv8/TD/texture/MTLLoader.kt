package com.jv8.TD.texture

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.stb.STBImage.*
import org.lwjgl.system.MemoryStack
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.util.*

class MTLLoader {
    data class Material(
        val name: String,
        val ambientColor: FloatArray = floatArrayOf(0.2f, 0.2f, 0.2f),
        val diffuseColor: FloatArray = floatArrayOf(0.8f, 0.8f, 0.8f),
        val specularColor: FloatArray = floatArrayOf(1.0f, 1.0f, 1.0f),
        val shininess: Float = 0.0f,
        val diffuseMap: Int = 0, // Texture ID for diffuse map
        val normalMap: Int = 0,  // Texture ID for normal map
        val specularMap: Int = 0 // Texture ID for specular map
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Material) return false
            return name == other.name
        }
        
        override fun hashCode(): Int = name.hashCode()
    }
    
    companion object {
        fun loadMaterials(mtlFilename: String, baseDirectory: String = ""): Map<String, Material> {
            val materials = HashMap<String, Material>()
            var currentMaterial: Material? = null
            
            val classLoader = MTLLoader::class.java.classLoader
            val mtlPath = if (baseDirectory.isEmpty()) mtlFilename else "$baseDirectory/$mtlFilename"
            val resourceStream = classLoader.getResourceAsStream(mtlPath)
                ?: throw RuntimeException("Could not find MTL file: $mtlPath")
            
            BufferedReader(InputStreamReader(resourceStream)).use { reader ->
                var line = reader.readLine()
                
                while (line != null) {
                    val parts = line.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }
                    
                    if (parts.isNotEmpty()) {
                        when (parts[0]) {
                            "newmtl" -> {
                                // Start a new material
                                if (currentMaterial != null) {
                                    materials[currentMaterial.name] = currentMaterial
                                }
                                currentMaterial = Material(parts[1])
                            }
                            "Ka" -> {
                                // Ambient color
                                currentMaterial = currentMaterial?.copy(
                                    ambientColor = floatArrayOf(
                                        parts[1].toFloat(),
                                        parts[2].toFloat(),
                                        parts[3].toFloat()
                                    )
                                )
                            }
                            "Kd" -> {
                                // Diffuse color
                                currentMaterial = currentMaterial?.copy(
                                    diffuseColor = floatArrayOf(
                                        parts[1].toFloat(),
                                        parts[2].toFloat(),
                                        parts[3].toFloat()
                                    )
                                )
                            }
                            "Ks" -> {
                                // Specular color
                                currentMaterial = currentMaterial?.copy(
                                    specularColor = floatArrayOf(
                                        parts[1].toFloat(),
                                        parts[2].toFloat(),
                                        parts[3].toFloat()
                                    )
                                )
                            }
                            "Ns" -> {
                                // Specular exponent (shininess)
                                currentMaterial = currentMaterial?.copy(
                                    shininess = parts[1].toFloat()
                                )
                            }
                            "map_Kd" -> {
                                // Diffuse texture map
                                val texturePath = parts.subList(1, parts.size).joinToString(" ")
                                val textureId = loadTexture("$baseDirectory/$texturePath")
                                currentMaterial = currentMaterial?.copy(diffuseMap = textureId)
                            }
                            "map_Bump", "bump" -> {
                                // Normal map
                                val texturePath = parts.subList(1, parts.size).joinToString(" ")
                                val textureId = loadTexture("$baseDirectory/$texturePath")
                                currentMaterial = currentMaterial?.copy(normalMap = textureId)
                            }
                            "map_Ks" -> {
                                // Specular map
                                val texturePath = parts.subList(1, parts.size).joinToString(" ")
                                val textureId = loadTexture("$baseDirectory/$texturePath")
                                currentMaterial = currentMaterial?.copy(specularMap = textureId)
                            }
                        }
                    }
                    
                    line = reader.readLine()
                }
                
                if (currentMaterial != null) {
                    materials[currentMaterial.name] = currentMaterial
                }
            }
            
            return materials
        }
        
        private fun loadTexture(fileName: String): Int {
            val textureId = glGenTextures()
            glBindTexture(GL_TEXTURE_2D, textureId)
            
            // Set texture parameters
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            
            val classLoader = MTLLoader::class.java.classLoader
            val resourceStream = classLoader.getResourceAsStream(fileName)
                ?: throw RuntimeException("Could not find texture file: $fileName")
            
            val byteArray = resourceStream.readBytes()
            
            MemoryStack.stackPush().use { stack ->
                val w = stack.mallocInt(1)
                val h = stack.mallocInt(1)
                val channels = stack.mallocInt(1)
                
                // Load image using STB
                val buffer = ByteBuffer.allocateDirect(byteArray.size).put(byteArray).flip()
                val image = stbi_load_from_memory(buffer, w, h, channels, 0)
                    ?: throw RuntimeException("Failed to load texture file: ${stbi_failure_reason()}")
                
                // Upload texture to GPU
                val width = w.get()
                val height = h.get()
                val format = when (channels.get()) {
                    1 -> GL_RED
                    3 -> GL_RGB
                    4 -> GL_RGBA
                    else -> GL_RGB // Default
                }
                
                glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, image)
                glGenerateMipmap(GL_TEXTURE_2D)
                
                // Free image memory
                stbi_image_free(image)
            }
            
            // Return texture ID
            return textureId
        }
        
        // Method to free textures when no longer needed
        fun cleanupTexture(textureId: Int) {
            glDeleteTextures(textureId)
        }
    }
}
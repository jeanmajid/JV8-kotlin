package com.jv8.TD.texture

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL30.glGenerateMipmap
import org.lwjgl.system.MemoryStack
import org.lwjgl.stb.STBImage.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.io.File
import java.nio.file.Paths

class MTLLoader {

    data class Material(
        var name: String = "",
        var ambientColor: FloatArray = floatArrayOf(0.2f, 0.2f, 0.2f),
        var diffuseColor: FloatArray = floatArrayOf(0.8f, 0.8f, 0.8f),
        var specularColor: FloatArray = floatArrayOf(1.0f, 1.0f, 1.0f),
        var specularExponent: Float = 0f,
        var alpha: Float = 1.0f,
        var diffuseMap: Int = 0,
        var normalMap: Int = 0,
        var specularMap: Int = 0
    ) {
        val hasDiffuseMap: Boolean get() = diffuseMap > 0
        val hasNormalMap: Boolean get() = normalMap > 0
        val hasSpecularMap: Boolean get() = specularMap > 0
    }

    fun loadMaterialLibrary(mtlFileName: String): Map<String, Material> {
        val materials = HashMap<String, Material>()
        var currentMaterial: Material? = null
        
        val classLoader = javaClass.classLoader
        val resourcePath = mtlFileName.replace("\\", "/")
        val basePath = resourcePath.substringBeforeLast("/")
        
        try {
            val resourceStream = classLoader.getResourceAsStream(resourcePath)
                ?: throw RuntimeException("Could not find MTL file: $resourcePath")
            
            BufferedReader(InputStreamReader(resourceStream)).use { reader ->
                var line = reader.readLine()
                
                while (line != null) {
                    val parts = line.trim().split("\\s+".toRegex())
                    
                    if (parts.isNotEmpty()) {
                        when (parts[0]) {                            "newmtl" -> {
                                if (parts.size > 1) {
                                    val materialName = parts[1]
                                    currentMaterial = Material(name = materialName)
                                    materials[materialName] = currentMaterial!!
                                }
                            }
                            "Ka" -> {
                                if (parts.size > 3) {
                                    currentMaterial?.ambientColor = floatArrayOf(
                                        parts[1].toFloat(),
                                        parts[2].toFloat(),
                                        parts[3].toFloat()
                                    )
                                }
                            }
                            "Kd" -> {
                                if (parts.size > 3) {
                                    currentMaterial?.diffuseColor = floatArrayOf(
                                        parts[1].toFloat(),
                                        parts[2].toFloat(),
                                        parts[3].toFloat()
                                    )
                                }
                            }
                            "Ks" -> {
                                if (parts.size > 3) {
                                    currentMaterial?.specularColor = floatArrayOf(
                                        parts[1].toFloat(),
                                        parts[2].toFloat(),
                                        parts[3].toFloat()
                                    )
                                }
                            }
                            "Ns" -> {
                                if (parts.size > 1) {
                                    currentMaterial?.specularExponent = parts[1].toFloat()
                                }
                            }                            "d", "Tr" -> {
                                if (parts.size > 1) {
                                    val alpha = parts[1].toFloat()
                                    currentMaterial?.alpha = if (parts[0] == "Tr") 1.0f - alpha else alpha

                                }
                            }
                            "map_Kd" -> {
                                if (parts.size > 1) {
                                    val texturePath = parts.subList(1, parts.size).joinToString(" ")
                                    val fullTexturePath = "$basePath/$texturePath"
                                    currentMaterial?.diffuseMap = loadTexture(fullTexturePath)
                                }
                            }                            "map_Bump", "bump" -> {
                                if (parts.size > 1) {
                                    val texturePath = parts.subList(1, parts.size).joinToString(" ")
                                    val fullTexturePath = "$basePath/$texturePath"
                                    currentMaterial?.normalMap = loadTexture(fullTexturePath)
                                }
                            }
                            "map_Ks" -> {
                                if (parts.size > 1) {
                                    val texturePath = parts.subList(1, parts.size).joinToString(" ")
                                    val fullTexturePath = "$basePath/$texturePath"
                                    currentMaterial?.specularMap = loadTexture(fullTexturePath)
                                }                            }
                        }
                    }
                    line = reader.readLine()
                }
            }
              // Finish the last material
            currentMaterial?.let { material ->
                materials[material.name] = material
                // Debug output for transparency
                if (material.alpha < 1.0f) {

                }
            }
        } catch (e: Exception) {
            println("Error loading MTL file: $mtlFileName - ${e.message}")
            e.printStackTrace()
        }
        

        return materials
    }
    
    private fun loadTexture(fileName: String): Int {
        // For now, return 0 to indicate no texture loaded
        // You can implement actual texture loading here if needed
        return 0
    }
}
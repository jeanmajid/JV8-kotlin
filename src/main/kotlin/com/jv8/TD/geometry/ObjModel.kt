package com.jv8.TD.geometry

import com.jv8.TD.shader.Shader
import com.jv8.TD.texture.MTLLoader
import org.joml.Vector3f
import org.lwjgl.opengl.GL13.*
import org.lwjgl.opengl.GL30.*

class ObjModel(filePath: String, loadMaterials: Boolean = false) {
    private val model: ObjLoader.Model
    private val materials: MutableMap<String, MTLLoader.Material> = HashMap()
    private var currentMaterial: MTLLoader.Material? = null

    init {
        // Extract base directory for material files
        val baseDirectory = filePath.substringBeforeLast("/")

        // Load model and materials if requested
        model = ObjLoader.loadObjModel(filePath, loadMaterials)

        // Load materials if available and requested
        if (loadMaterials && model.mtlFile.isNotEmpty()) {
            try {
                val mtlMaterials = MTLLoader.loadMaterials(model.mtlFile, baseDirectory)
                materials.putAll(mtlMaterials)
            } catch (e: Exception) {
                println("Warning: Could not load materials: ${e.message}")
            }
        }
    }

    fun setMaterial(materialName: String) {
        currentMaterial = materials[materialName]
    }

    fun render(shader: Shader? = null) {
        glBindVertexArray(model.vaoId)
        glEnableVertexAttribArray(0) // Position
        glEnableVertexAttribArray(1) // Normal
        // glEnableVertexAttribArray(2) // TexCoord

        // Draw the model
        if (model.groups.isEmpty()) {
            // Draw the entire model with a single call
            currentMaterial?.let { material ->
                // Set material properties in shader if provided
                shader?.let { s: Shader ->
                    s.setUniform(
                            "ambientColor",
                            Vector3f(
                                    material.ambientColor[0],
                                    material.ambientColor[1],
                                    material.ambientColor[2]
                            )
                    )
                    s.setUniform(
                            "diffuseColor",
                            Vector3f(
                                    material.diffuseColor[0],
                                    material.diffuseColor[1],
                                    material.diffuseColor[2]
                            )
                    )
                    s.setUniform(
                            "specularColor",
                            Vector3f(
                                    material.specularColor[0],
                                    material.specularColor[1],
                                    material.specularColor[2]
                            )
                    )
                    s.setUniform("shininess", material.shininess)

                    // Set texture flags and bind textures
                    val hasDiffuseMap = material.diffuseMap > 0
                    val hasNormalMap = material.normalMap > 0
                    val hasSpecularMap = material.specularMap > 0

                    s.setUniform("hasDiffuseMap", hasDiffuseMap)
                    s.setUniform("hasNormalMap", hasNormalMap)
                    s.setUniform("hasSpecularMap", hasSpecularMap)

                    if (hasDiffuseMap) {
                        glActiveTexture(GL_TEXTURE0)
                        glBindTexture(GL_TEXTURE_2D, material.diffuseMap)
                        s.setUniform("diffuseMap", 0)
                    }

                    if (hasNormalMap) {
                        glActiveTexture(GL_TEXTURE1)
                        glBindTexture(GL_TEXTURE_2D, material.normalMap)
                        s.setUniform("normalMap", 1)
                    }

                    if (hasSpecularMap) {
                        glActiveTexture(GL_TEXTURE2)
                        glBindTexture(GL_TEXTURE_2D, material.specularMap)
                        s.setUniform("specularMap", 2)
                    }
                }
            }

            glDrawElements(GL_TRIANGLES, model.vertexCount, GL_UNSIGNED_INT, 0)
        } else {
            // Draw each material group separately
            for (group in model.groups) {
                // Set material for this group
                setMaterial(group.materialName)

                // Set material properties in shader if provided
                currentMaterial?.let { material ->
                    shader?.let { s: Shader ->
                        s.setUniform(
                                "ambientColor",
                                Vector3f(
                                        material.ambientColor[0],
                                        material.ambientColor[1],
                                        material.ambientColor[2]
                                )
                        )
                        s.setUniform(
                                "diffuseColor",
                                Vector3f(
                                        material.diffuseColor[0],
                                        material.diffuseColor[1],
                                        material.diffuseColor[2]
                                )
                        )
                        s.setUniform(
                                "specularColor",
                                Vector3f(
                                        material.specularColor[0],
                                        material.specularColor[1],
                                        material.specularColor[2]
                                )
                        )
                        s.setUniform("shininess", material.shininess)

                        // Set texture flags and bind textures
                        val hasDiffuseMap = material.diffuseMap > 0
                        val hasNormalMap = material.normalMap > 0
                        val hasSpecularMap = material.specularMap > 0

                        s.setUniform("hasDiffuseMap", hasDiffuseMap)
                        s.setUniform("hasNormalMap", hasNormalMap)
                        s.setUniform("hasSpecularMap", hasSpecularMap)

                        if (hasDiffuseMap) {
                            glActiveTexture(GL_TEXTURE0)
                            glBindTexture(GL_TEXTURE_2D, material.diffuseMap)
                            s.setUniform("diffuseMap", 0)
                        }

                        if (hasNormalMap) {
                            glActiveTexture(GL_TEXTURE1)
                            glBindTexture(GL_TEXTURE_2D, material.normalMap)
                            s.setUniform("normalMap", 1)
                        }

                        if (hasSpecularMap) {
                            glActiveTexture(GL_TEXTURE2)
                            glBindTexture(GL_TEXTURE_2D, material.specularMap)
                            s.setUniform("specularMap", 2)
                        }
                    }
                }

                // Draw this group
                glDrawElements(
                        GL_TRIANGLES,
                        group.indexCount,
                        GL_UNSIGNED_INT,
                        group.indexOffset * Int.SIZE_BYTES.toLong()
                )
            }
        }

        // Unbind everything
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, 0)

        // glDisableVertexAttribArray(2)
        glDisableVertexAttribArray(1)
        glDisableVertexAttribArray(0)
        glBindVertexArray(0)
    }

    fun cleanup() {
        glDeleteVertexArrays(model.vaoId)

        // Cleanup textures
        for (material in materials.values) {
            if (material.diffuseMap > 0) MTLLoader.cleanupTexture(material.diffuseMap)
            if (material.normalMap > 0) MTLLoader.cleanupTexture(material.normalMap)
            if (material.specularMap > 0) MTLLoader.cleanupTexture(material.specularMap)
        }
    }
}

package com.jv8.TD.geometry

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import org.joml.Vector3f
import com.jv8.TD.shader.Shader
import com.jv8.TD.texture.MTLLoader

class ObjModel private constructor(
    val vaoID: Int,
    val indexCount: Int,
    val vboIDs: List<Int>,
    val indexVBO: Int,
    val materialGroups: List<ObjLoader.MaterialGroup>,
    val materials: Map<String, MTLLoader.Material>,
    val hasTexCoords: Boolean,
    val hasNormals: Boolean,
    val hasTangents: Boolean
) {
    
    // Check if the model has valid materials loaded from .mtl files
    val hasMaterials: Boolean = materials.isNotEmpty() && materialGroups.isNotEmpty()

    companion object {        fun load(filePath: String): ObjModel {
            val loader = ObjLoader()
            return loader.loadObjModel(filePath)
        }
          fun create(
            vaoID: Int,
            indexCount: Int,
            vboIDs: List<Int>,
            indexVBO: Int,
            materialGroups: List<ObjLoader.MaterialGroup>,
            materials: Map<String, MTLLoader.Material>,
            hasTexCoords: Boolean,
            hasNormals: Boolean,
            hasTangents: Boolean
        ): ObjModel {
            return ObjModel(vaoID, indexCount, vboIDs, indexVBO, materialGroups, materials, hasTexCoords, hasNormals, hasTangents)
        }
    }

    fun render(shader: Shader) {
        glBindVertexArray(vaoID)
          // Enable all attribute arrays
        glEnableVertexAttribArray(0) // vertices
        if (hasTexCoords) glEnableVertexAttribArray(1) // texture coords
        if (hasNormals) glEnableVertexAttribArray(2) // normals
        if (hasTangents) glEnableVertexAttribArray(3) // tangents

        if (materialGroups.isEmpty()) {
            // No material groups, render all vertices without material uniforms
            // (Our geometry shader doesn't use material uniforms)
            glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0)
        } else {
            // Separate opaque and transparent material groups
            val opaqueMaterials = mutableListOf<ObjLoader.MaterialGroup>()
            val transparentMaterials = mutableListOf<ObjLoader.MaterialGroup>()
            
            for (group in materialGroups) {
                val material = materials[group.materialName]
                if (material != null && material.alpha < 1.0f) {
                    transparentMaterials.add(group)
                } else {
                    opaqueMaterials.add(group)
                }
            }
            
            // Render opaque materials first
            for (group in opaqueMaterials) {
                renderMaterialGroup(group, shader)
            }
            
            // Render transparent materials last with depth writing disabled
            if (transparentMaterials.isNotEmpty()) {
                glDepthMask(false) // Disable depth writing for transparent objects
                
                for (group in transparentMaterials) {
                    renderMaterialGroup(group, shader)
                }
                
                glDepthMask(true) // Re-enable depth writing
            }
        }
        
        // Unbind all textures
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, 0)
        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, 0)
        glActiveTexture(GL_TEXTURE2)
        glBindTexture(GL_TEXTURE_2D, 0)
        
        // Disable attribute arrays
        glDisableVertexAttribArray(0)
        if (hasTexCoords) glDisableVertexAttribArray(1)
        if (hasNormals) glDisableVertexAttribArray(2)
        if (hasTangents) glDisableVertexAttribArray(3)
        
        glBindVertexArray(0)
    }
    
    private fun renderMaterialGroup(group: ObjLoader.MaterialGroup, shader: Shader) {
        val material = materials[group.materialName]
        
        if (material != null) {            // Set material properties
            shader.setUniform("material.diffuse", Vector3f(material.diffuseColor[0], material.diffuseColor[1], material.diffuseColor[2]))
            shader.setUniform("material.ambient", Vector3f(material.ambientColor[0], material.ambientColor[1], material.ambientColor[2]))
            shader.setUniform("material.specular", Vector3f(material.specularColor[0], material.specularColor[1], material.specularColor[2]))
            shader.setUniform("material.shininess", material.specularExponent)
            shader.setUniform("material.alpha", material.alpha)
            
            // Handle textures
            val hasDiffuseMap = material.hasDiffuseMap
            val hasNormalMap = material.hasNormalMap
            val hasSpecularMap = material.hasSpecularMap
            
            shader.setUniform("material.hasDiffuseMap", if (hasDiffuseMap) 1 else 0)
            shader.setUniform("material.hasNormalMap", if (hasNormalMap) 1 else 0)
            shader.setUniform("material.hasSpecularMap", if (hasSpecularMap) 1 else 0)
            
            // Bind diffuse texture if available
            if (hasDiffuseMap) {
                glActiveTexture(GL_TEXTURE0)
                glBindTexture(GL_TEXTURE_2D, material.diffuseMap)
                shader.setUniform("material.diffuseMap", 0)
            }
            
            // Bind normal map if available
            if (hasNormalMap) {
                glActiveTexture(GL_TEXTURE1)
                glBindTexture(GL_TEXTURE_2D, material.normalMap)
                shader.setUniform("material.normalMap", 1)
            }
            
            // Bind specular map if available
            if (hasSpecularMap) {
                glActiveTexture(GL_TEXTURE2)
                glBindTexture(GL_TEXTURE_2D, material.specularMap)
                shader.setUniform("material.specularMap", 2)
            }
        } else {
            // Use default material if not found
            shader.setUniform("material.diffuse", Vector3f(0.8f, 0.8f, 0.8f))
            shader.setUniform("material.ambient", Vector3f(0.2f, 0.2f, 0.2f))
            shader.setUniform("material.specular", Vector3f(1.0f, 1.0f, 1.0f))
            shader.setUniform("material.shininess", 32.0f)
            shader.setUniform("material.alpha", 1.0f)
            
            shader.setUniform("material.hasDiffuseMap", 0)
            shader.setUniform("material.hasNormalMap", 0)
            shader.setUniform("material.hasSpecularMap", 0)
        }
          // Draw the material group
        glDrawElements(GL_TRIANGLES, group.indexCount, GL_UNSIGNED_INT, group.startIndex * Integer.BYTES.toLong())
    }
    
    fun cleanup() {
        // Delete VBOs
        this.vboIDs.forEach { vboID: Int ->
            glDeleteBuffers(vboID)
        }
        
        // Delete index VBO
        glDeleteBuffers(this.indexVBO)
        
        // Delete VAO
        glDeleteVertexArrays(this.vaoID)
        
        // Delete textures
        this.materials.values.forEach { material: MTLLoader.Material ->
            if (material.hasDiffuseMap) {
                glDeleteTextures(material.diffuseMap)
            }
            if (material.hasNormalMap) {
                glDeleteTextures(material.normalMap)
            }
            if (material.hasSpecularMap) {
                glDeleteTextures(material.specularMap)
            }
        }
    }
    
    fun renderWithDefaultMaterial(shader: Shader) {
        glBindVertexArray(vaoID)
        
        // Enable all attribute arrays
        glEnableVertexAttribArray(0) // vertices
        if (hasTexCoords) glEnableVertexAttribArray(1) // texture coords  
        if (hasNormals) glEnableVertexAttribArray(2) // normals
        if (hasTangents) glEnableVertexAttribArray(3) // tangents
          // Set default material properties for the simple shader
        shader.setUniform("ambientColor", Vector3f(0.15f, 0.15f, 0.2f))     // Slightly blue ambient
        shader.setUniform("diffuseColor", Vector3f(0.7f, 0.7f, 0.8f))       // Light gray-blue diffuse
        shader.setUniform("specularColor", Vector3f(0.9f, 0.9f, 1.0f))      // Bright white specular
        shader.setUniform("shininess", 64.0f)                               // Higher shininess for better highlights
        
        // Set texture flags to false since we're using default materials
        shader.setUniform("hasDiffuseMap", false)
        shader.setUniform("hasNormalMap", false)
        shader.setUniform("hasSpecularMap", false)
        
        // Render all vertices as one group
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0)
        
        // Disable attribute arrays
        glDisableVertexAttribArray(0)
        if (hasTexCoords) glDisableVertexAttribArray(1)
        if (hasNormals) glDisableVertexAttribArray(2)
        if (hasTangents) glDisableVertexAttribArray(3)
        
        glBindVertexArray(0)
    }
}
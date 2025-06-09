package com.jv8.TD.geometry

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil

class ObjLoader {
    data class MaterialGroup(val materialName: String, val indexOffset: Int, val indexCount: Int)

    data class Model(
            val vaoId: Int,
            val vertexCount: Int,
            val mtlFile: String = "",
            val groups: List<MaterialGroup> = emptyList()
    )

    companion object {
        fun loadObjModel(filename: String, loadMaterials: Boolean = false): Model {
            val vertices = ArrayList<Float>()
            val textures = ArrayList<Float>()
            val normals = ArrayList<Float>()

            val vertexArray = ArrayList<Float>()
            val normalArray = ArrayList<Float>()
            val textureArray = ArrayList<Float>()
            val indexArray = ArrayList<Int>()

            var mtlFile = ""
            val materialGroups = ArrayList<MaterialGroup>()
            var currentMaterial = ""
            var currentGroupStartIndex = 0

            var line: String?
            val classLoader = ObjLoader::class.java.classLoader
            val resourceStream =
                    classLoader.getResourceAsStream(filename)
                            ?: throw RuntimeException("Could not find OBJ file: $filename")

            BufferedReader(InputStreamReader(resourceStream)).use { reader ->
                line = reader.readLine()
                while (line != null) {
                    val parts = line!!.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }
                    if (parts.isNotEmpty()) {
                        when (parts[0]) {
                            "v" -> {
                                // Vertex position
                                vertices.add(parts[1].toFloat())
                                vertices.add(parts[2].toFloat())
                                vertices.add(parts[3].toFloat())
                            }
                            "vt" -> {
                                // Texture coordinate
                                textures.add(parts[1].toFloat())
                                textures.add(parts[2].toFloat())
                            }
                            "vn" -> {
                                // Normal
                                normals.add(parts[1].toFloat())
                                normals.add(parts[2].toFloat())
                                normals.add(parts[3].toFloat())
                            }
                            "f" -> {
                                // Face definition
                                // Support for faces with more than 3 vertices
                                val faceVertices = ArrayList<List<String>>()
                                for (i in 1 until parts.size) {
                                    faceVertices.add(parts[i].split("/"))
                                }

                                // Triangulate the face
                                triangulate(
                                        faceVertices,
                                        textureArray,
                                        normalArray,
                                        vertices,
                                        textures,
                                        normals,
                                        vertexArray,
                                        indexArray
                                )
                            }
                            // Only process material-related lines if loadMaterials is true
                            "mtllib" -> {
                                if (loadMaterials) {
                                    // Material library reference
                                    mtlFile = parts[1]
                                }
                            }
                            "usemtl" -> {
                                if (loadMaterials) {
                                    // Material usage
                                    // If we already had a material, add the previous group
                                    if (currentMaterial.isNotEmpty() &&
                                                    indexArray.size > currentGroupStartIndex
                                    ) {
                                        materialGroups.add(
                                                MaterialGroup(
                                                        currentMaterial,
                                                        currentGroupStartIndex,
                                                        indexArray.size - currentGroupStartIndex
                                                )
                                        )
                                    }

                                    // Start a new material group
                                    currentMaterial = parts[1]
                                    currentGroupStartIndex = indexArray.size
                                }
                            }
                        }
                    }
                    line = reader.readLine()
                }
            }

            // Add final material group if needed and if loading materials
            if (loadMaterials && currentMaterial.isNotEmpty() && indexArray.size > currentGroupStartIndex) {
                materialGroups.add(
                        MaterialGroup(
                                currentMaterial,
                                currentGroupStartIndex,
                                indexArray.size - currentGroupStartIndex
                        )
                )
            }

            // Create VAO
            val vaoId = glGenVertexArrays()
            glBindVertexArray(vaoId)

            // Vertex positions VBO
            storeDataInAttributeList(0, 3, vertexArray.toFloatArray())

            // Normals VBO (if available)
            if (normalArray.isNotEmpty()) {
                storeDataInAttributeList(1, 3, normalArray.toFloatArray())
            }

            // Texture coords VBO (if available)
            if (textureArray.isNotEmpty()) {
                storeDataInAttributeList(2, 2, textureArray.toFloatArray())
            }

            // Index buffer
            val indexVbo = glGenBuffers()
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexVbo)
            val indicesBuffer = createIntBuffer(indexArray.toIntArray())
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW)
            MemoryUtil.memFree(indicesBuffer)

            glBindVertexArray(0)

            return if (loadMaterials) {
                Model(vaoId, indexArray.size, mtlFile, materialGroups)
            } else {
                Model(vaoId, indexArray.size, "", emptyList())
            }
        }

        private fun triangulate(
                faceVertices: List<List<String>>,
                textureCoords: ArrayList<Float>,
                normals: ArrayList<Float>,
                vertices: ArrayList<Float>,
                textures: ArrayList<Float>,
                normalsIn: ArrayList<Float>,
                vertexArray: ArrayList<Float>,
                indexArray: ArrayList<Int>
        ) {

            // Handle triangular faces directly
            if (faceVertices.size == 3) {
                processVertex(
                        faceVertices[0],
                        textureCoords,
                        normals,
                        vertices,
                        textures,
                        normalsIn,
                        vertexArray
                )
                processVertex(
                        faceVertices[1],
                        textureCoords,
                        normals,
                        vertices,
                        textures,
                        normalsIn,
                        vertexArray
                )
                processVertex(
                        faceVertices[2],
                        textureCoords,
                        normals,
                        vertices,
                        textures,
                        normalsIn,
                        vertexArray
                )

                // Add indices for this triangle
                val baseIndex = vertexArray.size / 3 - 3
                indexArray.add(baseIndex)
                indexArray.add(baseIndex + 1)
                indexArray.add(baseIndex + 2)
                return
            }

            // Triangulate faces with more than 3 vertices using fan triangulation
            val baseIndex = vertexArray.size / 3

            // Process all vertices of the face
            for (vertexData in faceVertices) {
                processVertex(
                        vertexData,
                        textureCoords,
                        normals,
                        vertices,
                        textures,
                        normalsIn,
                        vertexArray
                )
            }

            // Create triangles - connect each vertex with the first vertex to form a fan
            for (i in 1 until faceVertices.size - 1) {
                indexArray.add(baseIndex) // First vertex
                indexArray.add(baseIndex + i) // Second vertex
                indexArray.add(baseIndex + i + 1) // Third vertex
            }
        }

        private fun processVertex(
                vertexData: List<String>,
                textureCoords: ArrayList<Float>,
                normals: ArrayList<Float>,
                vertices: ArrayList<Float>,
                textures: ArrayList<Float>,
                normalsIn: ArrayList<Float>,
                vertexArray: ArrayList<Float>
        ) {
            // OBJ indices are 1-based, so we subtract 1
            val vertexIndex = vertexData[0].toInt() - 1

            // Add vertex position data to vertexArray
            val vx = vertices[vertexIndex * 3]
            val vy = vertices[vertexIndex * 3 + 1]
            val vz = vertices[vertexIndex * 3 + 2]
            vertexArray.add(vx)
            vertexArray.add(vy)
            vertexArray.add(vz)

            // Process texture coordinates if available
            if (vertexData.size > 1 && vertexData[1].isNotEmpty()) {
                val textureIndex = vertexData[1].toInt() - 1
                val texU = textures[textureIndex * 2]
                val texV = textures[textureIndex * 2 + 1]
                textureCoords.add(texU)
                textureCoords.add(texV)
            }

            // Process normals if available
            if (vertexData.size > 2 && vertexData[2].isNotEmpty()) {
                val normalIndex = vertexData[2].toInt() - 1
                val normalX = normalsIn[normalIndex * 3]
                val normalY = normalsIn[normalIndex * 3 + 1]
                val normalZ = normalsIn[normalIndex * 3 + 2]
                normals.add(normalX)
                normals.add(normalY)
                normals.add(normalZ)
            }
        }

        private fun storeDataInAttributeList(
                attributeNumber: Int,
                coordinateSize: Int,
                data: FloatArray
        ): Int {
            val vboId = glGenBuffers()
            glBindBuffer(GL_ARRAY_BUFFER, vboId)
            val buffer = createFloatBuffer(data)
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
            glVertexAttribPointer(attributeNumber, coordinateSize, GL_FLOAT, false, 0, 0)
            glEnableVertexAttribArray(attributeNumber)
            MemoryUtil.memFree(buffer)
            return vboId
        }

        private fun createFloatBuffer(data: FloatArray): FloatBuffer {
            val buffer = MemoryUtil.memAllocFloat(data.size)
            buffer.put(data).flip()
            return buffer
        }

        private fun createIntBuffer(data: IntArray): IntBuffer {
            val buffer = MemoryUtil.memAllocInt(data.size)
            buffer.put(data).flip()
            return buffer
        }
    }
}

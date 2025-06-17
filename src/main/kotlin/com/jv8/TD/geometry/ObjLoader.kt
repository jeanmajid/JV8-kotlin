package com.jv8.TD.geometry

import com.jv8.TD.texture.MTLLoader
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*

class ObjLoader {
    data class MaterialGroup(val materialName: String, val startIndex: Int, val indexCount: Int)
    
    // Configuration for large model handling
    private val MAX_VERTICES_WARNING = 100000
    private val MAX_FACES_WARNING = 200000
    
    private fun validateModelSize(vertexCount: Int, faceCount: Int): Boolean {
        if (vertexCount > MAX_VERTICES_WARNING || faceCount > MAX_FACES_WARNING) {
            println("WARNING: Large model detected!")
            println("  Vertices: $vertexCount (max recommended: $MAX_VERTICES_WARNING)")
            println("  Faces: $faceCount (max recommended: $MAX_FACES_WARNING)")
            println("  Loading may take a long time and use significant memory...")
            
            // Check available memory
            val runtime = Runtime.getRuntime()
            val maxMemory = runtime.maxMemory() / (1024 * 1024) // MB
            val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) // MB
            val freeMemory = maxMemory - usedMemory
            
            println("  Available memory: ${freeMemory}MB / ${maxMemory}MB")
            
            if (freeMemory < 500) { // Less than 500MB free
                println("  WARNING: Low memory available for loading this model!")
                return false
            }
        }
        return true
    }

    fun loadObjModel(fileName: String): ObjModel {
        println("Loading OBJ model: $fileName")
        
        // Use ArrayList with initial capacity for better performance
        val vertList = ArrayList<Vector3f>()
        val textList = ArrayList<Vector2f>()
        val normList = ArrayList<Vector3f>()
        val vertices = ArrayList<Float>()
        val textures = ArrayList<Float>()
        val normals = ArrayList<Float>()
        val indices = ArrayList<Int>()
        val materialGroups = mutableListOf<MaterialGroup>()
        var materialLibrary: Map<String, MTLLoader.Material> = emptyMap()
        var currentMaterial = ""
        var materialStartIndex = 0
        val vertexMap = mutableMapOf<String, Int>()
        var vertexIndex = 0
        
        // Progress tracking for large models
        var lineCount = 0
        var vertexCount = 0
        var faceCount = 0

        try {
            val classLoader = javaClass.classLoader
            val resourcePath = fileName.replace("\\", "/")
            val resourceStream =
                    classLoader.getResourceAsStream(resourcePath)
                            ?: throw RuntimeException("Could not find OBJ file: $resourcePath")

            val lines = resourceStream.bufferedReader().readLines()
            val basePath = resourcePath.substringBeforeLast("/")

            // First pass: Load material library
            for (line in lines) {
                val parts = line.trim().split("\\s+".toRegex())
                if (parts.isNotEmpty()) {
                    when (parts[0]) {
                        "mtllib" -> {
                            if (parts.size > 1) {
                                val mtlPath =
                                        if (basePath.isNotEmpty()) "$basePath/${parts[1]}"
                                        else parts[1]
                                val mtlLoader = MTLLoader()
                                materialLibrary = mtlLoader.loadMaterialLibrary(mtlPath)
                            }
                        }
                    }                }
            }

            // Validate model size before processing
            if (!validateModelSize(vertList.size, lines.count { it.trim().startsWith("f ") })) {
                throw RuntimeException("Model is too large to load safely with current memory settings")
            }

            // Second pass: Process geometry
            println("Processing geometry...")
            for (line in lines) {
                lineCount++
                if (lineCount % 50000 == 0) {
                    println("Processed $lineCount lines, vertices: $vertexCount, faces: $faceCount")
                }
                
                val parts = line.trim().split("\\s+".toRegex())
                if (parts.isNotEmpty()) {
                    when (parts[0]) {
                        "v" -> {
                            if (parts.size >= 4) {
                                try {
                                    vertList.add(
                                            Vector3f(
                                                    parts[1].toFloat(),
                                                    parts[2].toFloat(),
                                                    parts[3].toFloat()
                                            )
                                    )
                                    vertexCount++
                                } catch (e: NumberFormatException) {
                                    println("Warning: Invalid vertex data at line $lineCount: $line")
                                }
                            }
                        }                        "vt" -> {
                            if (parts.size >= 3) {
                                try {
                                    textList.add(
                                            Vector2f(parts[1].toFloat(), 1.0f - parts[2].toFloat())
                                    )
                                } catch (e: NumberFormatException) {
                                    println("Warning: Invalid texture coordinate at line $lineCount: $line")
                                }
                            }
                        }
                        "vn" -> {
                            if (parts.size >= 4) {
                                try {
                                    normList.add(
                                            Vector3f(
                                                    parts[1].toFloat(),
                                                    parts[2].toFloat(),
                                                    parts[3].toFloat()
                                            )
                                    )
                                } catch (e: NumberFormatException) {
                                    println("Warning: Invalid normal data at line $lineCount: $line")
                                }
                            }
                        }
                        "usemtl" -> {
                            if (parts.size > 1) {
                                // Finish previous material group
                                if (currentMaterial.isNotEmpty() &&
                                                indices.size > materialStartIndex
                                ) {
                                    val indexCount = indices.size - materialStartIndex
                                    materialGroups.add(
                                            MaterialGroup(
                                                    currentMaterial,
                                                    materialStartIndex,
                                                    indexCount
                                            )
                                    )
                                }

                                // Start new material group
                                currentMaterial = parts[1]
                                materialStartIndex = indices.size
                            }
                        }                        "f" -> {
                            if (parts.size >= 4) {
                                faceCount++
                                try {
                                    val faceIndices = mutableListOf<Int>()

                                    // Process each vertex in the face
                                    for (i in 1 until parts.size) {
                                        val vertexData = parts[i].split("/")

                                        if (vertexData.isNotEmpty()) {
                                            val vertexKey =
                                                    parts[i] // Use the complete vertex/texture/normal
                                            // combination as key

                                            val finalIndex =
                                                    vertexMap.getOrPut(vertexKey) {
                                                        // This is a new unique vertex combination, add
                                                        // it
                                                        val vIndex = vertexData[0].toInt() - 1
                                                        if (vIndex < 0 || vIndex >= vertList.size) {
                                                            throw IndexOutOfBoundsException("Vertex index $vIndex out of bounds (max: ${vertList.size - 1})")
                                                        }
                                                        
                                                        val vertex = vertList[vIndex]
                                                        vertices.add(vertex.x)
                                                        vertices.add(vertex.y)
                                                        vertices.add(vertex.z)

                                                        // Process texture coordinate if available
                                                        if (vertexData.size > 1 &&
                                                                        vertexData[1].isNotEmpty()
                                                        ) {
                                                            val textureIndex = vertexData[1].toInt() - 1
                                                            if (textureIndex >= 0 &&
                                                                            textureIndex < textList.size
                                                            ) {
                                                                val texture = textList[textureIndex]
                                                                textures.add(texture.x)
                                                                textures.add(texture.y)
                                                            } else {
                                                                textures.add(0f)
                                                                textures.add(0f)
                                                            }
                                                        } else {
                                                            textures.add(0f)
                                                            textures.add(0f)
                                                        }

                                                        // Process normal coordinate if available
                                                        if (vertexData.size > 2 &&
                                                                        vertexData[2].isNotEmpty()
                                                        ) {
                                                            val normalIndex = vertexData[2].toInt() - 1
                                                            if (normalIndex >= 0 &&
                                                                            normalIndex < normList.size
                                                            ) {
                                                                val normal = normList[normalIndex]
                                                                normals.add(normal.x)
                                                                normals.add(normal.y)
                                                                normals.add(normal.z)
                                                            } else {
                                                                normals.add(0f)
                                                                normals.add(1f)
                                                                normals.add(0f)
                                                            }
                                                        } else {
                                                            normals.add(0f)
                                                            normals.add(1f)
                                                            normals.add(0f)
                                                        }

                                                        vertexIndex++
                                                        vertexIndex -
                                                                1 // Return the index for this vertex
                                                    }

                                            faceIndices.add(finalIndex)
                                        }
                                    }

                                    // Triangulate the face (fan triangulation for polygons)
                                    if (faceIndices.size >= 3) {
                                        if (faceIndices.size == 3) {
                                            // Triangle - add directly
                                            indices.addAll(faceIndices)
                                        } else {
                                            // Polygon - triangulate using fan method
                                            for (i in 1 until faceIndices.size - 1) {
                                                indices.add(faceIndices[0])
                                                indices.add(faceIndices[i])
                                                indices.add(faceIndices[i + 1])
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    println("Warning: Error processing face at line $lineCount: $line - ${e.message}")
                                    faceCount-- // Don't count failed faces
                                }
                            }
                        }
                    }
                }
            }            // Finish last material group
            if (currentMaterial.isNotEmpty() && indices.size > materialStartIndex) {
                val indexCount = indices.size - materialStartIndex
                materialGroups.add(MaterialGroup(currentMaterial, materialStartIndex, indexCount))
            }
            
            println("OBJ loading complete:")
            println("  Original vertices: ${vertList.size}")
            println("  Original faces: $faceCount")
            println("  Final vertex data: ${vertices.size / 3}")
            println("  Final indices: ${indices.size}")
            println("  Material groups: ${materialGroups.size}")
            
        } catch (e: OutOfMemoryError) {
            println("Out of memory loading OBJ file: $fileName")
            println("Model is too large. Consider using a smaller/optimized version.")
            throw e
        } catch (e: Exception) {
            println("Error loading OBJ file: $fileName - ${e.message}")
            e.printStackTrace()
            throw e
        }

        return createModel(
                vertices,
                textures,
                normals,
                indices,
                materialGroups,
                materialLibrary,
                textures.isNotEmpty(),
                normals.isNotEmpty(),
                false
        )
    }    private fun createModel(
            vertices: List<Float>,
            textures: List<Float>,
            normals: List<Float>,
            indices: List<Int>,
            materialGroups: List<MaterialGroup>,
            materials: Map<String, MTLLoader.Material>,
            hasTexCoords: Boolean,
            hasNormals: Boolean,
            hasTangents: Boolean
    ): ObjModel {
        println("Creating OpenGL buffers...")
        println("  Vertices: ${vertices.size} floats (${vertices.size / 3} vertices)")
        println("  Indices: ${indices.size}")
        
        try {
            val vaoID = glGenVertexArrays()
            glBindVertexArray(vaoID)

            val vboIDs = mutableListOf<Int>()

            // Vertex VBO
            val vertexVBO = glGenBuffers()
            vboIDs.add(vertexVBO)
            val vertexBuffer = BufferUtils.createFloatBuffer(vertices.size)
            vertexBuffer.put(vertices.toFloatArray()).flip()
            glBindBuffer(GL_ARRAY_BUFFER, vertexVBO)
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)

        // Texture coordinates VBO
        if (hasTexCoords && textures.isNotEmpty()) {
            val textureVBO = glGenBuffers()
            vboIDs.add(textureVBO)
            val textureBuffer = BufferUtils.createFloatBuffer(textures.size)
            textureBuffer.put(textures.toFloatArray()).flip()
            glBindBuffer(GL_ARRAY_BUFFER, textureVBO)
            glBufferData(GL_ARRAY_BUFFER, textureBuffer, GL_STATIC_DRAW)
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0)
        }

        // Normals VBO
        if (hasNormals && normals.isNotEmpty()) {
            val normalVBO = glGenBuffers()
            vboIDs.add(normalVBO)
            val normalBuffer = BufferUtils.createFloatBuffer(normals.size)
            normalBuffer.put(normals.toFloatArray()).flip()
            glBindBuffer(GL_ARRAY_BUFFER, normalVBO)
            glBufferData(GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW)
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0)
        }        // Index VBO
        val indexVBO = glGenBuffers()
        val indexBuffer = BufferUtils.createIntBuffer(indices.size)
        indexBuffer.put(indices.toIntArray()).flip()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexVBO)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW)

        glBindVertexArray(0)
        
        println("OpenGL buffers created successfully")

        return ObjModel.create(
                vaoID,
                indices.size,
                vboIDs,
                indexVBO,
                materialGroups,
                materials,
                hasTexCoords,
                hasNormals,
                hasTangents
        )
        } catch (e: Exception) {
            println("Error creating OpenGL buffers: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}

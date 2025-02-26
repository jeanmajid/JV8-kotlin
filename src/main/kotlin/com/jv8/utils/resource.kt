package com.jv8.utils

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.charset.StandardCharsets

object Resource {
    fun readFile(fileName: String): String {
        val inputStream: InputStream? = readFileAsInputStream(fileName)

        return inputStream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }
                ?: throw IllegalArgumentException("File $fileName not found in resources")
    }

    fun readFileAsByteBuffer(fileName: String): ByteBuffer? {
        val inputStream: InputStream? = readFileAsInputStream(fileName)

        return inputStream?.use { stream ->
            val channel = Channels.newChannel(stream)
            val buffer = ByteBuffer.allocateDirect(stream.available())

            while (channel.read(buffer) > 0) ;

            buffer.flip()
            buffer
        }
                ?: throw IllegalArgumentException("File $fileName not found in resources")
    }

    fun readFileAsInputStream(fileName: String): InputStream? {
        return object {}.javaClass.getResourceAsStream("/$fileName")
    }
}

package com.jv8.audio

import com.jv8.utils.Resource
import java.nio.ByteBuffer
import java.nio.IntBuffer
import javax.sound.sampled.*
import org.lwjgl.openal.AL
import org.lwjgl.openal.AL10.*
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALC10

class SoundPlayer {
    private var device: Long = 0
    private var context: Long = 0
    private var buffer: Int = 0
    private var source: Int = 0

    fun init() {
        device = ALC10.alcOpenDevice(null as ByteBuffer?)
        if (device == 0L) {
            throw IllegalStateException("Failed to open the default audio device")
        }

        context = ALC10.alcCreateContext(device, null as IntBuffer?)
        if (context == 0L) {
            ALC10.alcCloseDevice(device)
            throw IllegalStateException("Failed to create OpenAL context")
        }

        ALC10.alcMakeContextCurrent(context)
        AL.createCapabilities(ALC.createCapabilities(device))

        buffer = alGenBuffers()
        source = alGenSources()

        if (alGetError() != AL_NO_ERROR) {
            throw IllegalStateException("Error initializing OpenAL")
        }
    }

    fun load(filePath: String) {
        val audioInputStream = getAudioInputStream(filePath)
        val format = audioInputStream.format

        val audioBuffer = convertStreamToByteBuffer(audioInputStream, format)
        audioInputStream.close()

        alBufferData(buffer, getOpenALFormat(format), audioBuffer, format.sampleRate.toInt())

        alSourcei(source, AL_BUFFER, buffer)

        if (alGetError() != AL_NO_ERROR) {
            throw IllegalStateException("Error loading sound into OpenAL buffer")
        }
    }

    fun play(gain: Float = 1.0f, pitch: Float = 1.0f) {
        alSourcef(source, AL_GAIN, gain)
        alSourcef(source, AL_PITCH, pitch)

        val state = alGetSourcei(source, AL_SOURCE_STATE)
        if (state != AL_PLAYING) {
            alSourcePlay(source)

            if (alGetError() != AL_NO_ERROR) {
                throw IllegalStateException("Error playing sound")
            }
        }
    }

    fun stop() {
        alSourceStop(source)

        if (alGetError() != AL_NO_ERROR) {
            throw IllegalStateException("Error stopping sound")
        }
    }

    fun cleanup() {
        alDeleteSources(source)
        alDeleteBuffers(buffer)

        ALC10.alcDestroyContext(context)
        ALC10.alcCloseDevice(device)

        if (alGetError() != AL_NO_ERROR) {
            throw IllegalStateException("Error cleaning up OpenAL resources")
        }
    }

    private fun getAudioInputStream(filePath: String): AudioInputStream {
        val inputStream = Resource.readFileAsInputStream(filePath)
        val audioInputStream = AudioSystem.getAudioInputStream(inputStream)
        val format = audioInputStream.format

        // println(
        //         "Audio Format: Channels=${format.channels}, SampleRate=${format.sampleRate}, SampleSizeInBits=${format.sampleSizeInBits}"
        // )

        val desiredFormat =
                AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        44100f,
                        16,
                        format.channels,
                        format.channels * 2,
                        44100f,
                        false
                )
        if (!format.matches(desiredFormat)) {
            return AudioSystem.getAudioInputStream(desiredFormat, audioInputStream)
        }
        return audioInputStream
    }

    private fun convertStreamToByteBuffer(
            audioInputStream: AudioInputStream,
            format: AudioFormat
    ): ByteBuffer {
        val audioBytes = audioInputStream.readAllBytes()
        val buffer = ByteBuffer.allocateDirect(audioBytes.size)
        buffer.put(audioBytes)
        buffer.flip()
        return buffer
    }

    private fun getOpenALFormat(format: AudioFormat): Int {
        return when (format.channels) {
            1 -> if (format.sampleSizeInBits == 8) AL_FORMAT_MONO8 else AL_FORMAT_MONO16
            2 -> if (format.sampleSizeInBits == 8) AL_FORMAT_STEREO8 else AL_FORMAT_STEREO16
            else ->
                    throw IllegalArgumentException(
                            "Unsupported audio format: ${format.channels} channels, ${format.sampleSizeInBits} bits per sample"
                    )
        }
    }
}

package com.jv8.ipc

import io.github.vyfor.kpresence.RichClient
import io.github.vyfor.kpresence.rpc.ActivityType
import io.github.vyfor.kpresence.ConnectionState

class DiscordRPC(private val clientID: Long) {
    private val client = RichClient(clientID)
    private var activityStartTime: Long = System.currentTimeMillis()

    init {
        tryConnect()
    }

    fun tryConnect() {
        try {
            client.connect()
        } catch (e: Exception) {}
    }

    fun updatePresence(newDetails: String, newState: String) {
        if (!isConnected()) {
            return
        }
        client.update {
            type = ActivityType.GAME

            details = newDetails
            state = newState

            timestamps { start = activityStartTime }

            assets { largeImage = "logo2" }
        }
    }

    fun isConnected(): Boolean {
        return client.connectionState == ConnectionState.SENT_HANDSHAKE
    }

    fun close() {
        client.shutdown()
    }
}

package com.jv8

import com.jv8.engine.GameEngine
import com.jv8.ipc.DiscordRPC
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun main() {
    val gameEngine = GameEngine()
    val discord = DiscordRPC(1292206787516436621)

    val scheduler = Executors.newScheduledThreadPool(1)
    var failedAttempts = 0;

    scheduler.scheduleAtFixedRate(
            { 
                if (!discord.isConnected()) {
                    println("Failed to connect to Discord RPC, retrying in 15 seconds.")
                    failedAttempts++
                    if (failedAttempts >= 3) {
                        println("Failed to connect to Discord RPC after 3 attempts, shutting down.")
                        scheduler.shutdown()
                    }
                    return@scheduleAtFixedRate
                }
                discord.updatePresence("Game engine WIP", "For a school project using Kotlin") },
            0,
            15,
            TimeUnit.SECONDS
    )

    gameEngine.run()
}

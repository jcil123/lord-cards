package org.lords.server

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.http.*
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

object GameManager {
    // Map of gameId, list of active games
    private val games = ConcurrentHashMap<String, MutableList<DefaultWebSocketServerSession>>()

    // create a new game and add the player (create an unique ID too)
    fun createGame(session: DefaultWebSocketServerSession): String {
        val gameId = UUID.randomUUID().toString()
        games[gameId] = mutableListOf(session)
        return gameId
    }

    // add player to game 
    fun joinGame(gameId: String, session: DefaultWebSocketServerSession): Boolean {
        val players = games[gameId] ?: return false
        if (players.isEmpty()) return false // Game is gone
        players.add(session)
        return true
    }

    // broadcast message to all players
    suspend fun broadcast(gameId: String, message: String) {
        val players = games[gameId] ?: return
        for (player in players) {
            try {
                player.send(Frame.Text(message))
            } catch (_: Exception) {
                // should do something here
            }
        }
    }

    // Removes a session and deletes the game if empty 
    fun removeSession(gameId: String, session: DefaultWebSocketServerSession) {
        val players = games[gameId] ?: return
        players.remove(session)
        if (players.isEmpty()) {
            games.remove(gameId)
        }
    }
}
package org.lords.server

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.http.*
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

object GameManager {
    // Map of gameId, list of active games
    private val games = ConcurrentHashMap<String, Game>()

    // create a new game and add the player (create an unique ID too)
    fun createGame(session: DefaultWebSocketServerSession, name : String): String {
        val gameId = UUID.randomUUID().toString()
        var player = Player(name = name, session = session)
        val game = Game(gameId, mutableListOf(player))
        games[gameId] = game
        return gameId
    }

    // add player to game 
    fun joinGame(gameId: String, session: DefaultWebSocketServerSession, name : String): Boolean {
        var player = Player(name = name, session = session)
        val game = games[gameId] ?: return false // if game[gameId] is null return false (not found or something)
        if (game.playerList.isEmpty() || game.started == true) return false // Game is gone
        game.playerList.add(player)
        return true
    }

    suspend fun receivePlay2(gameId: String, message: Frame) {
        val game = games[gameId] ?: return
        // only handle text/JSON frames
        val text = when (message) {
            is Frame.Text -> message.readText()
            else -> return  // ignore anything else but text/JSON
        }
        println("Received message: $text")
        for (player in game.playerList) {
            try {
                player.session.send(Frame.Text(text))
            } catch (e: Exception) {
                e.printStackTrace()
                // Optional: remove player if disconnected
            }
        }
    }

    suspend fun receivePlay(gameId: String, message: Frame) {
        val game = games[gameId] ?: return
        val text = when (message) {
            is Frame.Text -> message.readText()
            else -> return  // ignore anything else but text/JSON
        }
        game.addReceivedMessage(text)
    }


    // Removes a session and deletes the game if empty 
    fun removeSession(gameId: String, session: DefaultWebSocketServerSession) {
        val game = games[gameId] ?: return
        val player = game.playerList.find { it.session == session }
        if (player != null) {
            game.playerList.remove(player)
        }
        if (game.playerList.isEmpty()) {
            games.remove(gameId)
        }
    }
}
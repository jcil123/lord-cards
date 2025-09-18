package org.lords.server

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.http.*
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID
import kotlinx.serialization.json.Json
// import org.lords.game.Game
// import org.lords.game.Player

import org.lords.game.*

object GameManager {
    // map of gameId, list of active games
    private val games = ConcurrentHashMap<String, Game>()

    // create a new game and add the player (create an unique ID too)
    suspend fun createGame(session: DefaultWebSocketServerSession, name : String): String {
        val gameId = UUID.randomUUID().toString().substring(0, 5) // size of substring for game
        val playerId = UUID.randomUUID().toString().substring(0, 8)
        var player = Player(name = name, session = session, id = playerId)
        print(playerId + "\n")
        val game = Game(gameId, mutableListOf(player))
        games[gameId] = game
        // send the info to the player and then, to everyone
        val msg = CreateGameMessage(playerId,name,gameId,playerId)
        val chat = ChatMessage(playerId,name,"$name created game with id: $gameId")
        val response = Json.encodeToString<GameMessage>(msg)
        session.send(Frame.Text(response))
        receiveMessage(gameId,chat)
        return gameId
    }

    // add player to game 
    suspend fun joinGame(gameId: String, session: DefaultWebSocketServerSession, name : String): Boolean {
        val playerId = UUID.randomUUID().toString().substring(0, 8)
        var player = Player(name = name, session = session, id = playerId)
        print(playerId + "\n")
        val game = games[gameId] ?: return false // if game[gameId] is null return false (not found or something)
        if (game.playerList.isEmpty() || game.started == true) return false // Game is gone
        game.playerList.add(player)

        // TODO: ADD LOGIC WHEN JOINING GAME LIKE ABOVE

        return true
    }

    suspend fun receiveMessage(gameId: String, message: GameMessage) {
        val game = games[gameId] ?: return
        game.addReceivedMessage(message)
    }


    // removes a session and deletes the game if empty 
    fun removeSession(gameId: String, session: DefaultWebSocketServerSession) {
        val game = games[gameId] ?: return
        val player = game.playerList.find { it.session == session }
        if (player != null) {
            game.playerList.remove(player)
        }
        if (game.playerList.isEmpty()) {
            game.endGame() // stop the game when it is empty
            games.remove(gameId)
        }
    }
}
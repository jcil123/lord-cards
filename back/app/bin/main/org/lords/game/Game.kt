package org.lords.game

import io.ktor.websocket.*
import io.ktor.server.websocket.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class Game (
    val id : String, 
    var playerList : MutableList<Player>
) {
    private var currentTurn = 0
    public var started : Boolean = false
    private val incomingQueue = ConcurrentLinkedQueue<String>() // messages received
    private val outgoingQueue = ConcurrentLinkedQueue<String>() // messages sent
    private val voteSet = ConcurrentHashMap<String,Boolean>()

    private var turnJob: Job? = null


    fun startGame() {
        started = true
        // launch game loop
        turnJob = CoroutineScope(Dispatchers.Default).launch {
            currentTurn = 1
            while (isActive && playerList.isNotEmpty()) {
                // broadcast("Turn $currentTurn started! You have ${GameConstants.TURN_DURATION_SECS/1000} seconds.")
                // wait for players to make their moves
                delay(GameConstants.TURN_DURATION_SECS)
                // process all moves received during this turn
                processTurn() //TODO: implement turns
                // broadcast("Turn $currentTurn ended.")
                currentTurn++
            }
        }
    }

    fun endGame() {
        turnJob?.cancel()
        started = false
    }

    private suspend fun broadcast(message: GameMessage) {
        val msg = Json.encodeToString(message)
        for (player in playerList) {
            player.session.send(Frame.Text(msg))
        }
    }

    // private suspend fun registerStartVote(msg: GameMessage) {
    //     voteSet.put(msg.sender, true)
    //     if (voteSet.size >= (playerList.size)/2 && playerList.size >= GameConstants.MIN_PLAYERS) {
    //         startGame()
    //     } else {
    //         broadcast("Waiting for enough players to start ...")
    //     }
    // }

    private fun processTurn() {
        // TODO: pull moves from a queue, resolve them, update state
        println("Processing turn...")
    }

    suspend fun addReceivedMessage(message : GameMessage) {
        when (message) {
            // TODO: FIX THIS SHIT
            is CreateGameMessage -> broadcast(message)
            is VoteMessage -> broadcast(message)
            is JoinMessage -> broadcast(message)
            is ChatMessage -> broadcast(message)
            is PlayMessage -> broadcast(message)
            is RoleAbilityMessage -> broadcast(message)
        }
    }

}

package org.lords.server

import io.ktor.server.websocket.*
import java.util.concurrent.ConcurrentLinkedQueue


class Game (
    val id : String, 
    var playerList : MutableList<Player>
) {
    private var currentIndex = 0
    public var started : Boolean = false
    private val incomingQueue = ConcurrentLinkedQueue<String>() // messages received
    private val outgoingQueue = ConcurrentLinkedQueue<String>() // messages sent

    fun addReceivedMessage(message : String) {
        if (message == "start") {
            println("STARTED BY ")
        }
        incomingQueue.add(message)
    }

}
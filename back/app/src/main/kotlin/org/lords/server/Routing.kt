package org.lords.server 

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import kotlin.time.Duration.Companion.seconds
import org.slf4j.event.*

fun Application.configureRouting() {
    routing {
        get("/lords") {
            // loading page maybe?
            call.respondText("Hello Lords!")
        }

        webSocket("/lords/game") {
            // create a game
            println("Client connected to game")
            val id = GameManager.createGame(this)
            send(Frame.Text("Game created with id: $id"))
            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        GameManager.broadcast(id, "Player says: $text")
                    }
                }
            } finally {
                GameManager.removeSession(id, this)
            }
        }

        webSocket("/lords/game/{id}") {
            // find the game, error if it does not exist, redirect if it does exist
            val id = call.parameters["id"] ?: return@webSocket close()
            val success = GameManager.joinGame(id, this)
            if (success) {
                send("Joined game $id")
                try {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            GameManager.broadcast(id, "Player says: $text")
                        }
                    }
                } finally {
                    GameManager.removeSession(id, this)
                }
            } else {
                send(Frame.Text("Game $id not found"))
                close()
                return@webSocket
            }
        }
    }
}
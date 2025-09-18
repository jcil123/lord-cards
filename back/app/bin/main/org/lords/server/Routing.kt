package org.lords.server 

import io.ktor.http.*
import io.ktor.server.http.content.*
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

import kotlinx.serialization.json.Json

import org.lords.game.*

fun Application.configureRouting() {
    routing {
        
        staticResources("/", "static", index = "index.html") //serves everything, and index is in /lords

        webSocket("/lords/game") {
            // create a game
            val name = call.request.queryParameters["name"] ?: "Anonymous"
            val id = GameManager.createGame(this, name)
            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        val msg = Json.decodeFromString<GameMessage>(text)
                        GameManager.receiveMessage(id, msg)
                    }
                }
            } finally {
                GameManager.removeSession(id, this)
            }
        }

        webSocket("/lords/game/{id}") {
            // find the game, error if it does not exist, redirect if it does exist
            val id = call.parameters["id"] ?: return@webSocket close()
            val name = call.request.queryParameters["name"] ?: "Anonymous"
            val success = GameManager.joinGame(id, this, name)
            if (success) {
                // // TODO: REMOVE THIS TEST
                // GameManager.broadcastTest(id,(Frame.Text("$name joined game $id")))
                try {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            val msg = Json.decodeFromString<GameMessage>(text)
                            GameManager.receiveMessage(id, msg)
                        }
                    }
                } finally {
                    GameManager.removeSession(id, this)
                }
            } else {
                // game not found or full
                send(Frame.Text("Can not join game $id"))
                close()
                return@webSocket
            }
        }
    }
}
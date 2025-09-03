package org.lords

import io.ktor.server.engine.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.cio.CIO
import io.ktor.server.application.*
import org.lords.server.configureSockets
import org.lords.server.configureRouting


fun runBasicServer() {
    embeddedServer(CIO, port = 8080) {
        configureSockets()
        configureRouting()
    }.start(wait = true)
}

fun main() {
    runBasicServer()
}


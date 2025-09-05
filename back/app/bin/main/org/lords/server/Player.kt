package org.lords.server

import io.ktor.server.websocket.*

class Player (
    val name : String,
    var session : DefaultWebSocketServerSession,
    var moneyPoints : Int = 20,
    var religionPoints : Int = 20,
    var militaryPoints : Int = 20,
    var noblePoints : Int = 20,
    var peoplePoints : Int = 20
) {

}
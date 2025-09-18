package org.lords.game

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

@Serializable
sealed class GameMessage {
    abstract val sender: String
    abstract val senderName: String
}

@Serializable
@SerialName("Create")
data class CreateGameMessage(
    override val sender: String,
    override val senderName: String,
    val gameId: String,
    val playerId: String
) : GameMessage()

@Serializable
@SerialName("Join")
data class JoinMessage(
    override val sender: String,
    override val senderName: String,
    val playerId: String
) : GameMessage()

@Serializable
@SerialName("Vote")
data class VoteMessage(
    override val sender: String,
    override val senderName: String
) : GameMessage()

@Serializable
@SerialName("Chat")
data class ChatMessage(
    override val sender: String,
    override val senderName: String,
    val text: String
) : GameMessage()

@Serializable
@SerialName("Play")
data class PlayMessage(
    override val sender: String,
    override val senderName: String,
    val target: String,
    val description: String,
    val isSecret: Boolean
) : GameMessage()

@Serializable
@SerialName("Role")
data class RoleAbilityMessage(
    override val sender: String,
    override val senderName: String,
    val target: String,
    val description: String,
    val isSecret: Boolean
) : GameMessage()

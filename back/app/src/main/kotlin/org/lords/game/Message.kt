package org.lords.game

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

@Serializable
enum class MessageType {
    VoteStart,
    PlayCard,
    RoleAbility,
    Chat
}

@Serializable
abstract class GameMessage {
    abstract val type: MessageType
    abstract val sender: String
}

@Serializable
data class VoteMessage(
    override val type: MessageType = MessageType.VoteStart,
    override val sender: String,
) : GameMessage()

@Serializable
data class ChatMessage(
    override val type: MessageType = MessageType.Chat,
    override val sender: String,
    val text: String
) : GameMessage()

@Serializable
data class PlayMessage(
    override val type: MessageType = MessageType.PlayCard,
    override val sender: String,
    val target: String,
    val description: String,
    val isSecret: Boolean
) : GameMessage()

@Serializable
data class RoleAbilityMessage(
    override val type: MessageType = MessageType.RoleAbility,
    override val sender: String,
    val target: String,
    val description: String,
    val isSecret: Boolean
) : GameMessage()

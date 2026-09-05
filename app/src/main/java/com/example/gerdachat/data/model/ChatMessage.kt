package com.example.gerdachat.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "messages")
data class ChatMessage(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val contactId: String = "gerda",
    val sender: String = "user", // "user" or contact id
    val text: String = "",
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val audioDuration: String? = null,
    val videoUrl: String? = null,
    val timestamp: String = "",
    val isCallLog: Boolean = false,
    val callDuration: Int = 0,
    val isVideoCall: Boolean = false,
    val callStatus: String = "completed" // "completed" or "missed"
)

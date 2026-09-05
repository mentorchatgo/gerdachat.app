package com.example.gerdachat.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "calls")
data class CallRecord(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val contactId: String,
    val contactName: String,
    val contactPic: String,
    val timestamp: String,
    val durationSeconds: Int,
    val isVideo: Boolean,
    val isIncoming: Boolean,
    val status: String = "completed" // "completed" or "missed"
)

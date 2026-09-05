package com.example.gerdachat.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey
    val id: String,
    val name: String,
    val sysInstruct: String,
    val profilePic: String,
    val isCustom: Boolean = false,
    val phoneNumber: String = "06-12345678",
    val bio: String = "Hoi! Ik gebruik WhatsApp.",
    val voiceName: String = "Aoede",
    val voicePrompt: String = "Spreek heel vriendelijk en op een natuurlijke, rustige toon.",
    val lastMessage: String = "",
    val lastMessageTime: String = "",
    val unreadCount: Int = 0
) {
    companion object {
        const val GERDA_AVATAR = "https://i.imgur.com/e9o18Au.jpeg"
        const val GERDA_OVERLAY = "https://i.imgur.com/eOdHElW.gif"
        const val GERDA_VIDEO = "https://i.imgur.com/eCBZgoo.mp4"

        fun createDefaultGerda(): Contact {
            return Contact(
                id = "gerda",
                name = "Gerda B.",
                sysInstruct = "",
                profilePic = GERDA_AVATAR,
                isCustom = false,
                phoneNumber = "06-48291044",
                bio = "Hoi! Ik gebruik WhatsApp en ik zit in de Mekdonalts.",
                voiceName = "Despina",
                voicePrompt = "Praat traag, kinderachtig en een beetje dom",
                lastMessage = "effe lekker mekdonalts eten hoor",
                lastMessageTime = "12:30",
                unreadCount = 1
            )
        }
    }
}

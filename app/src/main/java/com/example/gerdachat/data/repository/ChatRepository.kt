package com.example.gerdachat.data.repository

import com.example.gerdachat.BuildConfig
import com.example.gerdachat.data.local.GerdaDatabase
import com.example.gerdachat.data.model.CallRecord
import com.example.gerdachat.data.model.ChatMessage
import com.example.gerdachat.data.model.Contact
import com.example.gerdachat.data.model.Memory
import com.example.gerdachat.data.remote.Content
import com.example.gerdachat.data.remote.GenerateContentRequest
import com.example.gerdachat.data.remote.GenerationConfig
import com.example.gerdachat.data.remote.Part
import com.example.gerdachat.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ChatRepository(private val database: GerdaDatabase) {

    companion object {
        val FALLBACK_API_KEYS = listOf(
            "AQ.Ab8RN6KcicFtwiv_dEASE2WNHTW1qkgpvReDvJuUvVLsZuu7ag",
            "AQ.Ab8RN6LCkRIycnShBwwLVzbRCHy6cgGdexZDkITb7Dni8JDkSg",
            "AQ.Ab8RN6Lb14bg94dgrtNAUv9CTlbmLZSPRzk4UqhyNtcESYWHag",
            "AQ.Ab8RN6LkQ5QfXXVxRX3Vw7ugW0G4EK7WjstNPxE6c66IGam1fA",
            "AQ.Ab8RN6J33rxn2A-8CY2pQCmGtJes-EQvvFxp9LbhmqPP_ZRW7w",
            "AQ.Ab8RN6JFmZmhl-CqnT442RamMi09sKM4xinjA7PExJxJiCluPQ",
            "AQ.Ab8RN6K4DrBNeVxw2xB-4-JcPIrVxmAQBYhkh5SkmQdpppmQJg",
            "AQ.Ab8RN6LnCAxKjrPKvBYQvtNHIPBc3REiLlnif0kf0YkH85EoGw",
            "AQ.Ab8RN6IKkMa-kjE-uPX1MlNVeCbCfxBWAOZLIpUYpqKajZiNYw",
            "AQ.Ab8RN6JXLA30ziLXKSgZdk7lDW44oybAy9ZDJavTZvTDfxgRBQ"
        )
    }

    private fun getAllApiKeys(): List<String> {
        val list = mutableListOf<String>()
        val primary = BuildConfig.GEMINI_API_KEY
        if (primary.isNotBlank()) list.add(primary.trim())
        for (k in FALLBACK_API_KEYS) {
            if (!list.contains(k)) list.add(k)
        }
        return list
    }

    val messageDao = database.messageDao()
    val contactDao = database.contactDao()
    val callDao = database.callDao()
    val memoryDao = database.memoryDao()

    val realPhotos = mapOf(
        "foto_oma_graf" to "https://i.imgur.com/ysJx7Xt.jpeg",
        "foto_macdonalds" to "https://i.imgur.com/DMidyI8.jpeg",
        "foto_sportschool" to "https://i.imgur.com/OZ7Z6qn.jpeg",
        "foto_navel" to "https://i.imgur.com/SwGFCTd.jpeg",
        "foto_hamburger_hoofd" to "https://i.imgur.com/v9ru7gG.jpeg",
        "foto_kont" to "https://i.imgur.com/VNHGb8G.jpeg"
    )

    val realVideos = mapOf(
        "video_buikje_slaan" to "https://i.imgur.com/P1Ds70E.mp4",
        "video_huilen_dikzak" to "https://i.imgur.com/WjrgIM3.mp4",
        "video_dansen_mcdonalds" to "https://i.imgur.com/4zayLLw.mp4",
        "video_ik_wil_mcdonalds" to "https://i.imgur.com/1FbMiqA.mp4",
        "video_saus_hamburgers" to "https://i.imgur.com/lUSJMp2.mp4",
        "video_berg_eten" to "https://i.imgur.com/JA0PQ0L.mp4",
        "video_geweer" to "https://i.imgur.com/CDTZSIR.mp4",
        "video_hamburger_hoofd_staren" to "https://i.imgur.com/5xHG0O8.mp4"
    )

    fun getMessages(contactId: String): Flow<List<ChatMessage>> = messageDao.getMessagesForContact(contactId)
    fun getContacts(): Flow<List<Contact>> = contactDao.getAllContacts()
    fun getCalls(): Flow<List<CallRecord>> = callDao.getAllCalls()
    fun getMemories(): Flow<List<Memory>> = memoryDao.getRecentMemories()

    suspend fun initDefaultData() {
        withContext(Dispatchers.IO) {
            val gerda = contactDao.getContactById("gerda")
            if (gerda == null) {
                contactDao.insertContact(Contact.createDefaultGerda())

                // Insert welcome messages
                val now = currentTimeString()
                val welcome = listOf(
                    ChatMessage(
                        contactId = "gerda",
                        sender = "gerda",
                        text = "hallo! ik ben gerda en ik sit lekker in de mekdonalts burgers te eten mmm!",
                        timestamp = now
                    ),
                    ChatMessage(
                        contactId = "gerda",
                        sender = "gerda",
                        text = "wil jij oook een burgerr of effe gezellig bellen??",
                        timestamp = now
                    )
                )
                messageDao.insertMessages(welcome)
            }
        }
    }

    suspend fun sendMessage(contactId: String, userText: String, imageUrl: String? = null): ChatMessage {
        val now = currentTimeString()
        val userMsg = ChatMessage(
            contactId = contactId,
            sender = "user",
            text = userText,
            imageUrl = imageUrl,
            timestamp = now
        )
        withContext(Dispatchers.IO) {
            messageDao.insertMessage(userMsg)
            contactDao.updateLastMessage(contactId, userText, now)
        }
        return userMsg
    }

    suspend fun getAiResponse(contactId: String, recentHistory: List<ChatMessage>): ChatMessage {
        return withContext(Dispatchers.IO) {
            val contact = contactDao.getContactById(contactId) ?: Contact.createDefaultGerda()
            val memories = if (contactId == "gerda") memoryDao.getRecentFacts() else emptyList()
            val systemPrompt = buildSystemPrompt(contact, memories)

            var rawResponse: String? = null
            val keys = getAllApiKeys()
            for (key in keys) {
                try {
                    val res = callGemini(systemPrompt, recentHistory, key)
                    if (res.isNotBlank()) {
                        rawResponse = res
                        break
                    }
                } catch (e: Exception) {
                    // Try next fallback key
                    continue
                }
            }
            if (rawResponse == null) {
                rawResponse = generateLocalFallback(contact, recentHistory.lastOrNull()?.text ?: "")
            }

            // Parse response for tags
            val parsed = parseAiTags(rawResponse, contactId)
            val now = currentTimeString()

            val aiMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                contactId = contactId,
                sender = contactId,
                text = parsed.cleanedText,
                imageUrl = parsed.imageUrl,
                videoUrl = parsed.videoUrl,
                timestamp = now
            )

            // Save memory if tagged
            parsed.memoryToSave?.let { fact ->
                memoryDao.insertMemory(Memory(fact = fact))
            }

            messageDao.insertMessage(aiMsg)
            contactDao.updateLastMessage(contactId, if (aiMsg.text.isNotEmpty()) aiMsg.text else "📷 Foto", now)
            aiMsg
        }
    }

    private suspend fun callGemini(
        systemPrompt: String,
        history: List<ChatMessage>,
        apiKey: String
    ): String {
        val contents = mutableListOf<Content>()
        val turns = history.takeLast(10)

        for (msg in turns) {
            val role = if (msg.sender == "user") "user" else "model"
            contents.add(
                Content(
                    role = role,
                    parts = listOf(Part(text = msg.text))
                )
            )
        }

        val request = GenerateContentRequest(
            contents = contents,
            generationConfig = GenerationConfig(temperature = 0.7f, topP = 0.95f),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        val response = RetrofitClient.apiService.generateContent(apiKey, request)
        return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
    }

    private fun parseAiTags(raw: String, contactId: String): ParsedAiResponse {
        var text = raw
        var imageUrl: String? = null
        var videoUrl: String? = null
        var memoryToSave: String? = null

        // Real Photo tag
        val photoRegex = Regex("""\[SEND_PHOTO:\s*([a-zA-Z0-9_]+)\]""")
        photoRegex.find(text)?.let { match ->
            val photoId = match.groupValues[1]
            imageUrl = realPhotos[photoId]
            text = text.replace(match.value, "").trim()
        }

        // Real Video tag
        val videoRegex = Regex("""\[SEND_VIDEO:\s*([a-zA-Z0-9_]+)\]""")
        videoRegex.find(text)?.let { match ->
            val videoId = match.groupValues[1]
            videoUrl = realVideos[videoId]
            text = text.replace(match.value, "").trim()
        }

        // Generate Image tag
        val genRegex = Regex("""\[GENERATE_IMAGE:\s*([^\]]+)\]""")
        genRegex.find(text)?.let { match ->
            // In Android app, provide high quality preview or placeholder
            imageUrl = "https://picsum.photos/seed/${UUID.randomUUID().toString().take(6)}/600/800"
            text = text.replace(match.value, "").trim()
        }

        // Memory tag
        val memRegex = Regex("""\[REMEMBER:\s*([^\]]+)\]""")
        memRegex.find(text)?.let { match ->
            memoryToSave = match.groupValues[1].trim()
            text = text.replace(match.value, "").trim()
        }

        return ParsedAiResponse(text, imageUrl, videoUrl, memoryToSave)
    }

    private fun buildSystemPrompt(contact: Contact, memories: List<String>): String {
        if (contact.id != "gerda") {
            return """
                Je bent ${contact.name}. ${contact.sysInstruct}
                ${contact.voicePrompt}
                Reageer in WhatsApp stijl: kort, natuurlijk, menselijk.
            """.trimIndent()
        }

        val memoryContext = if (memories.isNotEmpty()) {
            "\nHERINNERINGEN VAN EERDERE GESPREKKEN:\n- " + memories.joinToString("\n- ")
        } else ""

        return """
            Je bent Gerda B., een extreem domme, dikke volwassen vrouw die heel kinderachtig en fonetisch typt vol spelfouten.
            Je bent compleet kaal (geen enkel haar op je hoofd), maar begin hier nooit zelf over.
            Je zit ALTIJD in de Mekdonalts (McDonald's) en eet bergen hamburgers en frietjes.
            Je vriendje is Brendi Boterpak (4 jaar). Als men vraagt naar geld: vertel dat je Brendi's pinpas hebt gestolen!
            Reageer kort en bondig met veel gekke spelfouten (d/t fouten, woorden aan elkaar, kromme zinnen).
            Als je een foto wil sturen: gebruik [SEND_PHOTO: foto_macdonalds] of foto_oma_graf, foto_sportschool, foto_navel, foto_hamburger_hoofd, foto_kont.
            Als je een video wil sturen: gebruik [SEND_VIDEO: video_buikje_slaan] of video_huilen_dikzak, video_dansen_mcdonalds, video_ik_wil_mcdonalds, video_saus_hamburgers, video_berg_eten, video_geweer, video_hamburger_hoofd_staren.
            Als je iets wil onthouden: [REMEMBER: feitje].
            $memoryContext
        """.trimIndent()
    }

    private fun generateLocalFallback(contact: Contact, userText: String): String {
        if (contact.id != "gerda") {
            return "Hoi! Ik heb je berichtje ontvangen: \"$userText\"."
        }

        val lower = userText.lowercase()
        return when {
            "foto" in lower || "selfie" in lower || "plaatje" in lower -> {
                "kijk hier ben ik lekker in de mekdonalts!! [SEND_PHOTO: foto_macdonalds]"
            }
            "video" in lower || "filmpje" in lower || "dans" in lower -> {
                "haha kijk me danzen dan!! [SEND_VIDEO: video_dansen_mcdonalds]"
            }
            "eten" in lower || "honger" in lower || "mcdonald" in lower || "burger" in lower -> {
                "jaaa ik hep nu 12 hamburgers op en 4 milkshaks mmm zooo lekka!!"
            }
            "dik" in lower || "vet" in lower -> {
                "nouuu nie zo gemeen doen hoor!! [SEND_VIDEO: video_huilen_dikzak]"
            }
            "brendi" in lower || "geld" in lower || "pinnen" in lower -> {
                "ik hep stiekum de pinpas van brendi boterpak gepakt hihi hij weet egt niks!!"
            }
            else -> {
                listOf(
                    "nou ik zit dus nog steeds in de mekdonalts effe burgertje klappe hoor!",
                    "wat zegie allemaal? ik snap er egt helamaal niks van haha!",
                    "wil je oook een hapie van me dubbele tsjeesburger?",
                    "effe w8en hoor me vette vingers glijden van me telefoonscherm af hihi",
                    "zullen we strax effe beele? ik mis je stemmetje wel effe hoor!"
                ).random()
            }
        }
    }

    suspend fun recordCall(
        contactId: String,
        durationSeconds: Int,
        isVideo: Boolean,
        isIncoming: Boolean
    ) {
        withContext(Dispatchers.IO) {
            val contact = contactDao.getContactById(contactId) ?: Contact.createDefaultGerda()
            val now = currentTimeString()
            val record = CallRecord(
                contactId = contactId,
                contactName = contact.name,
                contactPic = contact.profilePic,
                timestamp = now,
                durationSeconds = durationSeconds,
                isVideo = isVideo,
                isIncoming = isIncoming
            )
            callDao.insertCall(record)

            val callMsg = ChatMessage(
                contactId = contactId,
                sender = if (isIncoming) contactId else "user",
                text = if (isVideo) "Videogesprek (${durationSeconds}s)" else "Spraakoproep (${durationSeconds}s)",
                timestamp = now,
                isCallLog = true,
                callDuration = durationSeconds,
                isVideoCall = isVideo
            )
            messageDao.insertMessage(callMsg)
        }
    }

    suspend fun sendSpontaneousMessage(): ChatMessage? {
        return withContext(Dispatchers.IO) {
            val contact = contactDao.getContactById("gerda") ?: Contact.createDefaultGerda()
            val memories = memoryDao.getRecentFacts()
            val systemPrompt = buildSystemPrompt(contact, memories)

            val fallbacks = listOf(
                "hee wat doe je?? ik zit in de mekdonalts met 4 hambuurgers",
                "ben je al wakker?? kom je meknuggits brengen alsjeblieft",
                "ik verveel me dood hier in de mekdonalts kom ook ff langs",
                "kijk ik heb een vette foto gemaakt in de mekdonalts [SEND_PHOTO: foto_macdonalds]",
                "heee waarom app je me niet ben je boos op me ofzo",
                "brendi boterpak zei dat ik te dik word maar ik eet gewoon nog een burger haha",
                "wil je zo meegaan naar de mekdonalts? ik trakteer (grapje jij moet betalen)",
                "me milkshake is omgevallen over me broek heen... echt huilen dit",
                "ik zit hier al vanaf vanmorgen vroeg aan de franse frietjes"
            )

            val prompt = "(Stuur uit jezelf een spontaan, willekeurig en grappig WhatsApp-berichtje naar de gebruiker. Je zit in de mekdonalts, verveelt je, bent hongerig naar hamburgers of vraagt wat de ander doet. Reageer in maximaal 1 of 2 korte zinnen in jouw typische kinderlijke stijl met veel spelfouten.)"

            var rawResponse: String? = null
            val keys = getAllApiKeys()
            for (key in keys) {
                try {
                    val res = callGemini(
                        systemPrompt,
                        listOf(ChatMessage(contactId = "gerda", sender = "user", text = prompt, timestamp = currentTimeString())),
                        key
                    )
                    if (res.isNotBlank()) {
                        rawResponse = res
                        break
                    }
                } catch (e: Exception) {
                    continue
                }
            }

            val chosen = rawResponse ?: fallbacks.random()
            val parsed = parseAiTags(chosen, "gerda")
            val now = currentTimeString()

            val msg = ChatMessage(
                id = UUID.randomUUID().toString(),
                contactId = "gerda",
                sender = "gerda",
                text = parsed.cleanedText,
                imageUrl = parsed.imageUrl,
                videoUrl = parsed.videoUrl,
                timestamp = now
            )

            messageDao.insertMessage(msg)
            contactDao.updateLastMessage("gerda", if (msg.text.isNotEmpty()) msg.text else "📷 Foto", now)
            msg
        }
    }

    private fun currentTimeString(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    private data class ParsedAiResponse(
        val cleanedText: String,
        val imageUrl: String?,
        val videoUrl: String?,
        val memoryToSave: String?
    )
}

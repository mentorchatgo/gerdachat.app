package com.example.gerdachat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gerdachat.data.model.Contact
import com.example.gerdachat.ui.screens.CallScreen
import com.example.gerdachat.ui.screens.CallsHistoryScreen
import com.example.gerdachat.ui.screens.ChatListScreen
import com.example.gerdachat.ui.screens.ChatScreen
import com.example.gerdachat.ui.screens.ContactProfileScreen
import com.example.gerdachat.ui.screens.SettingsScreen
import com.example.gerdachat.ui.theme.GerdaChatTheme
import com.example.gerdachat.ui.theme.WaBackgroundDark
import com.example.gerdachat.ui.theme.WaPanelDark
import com.example.gerdachat.ui.theme.WaTeal
import com.example.gerdachat.ui.theme.WaTextPrimary
import com.example.gerdachat.ui.theme.WaTextSecondary
import com.example.gerdachat.ui.viewmodel.ChatViewModel
import com.example.gerdachat.ui.viewmodel.ChatViewModelFactory
import java.util.UUID

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels {
        val app = application as GerdaChatApp
        ChatViewModelFactory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GerdaChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GerdaChatAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun GerdaChatAppContent(viewModel: ChatViewModel) {
    val contacts by viewModel.contacts.collectAsState()
    val selectedContact by viewModel.selectedContact.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    val realisticDelay by viewModel.realisticDelay.collectAsState()
    val activeCall by viewModel.activeCall.collectAsState()
    val calls by viewModel.calls.collectAsState()
    val memories by viewModel.memories.collectAsState()

    var currentScreen by remember { mutableStateOf("chats") } // "chats", "chat", "calls", "settings", "profile"
    var showNewContactDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            "chats" -> {
                ChatListScreen(
                    contacts = contacts,
                    onContactClick = { contact ->
                        viewModel.selectContact(contact)
                        currentScreen = "chat"
                    },
                    onNewContactClick = { showNewContactDialog = true },
                    onSettingsClick = { currentScreen = "settings" },
                    onCallsTabSelected = { currentScreen = "calls" }
                )
            }
            "chat" -> {
                selectedContact?.let { contact ->
                    ChatScreen(
                        contact = contact,
                        messages = messages,
                        isTyping = isTyping,
                        realisticDelay = realisticDelay,
                        onBackClick = { currentScreen = "chats" },
                        onVoiceCallClick = { viewModel.startCall(contact, isVideo = false) },
                        onVideoCallClick = { viewModel.startCall(contact, isVideo = true) },
                        onProfileClick = { currentScreen = "profile" },
                        onClearChatClick = { viewModel.clearChat(contact.id) },
                        onToggleDelayClick = { viewModel.toggleRealisticDelay() },
                        onSendMessage = { text -> viewModel.sendMessage(text) },
                        onSendPhotoPrompt = {
                            viewModel.sendMessage("stuur eens een foto!", null)
                        }
                    )
                } ?: run {
                    currentScreen = "chats"
                }
            }
            "calls" -> {
                CallsHistoryScreen(
                    calls = calls,
                    onBackClick = { currentScreen = "chats" },
                    onCallClick = { contactId, isVideo ->
                        val c = contacts.find { it.id == contactId } ?: Contact.createDefaultGerda()
                        viewModel.startCall(c, isVideo)
                    }
                )
            }
            "settings" -> {
                SettingsScreen(
                    realisticDelay = realisticDelay,
                    onToggleDelay = { viewModel.toggleRealisticDelay() },
                    memories = memories,
                    onClearMemories = { viewModel.clearMemories() },
                    onBackClick = { currentScreen = "chats" }
                )
            }
            "profile" -> {
                selectedContact?.let { contact ->
                    ContactProfileScreen(
                        contact = contact,
                        onBackClick = { currentScreen = "chat" },
                        onVoiceCallClick = { viewModel.startCall(contact, isVideo = false) },
                        onVideoCallClick = { viewModel.startCall(contact, isVideo = true) },
                        onSaveContact = { updated -> viewModel.saveContact(updated) },
                        onDeleteContact = { toDelete ->
                            viewModel.deleteContact(toDelete)
                            currentScreen = "chats"
                        }
                    )
                } ?: run {
                    currentScreen = "chats"
                }
            }
        }

        // Active Call Overlay
        activeCall?.let { callState ->
            CallScreen(
                callState = callState,
                onEndCall = { viewModel.endCall() }
            )
        }

        // New AI Contact Dialog
        if (showNewContactDialog) {
            NewContactDialog(
                onDismiss = { showNewContactDialog = false },
                onConfirm = { name, prompt, bio, phone ->
                    val newContact = Contact(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        sysInstruct = prompt,
                        profilePic = "https://picsum.photos/seed/${UUID.randomUUID().toString().take(5)}/200/200",
                        isCustom = true,
                        phoneNumber = phone,
                        bio = bio,
                        lastMessage = "Hoi! Ik ben nieuw op WhatsApp.",
                        lastMessageTime = "Nu"
                    )
                    viewModel.saveContact(newContact)
                    showNewContactDialog = false
                    viewModel.selectContact(newContact)
                    currentScreen = "chat"
                }
            )
        }
    }
}

@Composable
fun NewContactDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, prompt: String, bio: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("06-${(10000000..99999999).random()}") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WaPanelDark,
        title = {
            Text(
                text = "Nieuw AI Persona",
                color = WaTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Naam (bijv. Henk)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WaTextPrimary,
                        unfocusedTextColor = WaTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Status / Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WaTextPrimary,
                        unfocusedTextColor = WaTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("AI Persona Instructies") },
                    placeholder = { Text("Hoe moet dit contact zich gedragen?") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WaTextPrimary,
                        unfocusedTextColor = WaTextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, prompt, bio.ifEmpty { "Beschikbaar" }, phone)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WaTeal)
            ) {
                Text("Toevoegen", color = WaBackgroundDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuleren", color = WaTextSecondary)
            }
        }
    )
}

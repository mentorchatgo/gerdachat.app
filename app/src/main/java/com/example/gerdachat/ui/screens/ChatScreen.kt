package com.example.gerdachat.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.gerdachat.data.model.ChatMessage
import com.example.gerdachat.data.model.Contact
import com.example.gerdachat.ui.theme.WaBackgroundDark
import com.example.gerdachat.ui.theme.WaBlueTicks
import com.example.gerdachat.ui.theme.WaIncomingBubble
import com.example.gerdachat.ui.theme.WaOutgoingBubble
import com.example.gerdachat.ui.theme.WaPanelDark
import com.example.gerdachat.ui.theme.WaTeal
import com.example.gerdachat.ui.theme.WaTextPrimary
import com.example.gerdachat.ui.theme.WaTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    contact: Contact,
    messages: List<ChatMessage>,
    isTyping: Boolean,
    realisticDelay: Boolean,
    onBackClick: () -> Unit,
    onVoiceCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onProfileClick: () -> Unit,
    onClearChatClick: () -> Unit,
    onToggleDelayClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    onSendPhotoPrompt: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Terug",
                            tint = WaTextPrimary
                        )
                    }
                },
                title = {
                    Row(
                        modifier = Modifier
                            .clickable(onClick = onProfileClick)
                            .testTag("chat_header_profile"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = contact.profilePic,
                            contentDescription = contact.name,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(WaPanelDark),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = contact.name,
                                color = WaTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (isTyping) "aan het typen…" else "online",
                                color = if (isTyping) WaTeal else WaTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onVideoCallClick,
                        modifier = Modifier.testTag("video_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Videogesprek",
                            tint = WaTextPrimary
                        )
                    }
                    IconButton(
                        onClick = onVoiceCallClick,
                        modifier = Modifier.testTag("voice_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Spraakoproep",
                            tint = WaTextPrimary
                        )
                    }
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("chat_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opties",
                            tint = WaTextPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(WaPanelDark)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Contact weergeven", color = WaTextPrimary) },
                            onClick = {
                                menuExpanded = false
                                onProfileClick()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (realisticDelay) "Realistische vertraging: AAN" else "Realistische vertraging: UIT",
                                    color = WaTextPrimary
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onToggleDelayClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Chat wissen", color = WaTextPrimary) },
                            onClick = {
                                menuExpanded = false
                                onClearChatClick()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WaPanelDark)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WaBackgroundDark)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("message_input_field"),
                    placeholder = {
                        Text(
                            text = "Typ een bericht",
                            color = WaTextSecondary,
                            fontSize = 15.sp
                        )
                    },
                    leadingIcon = {
                        IconButton(onClick = { /* Emoji */ }) {
                            Icon(
                                imageVector = Icons.Default.SentimentSatisfiedAlt,
                                contentDescription = "Emoji",
                                tint = WaTextSecondary
                            )
                        }
                    },
                    trailingIcon = {
                        Row {
                            IconButton(onClick = onSendPhotoPrompt) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = "Bijlage",
                                    tint = WaTextSecondary
                                )
                            }
                            IconButton(onClick = onSendPhotoPrompt) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Camera",
                                    tint = WaTextSecondary
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = WaPanelDark,
                        unfocusedContainerColor = WaPanelDark,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = WaTextPrimary,
                        unfocusedTextColor = WaTextPrimary
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(WaTeal)
                        .clickable {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        }
                        .testTag("send_message_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (inputText.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                        contentDescription = "Verzenden",
                        tint = WaBackgroundDark
                    )
                }
            }
        },
        containerColor = WaBackgroundDark
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            items(messages, key = { it.id }) { msg ->
                MessageBubble(message = msg)
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (isTyping) {
                item {
                    TypingBubble()
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.sender == "user"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 12.dp
                    )
                )
                .background(if (isUser) WaOutgoingBubble else WaIncomingBubble)
                .padding(8.dp)
                .testTag("message_bubble_${message.id}")
        ) {
            // Photo if present
            if (!message.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = message.imageUrl,
                    contentDescription = "Foto bijlage",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .padding(bottom = 6.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Video preview if present
            if (!message.videoUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                        .padding(bottom = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Video afspelen",
                        tint = Color.White,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            // Audio player preview if present
            if (!message.audioUrl.isNullOrEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Audio afspelen",
                        tint = WaTeal,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Spraakbericht (${message.audioDuration ?: "0:12"})",
                        color = WaTextPrimary,
                        fontSize = 14.sp
                    )
                }
            }

            // Text message
            if (message.text.isNotEmpty()) {
                Text(
                    text = message.text,
                    color = WaTextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
            }

            // Timestamp and checkmarks
            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.timestamp.ifEmpty { "12:00" },
                    color = WaTextSecondary,
                    fontSize = 11.sp
                )
                if (isUser) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "Gelezen",
                        tint = WaBlueTicks,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TypingBubble() {
    val transition = rememberInfiniteTransition(label = "dots")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(WaIncomingBubble)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("typing_indicator")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(WaTextSecondary.copy(alpha = alpha))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(WaTextSecondary.copy(alpha = (alpha + 0.3f).coerceAtMost(1f)))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(WaTextSecondary.copy(alpha = (alpha + 0.6f).coerceAtMost(1f)))
                )
            }
        }
    }
}

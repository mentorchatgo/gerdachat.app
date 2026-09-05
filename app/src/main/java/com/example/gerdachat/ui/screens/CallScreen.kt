package com.example.gerdachat.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.SwitchCamera
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.gerdachat.data.model.Contact
import com.example.gerdachat.ui.theme.WaDanger
import com.example.gerdachat.ui.theme.WaPanelDark
import com.example.gerdachat.ui.theme.WaTextPrimary
import com.example.gerdachat.ui.theme.WaTextSecondary
import com.example.gerdachat.ui.viewmodel.ActiveCallState

@Composable
fun CallScreen(
    callState: ActiveCallState,
    onEndCall: () -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }
    var isSpeaker by remember { mutableStateOf(true) }

    val contact = callState.contact
    val isVideo = callState.isVideo
    val duration = callState.durationSeconds
    val isConnected = callState.isConnected

    val durationText = if (isConnected) {
        val minutes = duration / 60
        val seconds = duration % 60
        String.format("%02d:%02d", minutes, seconds)
    } else {
        "Rinkelt…"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1B21))
            .testTag("call_screen_container")
    ) {
        // Video background if video call and connected
        if (isVideo && isConnected) {
            AsyncImage(
                model = if (contact.id == "gerda") Contact.GERDA_OVERLAY else contact.profilePic,
                contentDescription = "Videogesprek overlay",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Dark gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )
        }

        // Header info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = contact.name,
                color = WaTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = durationText,
                color = WaTextSecondary,
                fontSize = 16.sp
            )

            if (!isVideo || !isConnected) {
                Spacer(modifier = Modifier.height(48.dp))
                AsyncImage(
                    model = contact.profilePic,
                    contentDescription = contact.name,
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(WaPanelDark),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 54.dp, start = 32.dp, end = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute Button
            IconButton(
                onClick = { isMuted = !isMuted },
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (isMuted) Color.White else WaPanelDark)
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Microfoon dempen",
                    tint = if (isMuted) Color.Black else WaTextPrimary
                )
            }

            // End Call Button
            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(66.dp)
                    .clip(CircleShape)
                    .background(WaDanger)
                    .testTag("end_call_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "Ophangen",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Speaker / Switch Camera
            IconButton(
                onClick = { isSpeaker = !isSpeaker },
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (isSpeaker) Color.White else WaPanelDark)
            ) {
                Icon(
                    imageVector = if (isVideo) Icons.Default.SwitchCamera else Icons.Default.VolumeUp,
                    contentDescription = "Luidspreker",
                    tint = if (isSpeaker) Color.Black else WaTextPrimary
                )
            }
        }
    }
}

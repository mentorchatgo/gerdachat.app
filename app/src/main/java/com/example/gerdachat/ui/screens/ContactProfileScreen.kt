package com.example.gerdachat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.gerdachat.data.model.Contact
import com.example.gerdachat.ui.theme.WaBackgroundDark
import com.example.gerdachat.ui.theme.WaDanger
import com.example.gerdachat.ui.theme.WaPanelDark
import com.example.gerdachat.ui.theme.WaTeal
import com.example.gerdachat.ui.theme.WaTextPrimary
import com.example.gerdachat.ui.theme.WaTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactProfileScreen(
    contact: Contact,
    onBackClick: () -> Unit,
    onVoiceCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onSaveContact: (Contact) -> Unit,
    onDeleteContact: (Contact) -> Unit
) {
    var name by remember { mutableStateOf(contact.name) }
    var bio by remember { mutableStateOf(contact.bio) }
    var sysInstruct by remember { mutableStateOf(contact.sysInstruct) }
    var phone by remember { mutableStateOf(contact.phoneNumber) }

    val isEditingAllowed = contact.isCustom

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Terug",
                            tint = WaTextPrimary
                        )
                    }
                },
                title = { Text("Contactgegevens", color = WaTextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WaPanelDark)
            )
        },
        containerColor = WaBackgroundDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image and Action Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WaPanelDark)
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = contact.profilePic,
                    contentDescription = contact.name,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(WaBackgroundDark),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = name,
                    color = WaTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                Text(
                    text = phone,
                    color = WaTextSecondary,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        IconButton(
                            onClick = onVoiceCallClick,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(WaBackgroundDark)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Bellen",
                                tint = WaTeal
                            )
                        }
                        Text("Bellen", color = WaTeal, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        IconButton(
                            onClick = onVideoCallClick,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(WaBackgroundDark)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video",
                                tint = WaTeal
                            )
                        }
                        Text("Video", color = WaTeal, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bio and Details
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = WaPanelDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Info en telefoonnummer",
                        color = WaTeal,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isEditingAllowed) {
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Status/Bio") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = WaTextPrimary,
                                unfocusedTextColor = WaTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = sysInstruct,
                            onValueChange = { sysInstruct = it },
                            label = { Text("AI Systeem Prompt") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = WaTextPrimary,
                                unfocusedTextColor = WaTextPrimary
                            )
                        )
                    } else {
                        Text(
                            text = bio,
                            color = WaTextPrimary,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Encryption info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = WaPanelDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = WaTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Versleuteling",
                            color = WaTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Berichten en oproepen zijn end-to-end versleuteld.",
                            color = WaTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            if (isEditingAllowed) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        onSaveContact(
                            contact.copy(
                                name = name,
                                bio = bio,
                                sysInstruct = sysInstruct,
                                phoneNumber = phone
                            )
                        )
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaTeal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Wijzigingen Opslaan", color = WaBackgroundDark, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onDeleteContact(contact)
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaDanger),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Contact Verwijderen", color = WaTextPrimary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

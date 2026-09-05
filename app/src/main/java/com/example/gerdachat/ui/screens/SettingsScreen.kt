package com.example.gerdachat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gerdachat.BuildConfig
import com.example.gerdachat.data.model.Memory
import com.example.gerdachat.ui.theme.WaBackgroundDark
import com.example.gerdachat.ui.theme.WaDanger
import com.example.gerdachat.ui.theme.WaDivider
import com.example.gerdachat.ui.theme.WaPanelDark
import com.example.gerdachat.ui.theme.WaTeal
import com.example.gerdachat.ui.theme.WaTextPrimary
import com.example.gerdachat.ui.theme.WaTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    realisticDelay: Boolean,
    onToggleDelay: () -> Unit,
    memories: List<Memory>,
    onClearMemories: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Terug",
                            tint = WaTextPrimary
                        )
                    }
                },
                title = {
                    Text(
                        text = "Instellingen",
                        color = WaTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WaPanelDark)
            )
        },
        containerColor = WaBackgroundDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Delay Setting
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WaPanelDark, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = WaTeal,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Realistische Typvertraging",
                            color = WaTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Gerda typt op menselijke snelheid met pauzes",
                            color = WaTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                    Switch(
                        checked = realisticDelay,
                        onCheckedChange = { onToggleDelay() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = WaBackgroundDark,
                            checkedTrackColor = WaTeal
                        ),
                        modifier = Modifier.testTag("switch_realistic_delay")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Spontaneous Messages Setting
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WaPanelDark, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = WaTeal,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Spontane Berichten (09:00 - 20:00)",
                            color = WaTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Gerda stuurt willekeurig ~3x per dag zelfstandig berichten met WhatsApp-meldingen",
                            color = WaTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // API Key Status
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WaPanelDark, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = WaTeal,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gemini API (11 Fallback Sleutels)",
                            color = WaTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Actief • 11 API sleutels geconfigureerd voor Chat, Beeld & Live Bellen met automatische fallback",
                            color = WaTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Memories Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = WaTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gerda's Geheugen (Herinneringen)",
                            color = WaTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    if (memories.isNotEmpty()) {
                        IconButton(onClick = onClearMemories) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Wis geheugen",
                                tint = WaDanger
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (memories.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = WaPanelDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Gerda heeft nog geen speciale herinneringen opgeslagen. Vertel haar iets in de chat!",
                            color = WaTextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(memories) { mem ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = WaPanelDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = WaTeal,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = mem.fact,
                                color = WaTextPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

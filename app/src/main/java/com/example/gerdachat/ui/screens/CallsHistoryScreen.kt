package com.example.gerdachat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.gerdachat.data.model.CallRecord
import com.example.gerdachat.ui.theme.WaBackgroundDark
import com.example.gerdachat.ui.theme.WaDanger
import com.example.gerdachat.ui.theme.WaDivider
import com.example.gerdachat.ui.theme.WaPanelDark
import com.example.gerdachat.ui.theme.WaTeal
import com.example.gerdachat.ui.theme.WaTextPrimary
import com.example.gerdachat.ui.theme.WaTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsHistoryScreen(
    calls: List<CallRecord>,
    onBackClick: () -> Unit,
    onCallClick: (String, Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Terug",
                            tint = WaTextPrimary
                        )
                    }
                },
                title = {
                    Text(
                        text = "Oproepen",
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
        if (calls.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(64.dp))
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = WaTextSecondary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Nog geen recente oproepen",
                    color = WaTextSecondary,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("calls_list")
            ) {
                items(calls, key = { it.id }) { call ->
                    CallItem(call = call, onCallClick = { onCallClick(call.contactId, call.isVideo) })
                    HorizontalDivider(color = WaDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 76.dp))
                }
            }
        }
    }
}

@Composable
fun CallItem(
    call: CallRecord,
    onCallClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCallClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("call_item_${call.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = call.contactPic,
            contentDescription = call.contactName,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(WaPanelDark),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = call.contactName,
                color = if (call.status == "missed") WaDanger else WaTextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (call.isIncoming) Icons.AutoMirrored.Filled.CallReceived else Icons.AutoMirrored.Filled.CallMade,
                    contentDescription = null,
                    tint = if (call.status == "missed") WaDanger else WaTeal,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${call.timestamp} (${call.durationSeconds}s)",
                    color = WaTextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        IconButton(onClick = onCallClick) {
            Icon(
                imageVector = if (call.isVideo) Icons.Default.Videocam else Icons.Default.Call,
                contentDescription = "Bellen",
                tint = WaTeal
            )
        }
    }
}

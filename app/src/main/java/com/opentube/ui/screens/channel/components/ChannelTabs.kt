package com.opentube.ui.screens.channel.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class ChannelTab {
    VIDEOS,
    PLAYLISTS,
    ABOUT
}

@Composable
fun ChannelTabs(
    selectedTab: ChannelTab,
    onTabSelected: (ChannelTab) -> Unit,
    modifier: Modifier = Modifier
) {
    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        modifier = modifier.fillMaxWidth()
    ) {
        Tab(
            selected = selectedTab == ChannelTab.VIDEOS,
            onClick = { onTabSelected(ChannelTab.VIDEOS) },
            text = { Text("Videos") }
        )
        
        Tab(
            selected = selectedTab == ChannelTab.PLAYLISTS,
            onClick = { onTabSelected(ChannelTab.PLAYLISTS) },
            text = { Text("Playlists") }
        )
        
        Tab(
            selected = selectedTab == ChannelTab.ABOUT,
            onClick = { onTabSelected(ChannelTab.ABOUT) },
            text = { Text("Acerca de") }
        )
    }
}

@Composable
fun AboutTab(
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Descripción",
            style = MaterialTheme.typography.titleMedium
        )
        
        Text(
            text = description.ifEmpty { "Sin descripción" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

package com.notifiy.interplanetary.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.*
import coil.compose.AsyncImage
import com.notifiy.interplanetary.data.model.Post
import com.notifiy.interplanetary.data.model.ItvPurchase
import com.notifiy.interplanetary.ui.components.MovieCard
import com.notifiy.interplanetary.ui.theme.Background
import com.notifiy.interplanetary.ui.theme.Blue
import com.notifiy.interplanetary.ui.viewmodel.ProfileViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Edit

@Composable
fun ProfileScreen(
    onLogoutConfirm: () -> Unit,
    onMovieClick: (Post) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf("Watchlist") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val tabs = listOf("Watchlist", "Playlist", "Liked", "Purchases", "Contact Us", "Logout")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Profile Card
        Card(
            onClick = { /* Account details */ },
            border = BorderStroke(1.dp, Color(0xFF2A2A3A)),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF111116),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2A3A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFF888899)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.userName.ifBlank { "User Profile" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = uiState.userEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFAAAAAA)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (uiState.activePlan != null) Color(0xFF00FF88) else Color.Gray, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.activePlan != null) "Plan: ${uiState.activePlan}" else "No active plan",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.activePlan != null) Color(0xFF00FF88) else Color.Gray
                        )
                    }
                }

                IconButton(onClick = { /* Edit Profile */ }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = Blue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs - using a Scrollable Row for mobile
        ScrollableTabRow(
            selectedTabIndex = tabs.indexOf(selectedTab),
            containerColor = Color.Transparent,
            contentColor = Blue,
            edgePadding = 0.dp,
            divider = {}
        ) {
            tabs.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = {
                        if (tab == "Logout") {
                            showLogoutDialog = true
                        } else {
                            selectedTab = tab
                        }
                    },
                    text = {
                        Text(
                            text = tab,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Content Area
        Box(modifier = Modifier.fillMaxWidth()) {
            when (selectedTab) {
                "Purchases" -> PurchasesContent(uiState.purchases, uiState.activePlan)
                "Contact Us" -> ContactUsContent()
                else -> CategorizedContent(
                    items = when (selectedTab) {
                        "Playlist" -> uiState.playlist
                        "Liked" -> uiState.liked
                        "Watchlist" -> uiState.watchlist
                        else -> emptyMap()
                    },
                    tabName = selectedTab,
                    onMovieClick = onMovieClick
                )
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutConfirm()
                    }
                ) {
                    Text("Logout", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Logout Confirmation") },
            text = { Text("Are you sure you want to log out?") },
            containerColor = Color(0xFF1A1A1A),
            titleContentColor = Color.White,
            textContentColor = Color.Gray
        )
    }
}

@Composable
fun CategorizedContent(
    items: Map<String, List<Post>>,
    tabName: String,
    onMovieClick: (Post) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Movie") }
    val categories = listOf("Movie", "Video", "Episode")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                TextButton(
                    onClick = { selectedCategory = category },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isSelected) Blue else Color.Gray
                    )
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val displayCategory = when(selectedCategory) {
            "Movie" -> "Movies"
            "Video" -> "Videos"
            "Episode" -> "TV Shows"
            else -> "Movies"
        }
        val currentItems = items[displayCategory] ?: emptyList()

        if (currentItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No ${tabName.lowercase()} in $displayCategory.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            LazyRow (
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(currentItems) { post ->
                    MovieCard(
                        post = post,
                        onClick = { onMovieClick(post) },
                        width = 200.dp,
                        aspectRatio = 1.77f
                    )
                }
            }
        }
    }
}

@Composable
fun PurchasesContent(purchases: List<ItvPurchase>, activePlan: String?) {
    Column {
        Text(
            "Transaction History",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (purchases.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Text("No purchases found.", color = Color.Gray)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                purchases.forEach { purchase ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF151515),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF222222), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("€", color = Blue, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(purchase.plan_name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text(purchase.purchase_date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "${purchase.currency} ${purchase.amount}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (purchase.plan_name == activePlan && purchase.status == "Success") {
                                    Text(
                                        "Active",
                                        color = Color(0xFF00FF88),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactUsContent() {
    Column {
        Text(
            "Contact Us",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            "How can we help you?",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFAAAAAA),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Email Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF161622),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✉️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Email Us", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("info@interplanetary.tv", style = MaterialTheme.typography.bodySmall, color = Blue)
                    }
                }
            }

            // Phone Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF161622),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📞", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Call Us", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("+37123112488", style = MaterialTheme.typography.bodySmall, color = Color(0xFF00FF88))
                    }
                }
            }
        }
    }
}

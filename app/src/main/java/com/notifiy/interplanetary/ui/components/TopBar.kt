package com.notifiy.interplanetary.ui.components

import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import com.notifiy.interplanetary.R

@Composable
fun TopBar(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    onSearchClick: () -> Unit,
    onLoginClick: () -> Unit,
    onSubscribeClick: () -> Unit,
    onAllVideosClick: () -> Unit,
    isDropdownOpen: Boolean,
    isLoggedIn: Boolean,
    activePlan: String?,
    onProfileClick: () -> Unit = {}
)
{
    val tabs = listOf("Home", "News", "All", "Plans & Advertise")
    val dropdownItems = listOf("TV Shows", "Movies", "News Videos", "Videos", "Documentary Films", "Documentary Series", "Science-Fiction")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    )
    {
        // Left Section: Logo
        AsyncImage(
            model = R.drawable.logo,
            contentDescription = "Logo",
            modifier = Modifier
                .width(100.dp)
                .height(30.dp),
            contentScale = ContentScale.Fit
        )

        // Center Section: Navigation (Using a simplified version for mobile)
        // In a real mobile app, this might be a Drawer or BottomNav, but keeping TopBar logic as requested
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(onClick = onSearchClick) {
                Text("🔍", style = MaterialTheme.typography.titleMedium)
            }

            if (activePlan == null) {
                TextButton(
                    onClick = onSubscribeClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFFD700))
                ) {
                    Text("Subscribe", fontWeight = FontWeight.Bold)
                }
            }

            TextButton(
                onClick = if (isLoggedIn) onProfileClick else onLoginClick,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
            ) {
                Text(if (isLoggedIn) "Profile" else "Login")
            }
        }
    }
}

@Composable
fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (isSelected) Color.White else Color.Gray
        )
    )
    {
        Text(
            text = text,
            style = if (isSelected) MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold) 
                    else MaterialTheme.typography.labelSmall
        )
    }
}


package com.notifiy.interplanetary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.notifiy.interplanetary.data.model.Post
import com.notifiy.interplanetary.ui.theme.Gold
import com.notifiy.interplanetary.ui.theme.TextPrimary
import com.notifiy.interplanetary.ui.theme.TextSecondary

@Composable
fun MovieCard(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 220.dp,
    aspectRatio: Float = 1.77f // Landscape for rectangle look
) {
    val imageUrl = post.getDisplayImageUrl()

    Column(
        modifier = modifier
            .width(width)
            .clickable { onClick() }
    ) {
        Card(
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B21)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = post.title.rendered,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient Overlay at bottom for title if needed, but we put title outside
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                                startY = 300f
                            )
                        )
                )

                // Premium Badge
                val isPremium = post.membershipLevel.isNotEmpty() && 
                                !post.membershipLevel.any { it.contains("free", ignoreCase = true) }
                
                if (isPremium) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(0.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "👑",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = post.title.rendered.replace(Regex("<[^>]*>"), "").trim(),
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )
        
        Text(
            text = if(post.category == "movie") "Movie" else "Series",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )
    }
}

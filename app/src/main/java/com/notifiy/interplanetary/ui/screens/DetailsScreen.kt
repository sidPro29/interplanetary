package com.notifiy.interplanetary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Check
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.notifiy.interplanetary.data.model.Post
import com.notifiy.interplanetary.ui.components.MovieCard
import com.notifiy.interplanetary.ui.theme.*
import com.notifiy.interplanetary.ui.viewmodel.DetailsViewModel

@Composable
fun DetailsScreen(
    id: Int,
    title: String,
    description: String,
    imageUrl: String,
    isVideoAvailable: Boolean,
    isLoggedIn: Boolean,
    onLoginRequired: () -> Unit,
    onPlayClick: () -> Unit,
    onSubscribeClick: () -> Unit,
    onMovieClick: (Post) -> Unit,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val post by viewModel.post.collectAsState()
    val isInWatchlist by viewModel.isInWatchlist.collectAsState()
    val isLiked by viewModel.isLiked.collectAsState()
    val recommendedMovies by viewModel.recommendedMovies.collectAsState()
    val context = LocalContext.current
    val postTags by viewModel.postTags.collectAsState()

    LaunchedEffect(id) {
        viewModel.loadDetails(id)
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // Header Image with Gradient
            item {
                Box(modifier = Modifier.fillMaxWidth().height(450.dp)) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Background.copy(alpha = 0.5f), Background)
                                )
                            )
                    )
                }
            }

            // Title and Info
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("2024", color = TextSecondary, fontSize = 14.sp)
                        Text(" • ", color = TextSecondary)
                        Text("18+", color = Color.Gray, modifier = Modifier.background(Color(0xFF333333), RoundedCornerShape(2.dp)).padding(horizontal = 4.dp), fontSize = 12.sp)
                        Text(" • ", color = TextSecondary)
                        Text(postTags.ifEmpty { "Sci-Fi • Epic" }, color = TextSecondary, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Primary Action Button
                    if (isVideoAvailable) {
                        Button(
                            onClick = {
                                if (viewModel.canWatch()) {
                                    onPlayClick()
                                } else {
                                    // Premium content: Check login before showing plans
                                    if (!isLoggedIn) {
                                        onLoginRequired()
                                    } else {
                                        onSubscribeClick()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewModel.canWatch()) Color.White else Primary,
                                contentColor = if (viewModel.canWatch()) Color.Black else Color.White
                            )
                        ) {
                            Icon(
                                imageVector = if (viewModel.canWatch()) Icons.Default.PlayArrow else Icons.Default.Favorite,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (viewModel.canWatch()) "Play" else "Subscribe to Watch",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Secondary Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionButton(
                            icon = if (isInWatchlist) Icons.Default.Check else Icons.Default.Add,
                            label = "My List",
                            onClick = { 
                                viewModel.toggleWatchlist(id)
                                android.widget.Toast.makeText(context, if (!isInWatchlist) "Added to List" else "Removed from List", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                        ActionButton(
                            icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            label = "Rate",
                            onClick = { 
                                viewModel.toggleLiked(id)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Description
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        lineHeight = 22.sp
                    )
                }
            }

            // More Like This
            item {
                if (recommendedMovies.isNotEmpty()) {
                    Column(modifier = Modifier.padding(top = 32.dp)) {
                        Text(
                            text = "More Like This",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp)
                        ) {
                            items(recommendedMovies) { relatedPost ->
                                MovieCard(
                                    post = relatedPost,
                                    onClick = { onMovieClick(relatedPost) },
                                    width = 200.dp,
                                    aspectRatio = 1.77f
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, color = TextSecondary, fontSize = 12.sp)
    }
}

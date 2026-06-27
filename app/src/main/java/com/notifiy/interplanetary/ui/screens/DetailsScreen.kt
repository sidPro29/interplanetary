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

import androidx.compose.ui.platform.LocalLifecycleOwner
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import com.notifiy.interplanetary.data.util.VideoUrlManager

@Composable
fun DetailsScreen(
    id: Int,
    title: String,
    description: String,
    imageUrl: String,
    isVideoAvailable: Boolean,
    isLoggedIn: Boolean,
    onLoginRequired: () -> Unit,
    onPlayClick: (String) -> Unit,
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

    val lifecycleOwner = LocalLifecycleOwner.current
    var isVideoResolving by remember { mutableStateOf(false) }
    var isVideoReady by remember { mutableStateOf(false) }
    var backgroundVideoUrl by remember { mutableStateOf("") }

    LaunchedEffect(post) {
        val currentPost = post ?: return@LaunchedEffect
        isVideoResolving = true
        isVideoReady = false
        backgroundVideoUrl = ""
        try {
            var clipId: String? = null
            var directUrl: String? = null

            val type = currentPost.type.lowercase()
            if (type == "movie" || type == "movies") {
                clipId = currentPost.trailer?.get("clipId") as? String
                if (clipId.isNullOrEmpty()) {
                    directUrl = currentPost.trailer?.get("ytUrl") as? String
                        ?: currentPost.trailer?.get("youtube") as? String
                }
            } else if (type == "tvshow" || type == "tvshows") {
                clipId = currentPost.videos?.get("clipId") as? String
                if (clipId.isNullOrEmpty()) {
                    directUrl = currentPost.videos?.get("ytUrl") as? String
                        ?: currentPost.videos?.get("youtube") as? String
                }
            } else if (type == "video") {
                clipId = currentPost.videos?.get("clipId") as? String
                if (clipId.isNullOrEmpty()) {
                    directUrl = currentPost.videos?.get("ytUrl") as? String
                        ?: currentPost.videos?.get("youtube") as? String
                }
            }

            val resolved: String = when {
                !clipId.isNullOrEmpty() -> {
                    val playbackUrl = "https://api.interplanetary.tv/api/media-assets/playback/$clipId"
                    withContext(Dispatchers.IO) {
                        try {
                            val client = okhttp3.OkHttpClient.Builder()
                                .followRedirects(true).followSslRedirects(true).build()
                            val request = okhttp3.Request.Builder()
                                .url("$playbackUrl?format=json").build()
                            client.newCall(request).execute().use { response ->
                                if (response.isSuccessful) {
                                    val body = response.body?.string() ?: return@withContext ""
                                    val mapType = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
                                    val map: Map<String, Any> = com.google.gson.Gson().fromJson(body, mapType)
                                    map["url"] as? String ?: ""
                                } else ""
                            }
                        } catch (e: Exception) {
                            Log.e("DetailsScreen", "SVP API error: ${e.message}")
                            ""
                        }
                    }
                }
                !directUrl.isNullOrEmpty() -> directUrl ?: ""
                else -> ""
            }

            backgroundVideoUrl = resolved
        } catch (e: Exception) {
            Log.e("DetailsScreen", "Error resolving details background video: ${e.message}", e)
        } finally {
            isVideoResolving = false
        }
    }

    val imageAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isVideoReady) 0f else 0.4f,
        label = "DetailsImageAlpha"
    )

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // Header Image with Gradient and Background Video Player
            item {
                Box(modifier = Modifier.fillMaxWidth().height(450.dp)) {
                    // Video Player Layer
                    if (backgroundVideoUrl.isNotEmpty() && !isVideoResolving) {
                        val isYouTube = backgroundVideoUrl.contains("youtube.com") || backgroundVideoUrl.contains("youtu.be")
                        val videoId = if (isYouTube) {
                            Regex("(?:v=|/embed/|youtu\\.be/|/v/)([^#&?]+)").find(backgroundVideoUrl)?.groupValues?.get(1)
                        } else null

                        when {
                            isYouTube && videoId != null -> {
                                androidx.compose.ui.viewinterop.AndroidView(
                                    factory = { ctx ->
                                        com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView(ctx).apply {
                                            lifecycleOwner.lifecycle.addObserver(this)
                                            enableAutomaticInitialization = false
                                            initialize(object : com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener() {
                                                override fun onReady(youTubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer) {
                                                    youTubePlayer.mute()
                                                    youTubePlayer.loadVideo(videoId, 0f)
                                                    isVideoReady = true
                                                }
                                            }, com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions.Builder()
                                                .controls(0)
                                                .rel(0)
                                                .origin("https://interplanetary.tv")
                                                .build())
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> {
                                val exoPlayer = remember(backgroundVideoUrl) {
                                    androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
                                        repeatMode = androidx.media3.common.Player.REPEAT_MODE_ONE
                                        playWhenReady = true
                                        volume = 0f
                                        val mediaItem = androidx.media3.common.MediaItem.fromUri(android.net.Uri.parse(backgroundVideoUrl))
                                        setMediaItem(mediaItem)
                                        prepare()
                                        addListener(object : androidx.media3.common.Player.Listener {
                                            override fun onPlaybackStateChanged(state: Int) {
                                                if (state == androidx.media3.common.Player.STATE_READY) {
                                                    isVideoReady = true
                                                }
                                            }
                                        })
                                    }
                                }

                                DisposableEffect(exoPlayer) {
                                    onDispose { exoPlayer.release() }
                                }

                                androidx.compose.ui.viewinterop.AndroidView(
                                    factory = {
                                        androidx.media3.ui.PlayerView(it).apply {
                                            player = exoPlayer
                                            useController = false
                                            resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    // Fallback/loading Image
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        alpha = imageAlpha
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
                                    val playUrl = post?.getEffectiveVideoUrl() ?: ""
                                    onPlayClick(playUrl)
                                } else {
                                    if (!isLoggedIn) {
                                        onLoginRequired()
                                    } else {
                                        onSubscribeClick()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp).padding(horizontal = 8.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0F4098),
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = if (viewModel.canWatch()) "▶ Click now to Watch" else "👑 Subscribe to Watch",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Secondary Actions (Circular Buttons matching TV)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { 
                                viewModel.toggleWatchlist(id)
                                val message = if (!isInWatchlist) "Added to Watchlist" else "Removed from Watchlist"
                                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                            },
                            shape = CircleShape,
                            modifier = Modifier.padding(horizontal = 8.dp).size(48.dp),
                            contentPadding = PaddingValues(top = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.1f),
                                contentColor = Color.White
                            )
                        ) {
                            Text(if (isInWatchlist) "✓" else "+", fontSize = 20.sp)
                        }

                        Button(
                            onClick = { 
                                viewModel.toggleLiked(id)
                                val message = if (!isLiked) "Added to Liked" else "Removed from Liked"
                                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                            },
                            shape = CircleShape,
                            modifier = Modifier.padding(horizontal = 8.dp).size(48.dp),
                            contentPadding = PaddingValues(top = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.1f),
                                contentColor = Color.White
                            )
                        ) {
                            Text(if (isLiked) "❤️" else "👍", fontSize = 18.sp)
                        }

                        Button(
                            onClick = { 
                                viewModel.togglePlaylist(id)
                                // Note: Mobile viewModel lacks isInPlaylist state flow but let's mirror action
                                val message = "Added to Playlist"
                                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                            },
                            shape = CircleShape,
                            modifier = Modifier.padding(horizontal = 8.dp).size(48.dp),
                            contentPadding = PaddingValues(top = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.1f),
                                contentColor = Color.White
                            )
                        ) {
                            Text("🔗", fontSize = 18.sp)
                        }
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

package com.notifiy.interplanetary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.notifiy.interplanetary.data.model.Post
import com.notifiy.interplanetary.ui.components.MovieCard
import com.notifiy.interplanetary.ui.theme.Background
import com.notifiy.interplanetary.ui.theme.Primary
import com.notifiy.interplanetary.ui.theme.TextPrimary
import com.notifiy.interplanetary.ui.theme.TextSecondary
import com.notifiy.interplanetary.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onMovieClick: (Post) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedPost by remember { mutableStateOf<Post?>(null) }

    LaunchedEffect(state.liveTv, state.top10) {
        if (selectedPost == null) {
            selectedPost = state.liveTv.firstOrNull() ?: state.top10.firstOrNull()
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().background(Background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Primary)
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Background)
        ) {
            // Fixed Top Player Section (60%)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
                    .padding(top = 100.dp)
            ) {
                selectedPost?.let { post ->
                    HeroBanner(
                        post = post, 
                        onClick = { onMovieClick(post) }
                    )
                }
            }

            // Scrollable Categories Section (40%)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f),
                contentPadding = PaddingValues(bottom = 20.dp, top = 8.dp)
            )
            {
                if (state.liveTv.isNotEmpty()) {
                    item { Section(title = "Live TV", items = state.liveTv, onClick = { selectedPost = it }) }
                }
                if (state.watchlist.isNotEmpty()) {
                    item { Section(title = "My Wishlist", items = state.watchlist, onClick = { selectedPost = it }) }
                }
                if (state.top10.isNotEmpty()) {
                    item { Section(title = "Our Top 10", items = state.top10, onClick = { selectedPost = it }) }
                }
                if (state.bingeVideos.isNotEmpty()) {
                    item { Section(title = "Binge Videos", items = state.bingeVideos, onClick = { selectedPost = it }) }
                }
                if (state.bingeEpicSeries.isNotEmpty()) {
                    item { Section(title = "Binge-Epic Series", items = state.bingeEpicSeries, onClick = { selectedPost = it }) }
                }
                if (state.mustWatchSpaceEpic.isNotEmpty()) {
                    item { Section(title = "Must-Watch Space Epics", items = state.mustWatchSpaceEpic, onClick = { selectedPost = it }) }
                }
                if (state.spaceToGround.isNotEmpty()) {
                    item { Section(title = "Space-to-Ground Report", items = state.spaceToGround, onClick = { selectedPost = it }) }
                }
                if (state.news.isNotEmpty()) {
                    item { Section(title = "News", items = state.news, onClick = { selectedPost = it }) }
                }
                if (state.talkShows.isNotEmpty()) {
                    item { Section(title = "Talk-Shows", items = state.talkShows, onClick = { selectedPost = it }) }
                }
                if (state.documentarySeries.isNotEmpty()) {
                    item { Section(title = "Documentary Series", items = state.documentarySeries, onClick = { selectedPost = it }) }
                }
                if (state.documentaryFilms.isNotEmpty()) {
                    item { Section(title = "Documentary Film", items = state.documentaryFilms, onClick = { selectedPost = it }) }
                }
                if (state.scienceFiction.isNotEmpty()) {
                    item { Section(title = "Science-Fiction", items = state.scienceFiction, onClick = { selectedPost = it }) }
                }
            }
        }
    }
}

@Composable
fun HeroBanner(post: Post, onClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    val videoUrl = post.getEffectiveVideoUrl()
    
    val isYouTube = videoUrl.contains("youtube.com") || videoUrl.contains("youtu.be")
    val isEmbed = videoUrl.contains(".php") || videoUrl.contains("webvideocore")
    
    val videoId = if (isYouTube) {
        Regex("(?:v=|/embed/|youtu\\.be/|/v/)([^#&?]+)").find(videoUrl)?.groupValues?.get(1)
    } else null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() }
    ) {
        // 1. Thumbnail Background (Always visible as fallback/loading state)
        AsyncImage(
            model = post.getDisplayImageUrl(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Player Layer
        if (videoUrl.isNotEmpty()) {
            when {
                isYouTube && videoId != null -> {
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { ctx ->
                            com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView(ctx).apply {
                                lifecycleOwner.lifecycle.addObserver(this)
                                enableAutomaticInitialization = false // Fix: Disable auto-init for manual init
                                initialize(object : com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener() {
                                    override fun onReady(youTubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer) {
                                        youTubePlayer.mute()
                                        youTubePlayer.loadVideo(videoId, 0f)
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
                isEmbed -> {
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { ctx ->
                            android.webkit.WebView(ctx).apply {
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    mediaPlaybackRequiresUserGesture = false
                                    useWideViewPort = true
                                    loadWithOverviewMode = true
                                    databaseEnabled = true
                                    userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
                                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                }
                                
                                webViewClient = object : android.webkit.WebViewClient() {
                                    override fun onReceivedSslError(view: android.webkit.WebView?, handler: android.webkit.SslErrorHandler?, error: android.net.http.SslError?) {
                                        handler?.proceed()
                                    }
                                }
                                
                                val embedHtml = """
                                    <html>
                                    <body style="margin:0;padding:0;background:black;">
                                        <div style="position: relative; padding-bottom: 56.25%; height: 100vh; width: 100vw; overflow: hidden;">
                                            <iframe src="$videoUrl" 
                                                    style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: none;" 
                                                    allow="autoplay; fullscreen" 
                                                    allowfullscreen>
                                            </iframe>
                                        </div>
                                    </body>
                                    </html>
                                """.trimIndent()
                                loadDataWithBaseURL("https://interplanetary.tv", embedHtml, "text/html", "UTF-8", null)
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { view ->
                            val embedHtml = """
                                <html>
                                <body style="margin:0;padding:0;background:black;">
                                    <div style="position: relative; padding-bottom: 56.25%; height: 100vh; width: 100vw; overflow: hidden;">
                                        <iframe src="$videoUrl" 
                                                style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: none;" 
                                                allow="autoplay; fullscreen" 
                                                allowfullscreen>
                                        </iframe>
                                    </div>
                                </body>
                                </html>
                            """.trimIndent()
                            view.loadDataWithBaseURL("https://interplanetary.tv", embedHtml, "text/html", "UTF-8", null)
                        }
                    )
                }
                else -> {
                    val exoPlayer = remember(post.id) {
                        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
                            repeatMode = androidx.media3.common.Player.REPEAT_MODE_ONE
                            playWhenReady = true
                            volume = 0f
                            val mediaItem = androidx.media3.common.MediaItem.fromUri(android.net.Uri.parse(videoUrl))
                            setMediaItem(mediaItem)
                            prepare()
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
        
        // 3. Subtle Gradient Overlay (Only at the bottom half)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.4f to Color.Transparent,
                        1f to Background.copy(alpha = 0.95f)
                    )
                )
        )

        // 4. Content Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = post.title.rendered.replace(Regex("<[^>]*>"), "").trim(),
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sci-Fi", color = TextSecondary, fontSize = 14.sp)
                Text(" • ", color = TextSecondary)
                Text("Epic", color = TextSecondary, fontSize = 14.sp)
                Text(" • ", color = TextSecondary)
                Text("Space", color = TextSecondary, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Text("My List", color = Color.White, fontSize = 12.sp)
                }
                
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.height(40.dp).padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Play", fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.White)
                    Text("Info", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun Section(
    title: String,
    items: List<Post>,
    onClick: (Post) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            modifier = Modifier.padding(start = 20.dp),
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            items(items) { post ->
                MovieCard(
                    post = post, 
                    onClick = { onClick(post) },
                    width = 220.dp,
                    aspectRatio = 1.77f
                )
            }
        }
    }
}

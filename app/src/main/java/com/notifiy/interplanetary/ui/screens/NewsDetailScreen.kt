package com.notifiy.interplanetary.ui.screens
import kotlin.OptIn

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.ExperimentalComposeUiApi
import coil.compose.AsyncImage
import com.notifiy.interplanetary.data.model.NewsArticle
import com.notifiy.interplanetary.ui.viewmodel.NewsDetailViewModel
import com.notifiy.interplanetary.ui.viewmodel.NewsViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.focusable

private const val DETAIL_TAG = "siddharthaLogs"

@kotlin.OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    detailViewModel: NewsDetailViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    newsViewModel: NewsViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onArticleClick: (Int) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val detailState by detailViewModel.uiState.collectAsState()
    val newsState by newsViewModel.uiState.collectAsState()

    // Scroll state for the article content column
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Log.d(DETAIL_TAG, "NewsDetailScreen: Composing — isLoading=${detailState.isLoading}, id=${detailState.article?.id}")

    Scaffold(
//        topBar = {
//            NewsDetailTopBar(onBackClick)
//        },
        containerColor = Color(0xFF0B0B0F)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
//                .padding(padding)
        ) {
            when {
                detailState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF6C63FF))
                    }
                }
                detailState.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Failed to load article", color = Color.Red)
                            Button(onClick = { detailViewModel.loadArticle() }) { Text("Retry") }
                        }
                    }
                }
                else -> {
                    val article = detailState.article
                    if (article != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            // Article Header (Meta)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = article.getAuthorName().uppercase(),
                                    color = Color(0xFF6C63FF),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(" • ", color = Color.Gray)
                                Text(
                                    text = article.getFormattedDate(),
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            
                            Spacer(Modifier.height(12.dp))
                            
                            Text(
                                text = article.getCleanTitle(),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 32.sp
                                ),
                                color = Color.White
                            )
                            
                            Spacer(Modifier.height(24.dp))
                            
                            val thumb = article.getThumbnailUrl()
                            if (thumb.isNotEmpty()) {
                                AsyncImage(
                                    model = thumb,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Spacer(Modifier.height(24.dp))
                            }
                            
                            Text(
                                text = article.getCleanContent(),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 28.sp,
                                    color = Color(0xFFE0E0E0)
                                )
                            )
                            
                            val tags = article.getTags()
                            if (tags.isNotEmpty()) {
                                Spacer(Modifier.height(32.dp))
                                FlowTagRow(tags = tags)
                            }

                            val allArticles = if (newsState.searchQuery.isBlank()) newsState.articles else newsState.searchResults
                            val currentIndex = allArticles.indexOfFirst { it.id == article.id }
                            val prevArticle = if (currentIndex > 0) allArticles[currentIndex - 1] else null
                            val nextArticle = if (currentIndex != -1 && currentIndex < allArticles.size - 1) allArticles[currentIndex + 1] else null

                            if (prevArticle != null || nextArticle != null) {
                                Divider(color = Color(0xFF1C1C2E), thickness = 1.dp, modifier = Modifier.padding(vertical = 24.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Previous
                                    if (prevArticle != null) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onArticleClick(prevArticle.id) }
                                                .padding(end = 8.dp)
                                        ) {
                                            Text(
                                                text = "← PREVIOUS",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF6C63FF),
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = prevArticle.getCleanTitle(),
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    } else {
                                        Spacer(Modifier.weight(1f))
                                    }

                                    Spacer(Modifier.width(16.dp))

                                    // Next
                                    if (nextArticle != null) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onArticleClick(nextArticle.id) }
                                                .padding(start = 8.dp),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text(
                                                text = "NEXT →",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF6C63FF),
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = nextArticle.getCleanTitle(),
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                                            )
                                        }
                                    } else {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                                Spacer(Modifier.height(40.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlowTagRow(tags: List<String>) {
    val chunked = tags.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        chunked.forEach { rowTags ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rowTags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1C1C2E), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFAAAAAA),
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF0B0B0F),
            navigationIconContentColor = Color.White
        )
    )
}

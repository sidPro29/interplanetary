package com.notifiy.interplanetary.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import coil.compose.AsyncImage
import com.notifiy.interplanetary.data.model.NewsArticle
import com.notifiy.interplanetary.ui.viewmodel.NewsViewModel

private const val TAG = "siddharthaLogs"

@Composable
fun SpaceNewsScreen(
    viewModel: NewsViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onArticleClick: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    Log.d(TAG, "SpaceNewsScreen: Composing — articles=${uiState.articles.size}")

    val nearEnd by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            last >= total - 3 && total > 0
        }
    }
    LaunchedEffect(nearEnd) {
        if (nearEnd) viewModel.loadNextPage()
    }

    val articles = if (uiState.searchQuery.isBlank()) uiState.articles else uiState.searchResults

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0F))
    ) {
        // Search Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Space News",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            )
            Spacer(Modifier.height(12.dp))
            NewsSearchBar(
                query = uiState.searchQuery,
                isSearching = uiState.isSearching,
                onQueryChange = { viewModel.onSearchQueryChanged(it) }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading && uiState.articles.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF6C63FF))
                    }
                }
                uiState.error != null && uiState.articles.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Failed to load news", color = Color.Red)
                            Button(onClick = { viewModel.refresh() }) { Text("Retry") }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(articles, key = { it.id }) { article ->
                            NewsListCard(
                                article = article,
                                onClick = { onArticleClick(article.id) }
                            )
                        }
                        
                        if (uiState.isLoading) {
                            item {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF6C63FF),
                                        modifier = Modifier.size(24.dp)
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
fun NewsListCard(article: NewsArticle, onClick: () -> Unit) {
    val thumb = article.getThumbnailUrl()
    val excerpt = article.getCleanExcerpt()

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF13131E),
            contentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(horizontal = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left: Image
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF1A1A2E))
            ) {
                AsyncImage(
                    model = thumb.ifEmpty { "https://interplanetary.tv/wp-content/uploads/2024/01/news-placeholder.jpg" }, // Use a real placeholder if possible
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Right: Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = article.getCleanTitle(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = excerpt,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFAAAAAA),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = article.getFormattedDate(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6C63FF),
                        fontSize = 11.sp
                    )
                    Text(
                        text = article.getAuthorName().uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF888888),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false).padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NewsSearchBar(
    query: String,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .background(
                    color = if (isFocused) Color(0xFF1C1C2E) else Color(0xFF1A1A1A),
                    shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text("Search articles...", color = Color(0xFF555555), fontSize = 12.sp)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                cursorBrush = SolidColor(Color(0xFF6C63FF)),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused }
            )
        }
        Box(
            modifier = Modifier
                .height(40.dp)
                .background(Color(0xFF6C63FF), RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isSearching) "..." else "Search",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RecentPostLink(article: NewsArticle, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = Color(0xFFCCCCCC)
        ),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "›",
                color = Color(0xFF6C63FF),
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 6.dp, top = 1.dp)
            )
            Text(
                text = article.getCleanTitle(),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCCCCCC),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp
            )
        }
    }
}

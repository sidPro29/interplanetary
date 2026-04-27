package com.notifiy.interplanetary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import com.notifiy.interplanetary.data.model.Post
import com.notifiy.interplanetary.ui.components.MovieCard
import com.notifiy.interplanetary.ui.theme.Background
import com.notifiy.interplanetary.ui.theme.Primary
import com.notifiy.interplanetary.ui.theme.TextPrimary
import com.notifiy.interplanetary.ui.viewmodel.CatalogViewModel

@Composable
fun CatalogScreen(
    title: String,
    type: String,
    modifier: Modifier = Modifier,
    viewModel: CatalogViewModel = hiltViewModel(),
    onMovieClick: (Post) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(type) {
        viewModel.loadData(type)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (!state.error.isNullOrEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${state.error}", color = Color.Red)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(1), // 1 column for rectangle cards on mobile
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(state.items) { post ->
                    MovieCard(
                        post = post,
                        onClick = { onMovieClick(post) },
                        width = 400.dp, // Take more width for 1 column
                        aspectRatio = 1.77f // Landscape 16:9
                    )
                }
            }
        }
    }
}

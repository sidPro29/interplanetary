package com.notifiy.interplanetary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.notifiy.interplanetary.data.model.Post
import com.notifiy.interplanetary.ui.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onMovieClick: (Post) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var searchText by remember { mutableStateOf("") }
    
    // Sync search text with viewModel
    LaunchedEffect(searchText) {
        viewModel.onSearchQueryChanged(searchText)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // Search Bar
        SearchBox(
            searchText = searchText,
            onSearchTextChange = { 
                searchText = it 
            },
            onSearch = { viewModel.onSearch() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Filters
        FilterTabs(
            selectedFilter = state.selectedFilter,
            onFilterSelected = { viewModel.onFilterSelected(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Results
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 20.dp, top = 10.dp)
            ) {
                items(state.filteredItems) { item ->
                    SearchCard(
                        post = item.post,
                        onClick = { onMovieClick(item.post) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBox(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    BasicTextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
        cursorBrush = SolidColor(Color.White),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp)),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onSearch() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchText.isEmpty()) {
                        Text("Search...", color = Color.Gray)
                    }
                    innerTextField()
                }
                if (searchText.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onSearchTextChange("") }
                    )
                }
            }
        }
    )
}

@Composable
fun FilterTabs(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf("All", "Tvshow", "Video", "Movie")
    
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        filters.forEach { filter ->
            val isSelected = filter == selectedFilter
            TextButton(
                onClick = { onFilterSelected(filter) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (isSelected) Color(0xFF007AFF) else Color.Gray
                )
            ) {
                 Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = filter,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(2.dp)
                            .background(if (isSelected) Color(0xFF007AFF) else Color.Transparent)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchCard(
    post: Post,
    onClick: () -> Unit
) {
    val imageUrl = post.getDisplayImageUrl()
        
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .aspectRatio(16/9f),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
    }
}

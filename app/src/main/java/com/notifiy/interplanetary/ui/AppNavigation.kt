package com.notifiy.interplanetary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.notifiy.interplanetary.R
import com.notifiy.interplanetary.ui.screens.CatalogScreen
import com.notifiy.interplanetary.ui.screens.DetailsScreen
import com.notifiy.interplanetary.ui.screens.HomeScreen
import com.notifiy.interplanetary.ui.screens.NewsDetailScreen
import com.notifiy.interplanetary.ui.screens.PlayerScreen
import com.notifiy.interplanetary.ui.screens.SearchScreen
import com.notifiy.interplanetary.ui.screens.SpaceNewsScreen
import com.notifiy.interplanetary.ui.theme.Background
import com.notifiy.interplanetary.ui.theme.Primary
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    mainViewModel: com.notifiy.interplanetary.ui.viewmodel.MainViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "Home"

    val isLoggedIn by mainViewModel.isLoggedIn.collectAsState()
    val activePlan by mainViewModel.activePlan.collectAsState()
    val refreshTrigger by mainViewModel.refreshTrigger.collectAsState()

    val showExpiryPopup by mainViewModel.showExpiryPopup.collectAsState()

    if (showExpiryPopup) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                Button(onClick = { mainViewModel.handleExpiryOk() }) {
                    Text("OK")
                }
            },
            title = { Text("Plan Expired Alert") },
            text = { Text("Your current plan has expired. Please renew to continue enjoying premium content.") })
    }

    Scaffold(topBar = {
        if (!currentRoute.startsWith("Player") && !currentRoute.startsWith("Login") && !currentRoute.startsWith(
                "Signup"
            )
        ) {
            // Background surface that covers status bar area
            Surface(
                modifier = Modifier.fillMaxWidth(), color = Color(0xFF0B0B0F)
            ) {
                Column {
                    // Solid background matching status bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0B0B0F))
                            .statusBarsPadding() // Content starts after status bar
                            .height(64.dp)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        )
                        {
                           Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                               // Left: Logo (Animated GIF) - Increased height by 20% (35dp -> 42dp)
                               AsyncImage(
                                   model = R.drawable.logo,
                                   contentDescription = "Logo",
                                   modifier = Modifier
                                       .height(50.dp)
                                       .width(154.dp),
                                   contentScale = ContentScale.Fit
                               )

                               // Center: Thin Subscribe Label
                               if (activePlan == null) {
                                   Box(modifier = Modifier
                                       .clickable { navController.navigate("Plans") }
                                       .background(
                                           Primary.copy(alpha = 0.15f), RoundedCornerShape(2.dp)
                                       )
                                       .border(
                                           0.5.dp, Primary.copy(alpha = 0.5f), RoundedCornerShape(5.dp)
                                       )
                                       .padding(
                                           horizontal = 4.dp
                                           , vertical = 1.dp
                                       ), // Thinner padding
                                       contentAlignment = Alignment.Center) {
                                       Text(
                                           text = "SUBSCRIBE",
                                           color = Primary,
                                           fontSize = 7.sp, // Slightly smaller
                                           fontWeight = FontWeight.Bold,
                                           letterSpacing = 0.5.sp,
                                           lineHeight = 15.sp
                                       )
                                   }
                               } else {
                                   Spacer(modifier = Modifier.width(1.dp))
                               }
                           }

                            // Right: Search, Contact & Profile
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { navController.navigate("Search") }) {
                                    Icon(
                                        Icons.Default.Search,
                                        "Search",
                                        tint = Color.White,
                                        modifier = Modifier.size(21.dp)
                                    )
                                }
                                IconButton(onClick = { navController.navigate("ContactUs") }) {
                                    Icon(
                                        Icons.Default.ContactSupport,
                                        "Contact Us",
                                        tint = Color.White,
                                        modifier = Modifier.size(21.dp)
                                    )
                                }
                                IconButton(onClick = {
                                    if (isLoggedIn) navController.navigate("Profile") else navController.navigate(
                                        "Login"
                                    )
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Account",
                                        tint = if (isLoggedIn) Primary else Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }, bottomBar = {
        if (!currentRoute.startsWith("Player") && !currentRoute.startsWith("Details") && !currentRoute.startsWith(
                "Login"
            ) && !currentRoute.startsWith("Signup")
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(), color = Color(0xFF0B0B0F)
            ) {
                NavigationBar(
                    containerColor = Color.Transparent, // Using Surface background
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .background(Color(0xFF0B0B0F))
                        .navigationBarsPadding()
                ) {
                    val items = listOf(
                        Triple("Home", "Home", Icons.Default.Home),
                        Triple("News", "News", Icons.Default.Newspaper),
                        Triple("TV Shows", "TV Shows", Icons.Default.Tv),
                        Triple("Movies", "Movies", Icons.Default.Movie),
                        Triple("Videos", "Videos", Icons.Default.Movie)
                    )
                    items.forEach { (title, route, icon) ->
                        val isSelected = currentRoute == route
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = title) },
                            label = {
                                Text(
                                    title,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color.Transparent
                            ),
                            onClick = {
                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo("Home")
                                        launchSingleTop = true
                                    }
                                }
                            })
                    }
                }
            }
        }
    }, content = { innerPadding ->
        val effectivePadding =
            if (currentRoute == "Home" || currentRoute.startsWith("Details")) PaddingValues(0.dp) else innerPadding
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(effectivePadding)
                .background(Background)
        ) {
            val navigateToDetails: (com.notifiy.interplanetary.data.model.Post) -> Unit = { post ->
                val encodedUrl = URLEncoder.encode(
                    post.getDisplayImageUrl(), StandardCharsets.UTF_8.toString()
                ).replace("+", "%20")
                val cleanTitle = post.title.rendered.replace(Regex("<[^>]*>"), "").trim()
                val encodedTitle = URLEncoder.encode(cleanTitle, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20")
                val videoUrl = post.getEffectiveVideoUrl()
                val encodedVideoUrl = if (videoUrl.isNotEmpty()) URLEncoder.encode(
                    videoUrl, StandardCharsets.UTF_8.toString()
                ).replace("+", "%20") else ""
                val cleanDescription = (post.description ?: "").replace(Regex("<[^>]*>"), "").trim()
                val encodedDescription =
                    URLEncoder.encode(cleanDescription, StandardCharsets.UTF_8.toString())
                        .replace("+", "%20")

                navController.navigate("Details/${post.id}/$encodedTitle/$encodedUrl?videoUrl=$encodedVideoUrl&description=$encodedDescription")
            }

            NavHost(
                navController = navController,
                startDestination = "Home",
                modifier = Modifier.fillMaxSize()
            ) {
                composable("Home") {
                    val homeViewModel: com.notifiy.interplanetary.ui.viewmodel.HomeViewModel =
                        androidx.hilt.navigation.compose.hiltViewModel()
                    LaunchedEffect(refreshTrigger, Unit) { homeViewModel.loadData() }
                    HomeScreen(viewModel = homeViewModel, onMovieClick = navigateToDetails)
                }
                composable("News") {
                    val newsViewModel: com.notifiy.interplanetary.ui.viewmodel.NewsViewModel =
                        androidx.hilt.navigation.compose.hiltViewModel()
                    SpaceNewsScreen(
                        viewModel = newsViewModel,
                        onArticleClick = { articleId -> navController.navigate("NewsDetail/$articleId") })
                }
                composable(
                    "NewsDetail/{articleId}",
                    arguments = listOf(navArgument("articleId") { type = NavType.IntType })
                ) {
                    val detailViewModel: com.notifiy.interplanetary.ui.viewmodel.NewsDetailViewModel =
                        androidx.hilt.navigation.compose.hiltViewModel()
                    val newsViewModel: com.notifiy.interplanetary.ui.viewmodel.NewsViewModel =
                        androidx.hilt.navigation.compose.hiltViewModel()
                    NewsDetailScreen(
                        detailViewModel = detailViewModel,
                        newsViewModel = newsViewModel,
                        onArticleClick = { articleId -> navController.navigate("NewsDetail/$articleId") },
                        onBackClick = { navController.popBackStack() })
                }
                composable("TV Shows") {
                    CatalogScreen(
                        title = "TV Shows", type = "TV Shows", onMovieClick = navigateToDetails
                    )
                }
                composable("Movies") {
                    CatalogScreen(
                        title = "Movies", type = "Movies", onMovieClick = navigateToDetails
                    )
                }
                composable("Search") { SearchScreen(onMovieClick = navigateToDetails) }
                composable(
                    "Plans?redirectTo={redirectTo}",
                    arguments = listOf(navArgument("redirectTo") { defaultValue = "Plans" })
                ) { backStackEntry ->
                    val redirectTo = backStackEntry.arguments?.getString("redirectTo") ?: "Plans"
                    val context = androidx.compose.ui.platform.LocalContext.current
                    com.notifiy.interplanetary.ui.screens.PlansScreen(
                        isLoggedIn = isLoggedIn,
                        onLoginRequired = {
                            navController.navigate("Login?redirectTo=$redirectTo")
                        },
                        onPaymentError = { error ->
                            android.widget.Toast.makeText(
                                context, "Error: $error", android.widget.Toast.LENGTH_LONG
                            ).show()
                        }, onPaymentSuccess = {
                            mainViewModel.updateLoginStatus()
                            android.widget.Toast.makeText(
                                context,
                                "Payment Successful!",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            navController.navigate("Home") {
                                popUpTo("Plans") {
                                    inclusive = true
                                }
                            }
                        })
                }
                composable(
                    "Login?redirectTo={redirectTo}",
                    arguments = listOf(navArgument("redirectTo") { defaultValue = "Home" })
                ) { backStackEntry ->
                    val redirectTo = backStackEntry.arguments?.getString("redirectTo") ?: "Home"
                    com.notifiy.interplanetary.ui.screens.LoginScreen(
                        onLoginSuccess = {
                            mainViewModel.updateLoginStatus(); navController.navigate(
                            redirectTo
                        ) { popUpTo("Login") { inclusive = true }; launchSingleTop = true }
                        },
                        onSignupClick = { navController.navigate("Signup?redirectTo=$redirectTo") })
                }
                composable(
                    "Signup?redirectTo={redirectTo}",
                    arguments = listOf(navArgument("redirectTo") { defaultValue = "Home" })
                ) { backStackEntry ->
                    val redirectTo = backStackEntry.arguments?.getString("redirectTo") ?: "Home"
                    com.notifiy.interplanetary.ui.screens.SignupScreen(
                        onSignupSuccess = {
                            mainViewModel.updateLoginStatus(); navController.navigate(
                            redirectTo
                        ) { popUpTo("Signup") { inclusive = true }; launchSingleTop = true }
                        },
                        onLoginClick = { navController.navigate("Login?redirectTo=$redirectTo") })
                }
                composable("Profile") {
                    if (!isLoggedIn) {
                        LaunchedEffect(Unit) {
                            navController.navigate("Login") {
                                popUpTo("Profile") {
                                    inclusive = true
                                }
                            }
                        }
                    } else {
                        com.notifiy.interplanetary.ui.screens.ProfileScreen(onLogoutConfirm = {
                            mainViewModel.logout(); navController.navigate(
                            "Home"
                        ) { popUpTo("Home") { inclusive = true }; launchSingleTop = true }
                        }, onMovieClick = navigateToDetails)
                    }
                }
                composable(
                    "Details/{id}/{title}/{imageUrl}?videoUrl={videoUrl}&description={description}",
                    arguments = listOf(
                        navArgument("id") { type = NavType.IntType },
                        navArgument("title") { type = NavType.StringType },
                        navArgument("imageUrl") { type = NavType.StringType },
                        navArgument("videoUrl") {
                            type = NavType.StringType; defaultValue = ""
                        },
                        navArgument("description") {
                            type = NavType.StringType; defaultValue = ""
                        })) { backStackEntry ->
                    DetailsScreen(
                        id = backStackEntry.arguments?.getInt("id") ?: 0,
                        title = backStackEntry.arguments?.getString("title") ?: "",
                        description = backStackEntry.arguments?.getString("description") ?: "",
                        imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: "",
                        isVideoAvailable = (backStackEntry.arguments?.getString("videoUrl")
                            ?: "").isNotEmpty(),
                        isLoggedIn = isLoggedIn,
                        onLoginRequired = {
                            val id = backStackEntry.arguments?.getInt("id") ?: 0
                            val title = backStackEntry.arguments?.getString("title") ?: ""
                            val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
                            val videoUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
                            val description = backStackEntry.arguments?.getString("description") ?: ""
                            
                            val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString()).replace("+", "%20")
                            val encodedImageUrl = URLEncoder.encode(imageUrl, StandardCharsets.UTF_8.toString()).replace("+", "%20")
                            val encodedVideoUrl = URLEncoder.encode(videoUrl, StandardCharsets.UTF_8.toString()).replace("+", "%20")
                            val encodedDescription = URLEncoder.encode(description, StandardCharsets.UTF_8.toString()).replace("+", "%20")
                            
                            val route = "Details/$id/$encodedTitle/$encodedImageUrl?videoUrl=$encodedVideoUrl&description=$encodedDescription"
                            navController.navigate("Login?redirectTo=$route")
                        },
                        onPlayClick = {
                            val vUrl = backStackEntry.arguments?.getString("videoUrl") ?: "";
                            val encodedVideoUrl =
                                URLEncoder.encode(vUrl, StandardCharsets.UTF_8.toString()).replace(
                                        "+", "%20"
                                    ); navController.navigate("Player?videoUrl=$encodedVideoUrl")
                        },
                        onSubscribeClick = { navController.navigate("Plans") },
                        onMovieClick = navigateToDetails
                    )
                }
                composable(
                    "Player?videoUrl={videoUrl}", arguments = listOf(navArgument("videoUrl") {
                        type = NavType.StringType; defaultValue = ""
                    })
                ) { backStackEntry ->
                    PlayerScreen(
                        videoUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
                    )
                }

                // Additional Catalog Routes from TV App
                composable("News Videos") {
                    CatalogScreen(
                        title = "News Videos", type = "News", onMovieClick = navigateToDetails
                    )
                }
                composable("Videos") {
                    CatalogScreen(
                        title = "Videos", type = "Videos", onMovieClick = navigateToDetails
                    )
                }
                composable("Documentary Films") {
                    CatalogScreen(
                        title = "Documentary Films",
                        type = "Documentary Films",
                        onMovieClick = navigateToDetails
                    )
                }
                composable("Documentary Series") {
                    CatalogScreen(
                        title = "Documentary Series",
                        type = "Documentary Series",
                        onMovieClick = navigateToDetails
                    )
                }
                composable("Science-Fiction") {
                    CatalogScreen(
                        title = "Science-Fiction",
                        type = "Science-Fiction",
                        onMovieClick = navigateToDetails
                    )
                }
                composable("ContactUs") {
                    com.notifiy.interplanetary.ui.screens.ContactUsScreen(onBackClick = { navController.popBackStack() })
                }
            }
        }
    })
}
package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.MainApplication
import com.example.ui.home.HomeScreen
import com.example.ui.player.FullPlayerScreen
import com.example.ui.player.MiniPlayer
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AppNavigation() {
    val application = LocalContext.current.applicationContext as MainApplication
    val factory = MainViewModel.Factory(application.container)
    val viewModel: MainViewModel = viewModel(factory = factory)
    
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    
    val showFullPlayer by viewModel.showFullPlayer.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var showErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            showErrorDialog = true
        }
    }

    // Error Dialog
    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                viewModel.clearError()
            },
            title = { Text("Error") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                Button(
                    onClick = {
                        showErrorDialog = false
                        viewModel.clearError()
                    }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!showFullPlayer) {
                Column {
                    MiniPlayer(viewModel = viewModel)
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            selected = currentRoute == "home",
                            onClick = { navController.navigate("home") }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            label = { Text("Search") },
                            selected = currentRoute == "search",
                            onClick = { navController.navigate("search") }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                            label = { Text("Favorites") },
                            selected = currentRoute == "favorites",
                            onClick = { navController.navigate("favorites") }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Info, contentDescription = "About") },
                            label = { Text("About") },
                            selected = currentRoute == "about",
                            onClick = { navController.navigate("about") }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideInHorizontally(initialOffsetX = { 300 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -300 }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -300 }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 300 }) + fadeOut() }
        ) {
            composable("home") {
                HomeScreen(viewModel = viewModel)
            }
            composable("search") {
                com.example.ui.search.SearchScreen(viewModel = viewModel)
            }
            composable("favorites") {
                com.example.ui.favorite.FavoriteScreen(viewModel = viewModel)
            }
            composable("about") {
                com.example.ui.about.AboutMeScreen()
            }
        }
        
        AnimatedVisibility(
            visible = showFullPlayer,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            FullPlayerScreen(viewModel = viewModel)
        }
    }
}

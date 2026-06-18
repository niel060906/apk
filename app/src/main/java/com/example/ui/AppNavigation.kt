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
    val currentRoute = navBackStackEntry?.destination?.route
    
    val showFullPlayer by viewModel.showFullPlayer.collectAsState()

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
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
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
        }
        
        AnimatedVisibility(
            visible = showFullPlayer,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            FullPlayerScreen(viewModel = viewModel)
        }
    }
}

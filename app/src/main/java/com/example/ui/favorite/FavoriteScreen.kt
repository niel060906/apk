package com.example.ui.favorite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.home.EmptyState
import com.example.ui.home.SongListItem

@Composable
fun FavoriteScreen(viewModel: MainViewModel) {
    val favorites by viewModel.favoriteSongs.collectAsState()
    val currentSong by viewModel.playerController.currentSong.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Text(
                text = "Favorites",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(16.dp)
            )
        }

        if (favorites.isEmpty()) {
            item {
                EmptyState(message = "No favorite songs yet. Start adding your favorites!")
            }
        } else {
            items(favorites) { song ->
                SongListItem(
                    song = song,
                    isNowPlaying = song.videoId == currentSong?.videoId,
                    onClick = { viewModel.playSong(song) }
                )
            }
        }
    }
}

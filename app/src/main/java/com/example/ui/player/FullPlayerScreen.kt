package com.example.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.MainViewModel

@Composable
fun FullPlayerScreen(viewModel: MainViewModel) {
    val currentSong by viewModel.playerController.currentSong.collectAsState()
    val isPlaying by viewModel.playerController.isPlaying.collectAsState()
    val position by viewModel.playerController.currentPosition.collectAsState()
    val duration by viewModel.playerController.duration.collectAsState()

    if (currentSong == null) return

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.closeFullPlayer() }) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close")
                }
                Text("Now Playing", style = MaterialTheme.typography.bodyMedium)
                IconButton(onClick = { /* menu */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            AsyncImage(
                model = currentSong?.catboxThumb,
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSong?.title ?: "",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                    Text(
                        text = currentSong?.artist ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { currentSong?.let { viewModel.toggleFavorite(it) } }) {
                    Icon(
                        imageVector = if (currentSong?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (currentSong?.isFavorite == true) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            var sliderPosition by remember { mutableStateOf<Float?>(null) }
            val currentPos = sliderPosition ?: position.toFloat()
            val maxPos = duration.toFloat().takeIf { it > 0 } ?: 1f

            Slider(
                value = currentPos,
                onValueChange = { sliderPosition = it },
                onValueChangeFinished = { 
                    sliderPosition?.let { viewModel.playerController.seekTo(it.toLong()) }
                    sliderPosition = null
                },
                valueRange = 0f..maxPos,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTime(currentPos.toLong()), style = MaterialTheme.typography.labelSmall)
                Text(formatTime(duration), style = MaterialTheme.typography.labelSmall)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.playerController.prev() }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(48.dp))
                }
                
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(80.dp).clickable { 
                        if (isPlaying) viewModel.playerController.pause() 
                        else viewModel.playerController.play() 
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                
                IconButton(onClick = { viewModel.playerController.next() }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", modifier = Modifier.size(48.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

package com.example.ui.player

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import com.example.ui.theme.NowPlayingIndicator

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
            
            // Header with Close & Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.closeFullPlayer() }) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close", modifier = Modifier.size(28.dp))
                }
                Text("Now Playing", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                IconButton(onClick = { /* menu */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", modifier = Modifier.size(28.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Album Art with Animation
            AnimatedContent(
                targetState = currentSong?.videoId,
                label = "albumArtChange"
            ) { targetId ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = currentSong?.catboxThumb,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Playing Indicator Overlay
                    if (isPlaying) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(NowPlayingIndicator.copy(alpha = 0.15f))
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Song Title and Artist
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSong?.title ?: "",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentSong?.artist ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Favorite Button
                IconButton(
                    onClick = { currentSong?.let { viewModel.toggleFavorite(it) } },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (currentSong?.isFavorite == true) NowPlayingIndicator.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (currentSong?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (currentSong?.isFavorite == true) NowPlayingIndicator else LocalContentColor.current,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Progress Slider with Time Display
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                colors = SliderDefaults.colors(
                    thumbColor = NowPlayingIndicator,
                    activeTrackColor = NowPlayingIndicator
                )
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTime(currentPos.toLong()), style = MaterialTheme.typography.labelSmall)
                Text(formatTime(duration), style = MaterialTheme.typography.labelSmall)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Playback Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Button
                IconButton(
                    onClick = { viewModel.playerController.prev() },
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(32.dp))
                }
                
                // Play/Pause Button (Large)
                Surface(
                    shape = CircleShape,
                    color = NowPlayingIndicator,
                    modifier = Modifier
                        .size(80.dp)
                        .clickable { 
                            if (isPlaying) viewModel.playerController.pause() 
                            else viewModel.playerController.play() 
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            modifier = Modifier.size(40.dp),
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
                
                // Next Button
                IconButton(
                    onClick = { viewModel.playerController.next() },
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

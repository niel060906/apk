package com.example.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.MainViewModel

@Composable
fun MiniPlayer(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val currentSong by viewModel.playerController.currentSong.collectAsState()
    val isPlaying by viewModel.playerController.isPlaying.collectAsState()

    if (currentSong == null) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp) // Apple Music style floating gap
            .clip(RoundedCornerShape(12.dp))
            .clickable { viewModel.openFullPlayer() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = currentSong?.catboxThumb,
                contentDescription = "Cover",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentSong?.title ?: "",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            IconButton(onClick = { 
                if (isPlaying) viewModel.playerController.pause() 
                else viewModel.playerController.play() 
            }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause"
                )
            }
            
            IconButton(onClick = { viewModel.playerController.next() }) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next"
                )
            }
        }
    }
}

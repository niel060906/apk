package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.Song
import com.example.ui.MainViewModel
import com.example.ui.theme.Error
import com.example.ui.theme.NowPlayingIndicator
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val songs by viewModel.allSongs.collectAsState()
    val recents by viewModel.recentlyPlayed.collectAsState()
    val currentSong by viewModel.playerController.currentSong.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Text(
                text = "Listen Now",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(16.dp)
            )
        }

        // Auto-Scrolling Banner Carousel
        if (songs.isNotEmpty()) {
            item {
                BannerSlider(
                    songs = songs,
                    currentSong = currentSong,
                    onClick = { viewModel.playSong(it) }
                )
            }
        }

        // Recently Played Section
        if (recents.isNotEmpty()) {
            item {
                SectionHeader("Recently Played")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(recents) { song ->
                        RecentItem(
                            song = song,
                            isNowPlaying = song.videoId == currentSong?.videoId,
                            onClick = { viewModel.playSong(song) }
                        )
                    }
                }
            }
        }

        // New Uploads Section
        item {
            SectionHeader("New Uploads")
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (songs.isEmpty()) {
            item {
                EmptyState(message = "No songs available. Try refreshing!")
            }
        } else {
            items(songs) { song ->
                SongListItem(
                    song = song,
                    isNowPlaying = song.videoId == currentSong?.videoId,
                    onClick = { viewModel.playSong(song) }
                )
            }
        }
    }
}

@Composable
fun BannerSlider(
    songs: List<Song>,
    currentSong: Song?,
    onClick: (Song) -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000) // Auto-scroll every 5 seconds
            currentIndex = (currentIndex + 1) % songs.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Banner Background Image
        AsyncImage(
            model = songs[currentIndex].catboxThumb,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.Transparent,
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f)
                        ),
                        startY = 50f
                    )
                )
        )

        // Content Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = songs[currentIndex].title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = songs[currentIndex].artist,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Play Button
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = NowPlayingIndicator,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .clickable { onClick(songs[currentIndex]) }
                .size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Indicator Dots
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(minOf(songs.size, 5)) { index ->
                Surface(
                    shape = RoundedCornerShape(2.dp),
                    color = if (index == currentIndex % songs.size) NowPlayingIndicator else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(
                        width = if (index == currentIndex % songs.size) 16.dp else 6.dp,
                        height = 4.dp
                    )
                ) {}
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun RecentItem(
    song: Song,
    isNowPlaying: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = song.catboxThumb,
                contentDescription = song.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Now Playing Indicator
            if (isNowPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NowPlayingIndicator.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NowPlayingIndicator,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            "Playing",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(4.dp),
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SongListItem(
    song: Song,
    isNowPlaying: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isNowPlaying) NowPlayingIndicator.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model = song.catboxThumb,
                contentDescription = song.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            if (isNowPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NowPlayingIndicator.copy(alpha = 0.4f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isNowPlaying) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = NowPlayingIndicator,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    "▶",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(4.dp),
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
                    CircularProgressIndicator()
                }
            }
        } else if (songs.isEmpty()) {
            item {
                EmptyState(message = "No songs available. Try refreshing!")
            }
        } else {
            items(songs) { song ->
                SongListItem(
                    song = song,
                    isNowPlaying = song.videoId == currentSong?.videoId,
                    onClick = { viewModel.playSong(song) }
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun RecentItem(
    song: Song,
    isNowPlaying: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = song.catboxThumb,
                contentDescription = song.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Now Playing Indicator
            if (isNowPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NowPlayingIndicator.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NowPlayingIndicator,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            "Playing",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(4.dp),
                            color = com.example.ui.theme.Color.White
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SongListItem(
    song: Song,
    isNowPlaying: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isNowPlaying) NowPlayingIndicator.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model = song.catboxThumb,
                contentDescription = song.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            if (isNowPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NowPlayingIndicator.copy(alpha = 0.4f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isNowPlaying) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = NowPlayingIndicator,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    "▶",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(4.dp),
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

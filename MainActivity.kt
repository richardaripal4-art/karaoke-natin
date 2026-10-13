package com.karaokenatin.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

data class Song(
    val title: String,
    val singer: String,
    val uri: Uri,
    val lyrics: String = "Magdagdag ng lyrics sa pamamagitan ng pag-edit ng kantang ito sa susunod na bersyon."
)

class MainActivity : ComponentActivity() {
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = ExoPlayer.Builder(this).build()
        setContent {
            MaterialTheme {
                KaraokeApp(
                    onPlay = { uri ->
                        player?.apply {
                            setMediaItem(MediaItem.fromUri(uri))
                            prepare()
                            playWhenReady = true
                        }
                    },
                    onPause = { player?.pause() }
                )
            }
        }
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KaraokeApp(onPlay: (Uri) -> Unit, onPause: () -> Unit) {
    val songs = remember { mutableStateListOf<Song>() }
    val queue = remember { mutableStateListOf<Song>() }
    var search by remember { mutableStateOf("") }
    var nowPlaying by remember { mutableStateOf<Song?>(null) }
    var showQueue by remember { mutableStateOf(false) }
    var showOnlineDialog by remember { mutableStateOf(false) }
    var onlineUrl by remember { mutableStateOf("") }
    var onlineTitle by remember { mutableStateOf("") }
    var onlineSinger by remember { mutableStateOf("") }
    var lyricsSong by remember { mutableStateOf<Song?>(null) }

    val pickAudio = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            runCatching {
                // Keep read access to selected document after app restart.
                // Some document providers may not allow persistable access; ignore if unavailable.
                songs.add(
                    Song(
                        title = uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.')
                            ?.replace('_', ' ') ?: "Local song",
                        singer = "Unknown singer",
                        uri = uri
                    )
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("KARAOKE NATIN")
                        Text("Kanta tayo!", style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    IconButton(onClick = { showQueue = !showQueue }) {
                        Icon(Icons.Default.QueueMusic, contentDescription = "Queue")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { pickAudio.launch(arrayOf("audio/*", "video/*")) }) {
                Icon(Icons.Default.Add, contentDescription = "Magdagdag ng kanta")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Hanapin ang kanta o singer") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { pickAudio.launch(arrayOf("audio/*", "video/*")) }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Pumili ng files")
                }
                OutlinedButton(onClick = { showOnlineDialog = true }) {
                    Text("Online URL")
                }
            }

            nowPlaying?.let { song ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("NGAYONG TUMUTUGTOG", style = MaterialTheme.typography.labelSmall)
                        Text(song.title, style = MaterialTheme.typography.titleMedium)
                        Text(song.singer, style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = onPause) { Text("Pause") }
                            TextButton(onClick = { lyricsSong = song }) { Text("Lyrics") }
                        }
                    }
                }
            }

            if (showQueue) {
                Text("SONG QUEUE (${queue.size})", style = MaterialTheme.typography.titleMedium)
                if (queue.isEmpty()) Text("Wala pang nakapila.")
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(queue) { index, song ->
                        ListItem(
                            headlineContent = { Text(song.title) },
                            supportingContent = { Text(song.singer) },
                            leadingContent = { Text("${index + 1}") },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = {
                                        nowPlaying = song
                                        onPlay(song.uri)
                                        queue.remove(song)
                                    }) { Icon(Icons.Default.PlayArrow, contentDescription = "Play") }
                                    IconButton(onClick = { queue.remove(song) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Tanggalin")
                                    }
                                }
                            }
                        )
                    }
                }
            } else {
                Text("SONG LIBRARY (${songs.size})", style = MaterialTheme.typography.titleMedium)
                if (songs.isEmpty()) {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Wala pang kanta.")
                            Text("Pindutin ang + para pumili ng audio o video file.")
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(songs.filter {
                            it.title.contains(search, true) || it.singer.contains(search, true)
                        }) { _, song ->
                            Card(Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f).padding(vertical = 10.dp)) {
                                        Text(song.title, style = MaterialTheme.typography.titleSmall)
                                        Text(song.singer, style = MaterialTheme.typography.bodySmall)
                                    }
                                    IconButton(onClick = { queue.add(song) }) {
                                        Icon(Icons.Default.QueueMusic, contentDescription = "Idagdag sa queue")
                                    }
                                    IconButton(onClick = {
                                        nowPlaying = song
                                        onPlay(song.uri)
                                    }) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Patugtugin")
                                    }
                                    TextButton(onClick = { lyricsSong = song }) { Text("Lyrics") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showOnlineDialog) {
        AlertDialog(
            onDismissRequest = { showOnlineDialog = false },
            title = { Text("Magdagdag ng online media URL") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Gumamit lamang ng direktang audio/video URL o stream na pinahihintulutan ng provider. Hindi tinatanggap ang ordinaryong YouTube watch link bilang direktang media file.")
                    OutlinedTextField(onlineTitle, { onlineTitle = it }, label = { Text("Pamagat") })
                    OutlinedTextField(onlineSinger, { onlineSinger = it }, label = { Text("Singer") })
                    OutlinedTextField(onlineUrl, { onlineUrl = it }, label = { Text("Direktang media URL") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val uri = runCatching { Uri.parse(onlineUrl.trim()) }.getOrNull()
                    if (uri != null && (uri.scheme == "https" || uri.scheme == "http") && !onlineTitle.isBlank()) {
                        songs.add(Song(onlineTitle.trim(), onlineSinger.ifBlank { "Online source" }, uri))
                        onlineTitle = ""; onlineSinger = ""; onlineUrl = ""
                        showOnlineDialog = false
                    }
                }) { Text("Idagdag") }
            },
            dismissButton = {
                TextButton(onClick = { showOnlineDialog = false }) { Text("Kanselahin") }
            }
        )
    }

    lyricsSong?.let { song ->
        AlertDialog(
            onDismissRequest = { lyricsSong = null },
            title = { Text("Lyrics: ${song.title}") },
            text = {
                Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                    Text(song.lyrics)
                    Spacer(Modifier.height(8.dp))
                    Text("Sa starter version, placeholder muna ang lyrics. Idadagdag sa susunod ang pag-import ng LRC/SRT lyrics at pag-edit ng singer/title.")
                }
            },
            confirmButton = {
                TextButton(onClick = { lyricsSong = null }) { Text("Isara") }
            }
        )
    }
}

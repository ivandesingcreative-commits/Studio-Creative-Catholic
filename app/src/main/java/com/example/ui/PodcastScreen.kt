package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.GenerationConfig
import com.example.data.PodcastEpisode
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastScreen(
    viewModel: PodcastViewModel,
    modifier: Modifier = Modifier
) {
    val episodes by viewModel.episodes.collectAsStateWithLifecycle()
    val currentlyPlaying by viewModel.currentlyPlaying.collectAsStateWithLifecycle()
    val voiceState by viewModel.voicePlaybackState.collectAsStateWithLifecycle()
    val generationState by viewModel.generationState.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Púlpito (Player), 1: Biblioteca (List), 2: Scriptorium (Generator)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SacredCrossIcon(modifier = Modifier.size(28.dp))
                        Column {
                            Text(
                                text = "La Voz de la Tradición",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Podcast y Crónicas Proféticas",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = { viewModel.stopEpisode() }) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Detener todo audio",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.VolumeUp, contentDescription = "Púlpito de Don Aurelio") },
                    label = { Text("Púlpito", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.LibraryBooks, contentDescription = "Biblioteca de Crónicas") },
                    label = { Text("Biblioteca", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.Create, contentDescription = "Scriptorium de Generación") },
                    label = { Text("Scriptorium", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                0 -> PlayerTab(
                    currentlyPlaying = currentlyPlaying,
                    voiceState = voiceState,
                    onPlayPause = {
                        currentlyPlaying?.let {
                            if (voiceState is PlaybackState.Playing) {
                                viewModel.pauseEpisode()
                            } else {
                                viewModel.playEpisode(it)
                            }
                        }
                    },
                    onStop = { viewModel.stopEpisode() },
                    onNavigateToScriptorium = { activeTab = 2 }
                )
                1 -> LibraryTab(
                    episodes = episodes,
                    currentlyPlaying = currentlyPlaying,
                    voiceState = voiceState,
                    onSelectEpisode = { episode ->
                        viewModel.playEpisode(episode)
                        activeTab = 0 // jump to player
                    },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onDelete = { viewModel.deleteEpisode(it) }
                )
                2 -> ScriptoriumTab(
                    generationState = generationState,
                    onGenerate = { topic -> viewModel.generateNewEpisode(topic) },
                    onClearState = { viewModel.clearGenerationState() },
                    onBackToPlayer = { activeTab = 0 }
                )
            }
        }
    }
}

// ------------------- COMPOSABLE: ACTIVE TAB (PLAYER) -------------------
@Composable
fun PlayerTab(
    currentlyPlaying: PodcastEpisode?,
    voiceState: PlaybackState,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onNavigateToScriptorium: () -> Unit
) {
    if (currentlyPlaying == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(ElegantGoldAccent.copy(alpha = 0.15f), Color.Transparent)
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                SacredCrossIcon(modifier = Modifier.size(72.dp), color = ElegantGoldAccent)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Púlpito Silencioso",
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No hay ninguna chronicle activa en este momento. Visite la Biblioteca para seleccionar una o ingrese al Scriptorium para invocar un nuevo guion doctrinal.",
                fontSize = 14.sp,
                fontFamily = FontFamily.Serif,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onNavigateToScriptorium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = AbsoluteCutCornerShape(8.dp)
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generar Crónica Católica", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Player Display with Cathedral Window & Title
            Row(
                modifier = Modifier
                    .fillMaxHeight(0.35f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CathedralArchCover(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = currentlyPlaying.category.uppercase(),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            letterSpacing = 1.2.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentlyPlaying.title,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Narrador: Don Aurelio",
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isPlaying = voiceState is PlaybackState.Playing
                        
                        FloatingActionButton(
                            onClick = onPlayPause,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pausar" else "Escuchar",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(onClick = onStop) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Detener",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Small voice visualizer
                        AnimatedVisibility(
                            visible = isPlaying,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            PulsingWaveform(modifier = Modifier.width(42.dp).height(24.dp))
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), thickness = 1.dp)

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle / Description of history
            Text(
                text = currentlyPlaying.description,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 12.5.sp,
                lineHeight = 17.0.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .padding(8.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        RoundedCornerShape(4.dp)
                    )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Real-time scrolling transcript
            Text(
                text = "GUION ORIGINAL DEL ORADOR",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            val currentParaIdx = if (voiceState is PlaybackState.Playing) voiceState.currentParagraphIndex else -1
            ScrollingTranscript(
                scriptText = currentlyPlaying.scriptText,
                currentParagraphIndex = currentParaIdx,
                isPlaying = voiceState is PlaybackState.Playing,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.25f))
                    .padding(12.dp)
            )
        }
    }
}

// ------------------- COMPOSABLE: BIBLIOTECA TAB (LIST) -------------------
@Composable
fun LibraryTab(
    episodes: List<PodcastEpisode>,
    currentlyPlaying: PodcastEpisode?,
    voiceState: PlaybackState,
    onSelectEpisode: (PodcastEpisode) -> Unit,
    onToggleFavorite: (PodcastEpisode) -> Unit,
    onDelete: (PodcastEpisode) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterFavorites by remember { mutableStateOf(false) }

    val filteredEpisodes = episodes.filter {
        (it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)) &&
                (!filterFavorites || it.isFavorite)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Biblioteca de Crónicas",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Colección de sagrados podcast marianos pre-conciliares sobre la restauración de la fe y el imperio legítimo.",
            fontSize = 12.sp,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Search and Filter Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar crónica...", fontSize = 13.sp, fontFamily = FontFamily.Serif) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp)
            )

            IconToggleButton(
                checked = filterFavorites,
                onCheckedChange = { filterFavorites = it },
                modifier = Modifier.background(
                    if (filterFavorites) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    RoundedCornerShape(8.dp)
                )
            ) {
                Icon(
                    imageVector = if (filterFavorites) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favoritos",
                    tint = if (filterFavorites) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredEpisodes.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ImportContacts,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ninguna crónica profética encontrada en las bóvedas.",
                        fontFamily = FontFamily.Serif,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredEpisodes, key = { it.id }) { episode ->
                    val isActive = currentlyPlaying?.id == episode.id
                    val isPlaying = isActive && voiceState is PlaybackState.Playing

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectEpisode(episode) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Indicator Play Icon
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        if (isActive) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        AbsoluteCutCornerShape(6.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Oír",
                                    tint = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Details Column
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = episode.title,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                
                                Text(
                                    text = episode.description,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 16.sp
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Category Tag
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = episode.category,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // Duration
                                    Text(
                                        text = "${episode.duration / 60}:${String.format("%02d", episode.duration % 60)} min",
                                        fontSize = 10.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            // Favorite and delete buttons
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.Center
                            ) {
                                IconButton(
                                    onClick = { onToggleFavorite(episode) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = if (episode.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Favorito",
                                        tint = if (episode.isFavorite) ElegantGoldAccent else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                IconButton(
                                    onClick = { onDelete(episode) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Borrar",
                                        tint = ElegantRedAccent,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------- COMPOSABLE: SCRIPTORIUM TAB (GENERATOR) -------------------
@Composable
fun ScriptoriumTab(
    generationState: GenerationUiState,
    onGenerate: (String) -> Unit,
    onClearState: () -> Unit,
    onBackToPlayer: () -> Unit
) {
    var topicQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val suggestions = listOf(
        "El resurgimiento glorioso del Gran Monarca según San Francisco de Paula",
        "El Pastor Angelical y el reinado celestial del Papa Santo",
        "El enigma escatológico pre-conciliar y el fin del modernismo",
        "La restauración apoteósica de la santa misa tradicional del Santo Altar",
        "Las apariciones de La Salette y la purificación divina del clero",
        "Venerable Holzhauser: El Quinto Estado de Consolación Temporal"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Scriptorium de Crónicas",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Escribe o selecciona un tema profético tradicional. Don Aurelio hilvanará una crónica teológica pre-conciliar mediante la teología patrística tradicional.",
            fontSize = 12.sp,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (generationState) {
            is GenerationUiState.Loading -> {
                LiturgicalLoader()
            }
            is GenerationUiState.Success -> {
                LaunchedEffect(Unit) {
                    onBackToPlayer()
                    onClearState()
                }
            }
            is GenerationUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .background(ElegantRedAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .border(1.dp, ElegantRedAccent, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ElegantRedAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tribulación Mecánica",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = ElegantRedAccent,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = generationState.message,
                            fontFamily = FontFamily.Serif,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onClearState,
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantRedAccent),
                            shape = AbsoluteCutCornerShape(4.dp)
                        ) {
                            Text("Reintentar con piedad", fontFamily = FontFamily.Serif, fontSize = 12.sp)
                        }
                    }
                }
            }
            is GenerationUiState.Idle -> {
                // Topic Field
                Text(
                    text = "TEMA DOCTRINAL PROFÉTICO",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = topicQuery,
                    onValueChange = { topicQuery = it },
                    placeholder = {
                        Text(
                            "e.g., El Gran Monarca reinstaurando el cetro regio...",
                            fontFamily = FontFamily.Serif,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (topicQuery.isNotBlank()) {
                            onGenerate(topicQuery)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = AbsoluteCutCornerShape(8.dp),
                    enabled = topicQuery.isNotBlank()
                ) {
                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "INVOCAR CRÓNICA PROFÉTICA",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Pre-designed suggestions
                Text(
                    text = "TEMAS SUGERIDOS POR EL SCRIPTORIUM",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                suggestions.forEach { suggestion ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { topicQuery = suggestion },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SacredCrossIcon(modifier = Modifier.size(14.dp), color = ElegantGoldAccent)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = suggestion,
                                fontFamily = FontFamily.Serif,
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// ------------------- AUX COMPOSABLE: CATHEDRAL WINDOW COVER ART -------------------
@Composable
fun CathedralArchCover(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "halo_glow")
    val alphaGlow by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 82.dp, topEnd = 82.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
            .background(ElegantDarkSurface)
            .border(
                BorderStroke(2.dp, Brush.verticalGradient(listOf(ElegantGoldAccent, ElegantDarkHighlight))),
                RoundedCornerShape(topStart = 82.dp, topEnd = 82.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            )
    ) {
        val width = size.width
        val height = size.height

        // Inner Gothic Arch decoration
        val path = Path().apply {
            moveTo(16f, height)
            lineTo(16f, height * 0.45f)
            // Draw pointed arch
            cubicTo(
                16f, height * 0.1f,
                width * 0.35f, 16f,
                width / 2, 16f
            )
            cubicTo(
                width * 0.65f, 16f,
                width - 16f, height * 0.1f,
                width - 16f, height * 0.45f
            )
            lineTo(width - 16f, height)
            close()
        }
        drawPath(
            path = path,
            color = ElegantGoldAccent.copy(alpha = 0.25f),
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw a glowing sun halo in the center
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(ElegantGoldAccent.copy(alpha = alphaGlow), Color.Transparent)
            ),
            radius = width * 0.35f,
            center = Offset(width / 2, height * 0.45f)
        )

        // Draw Roman Cross
        val crossYCenter = height * 0.45f
        val crossXCenter = width / 2
        val crossHLength = width * 0.32f
        val crossVLength = height * 0.4f

        // Vertical Bar
        drawLine(
            color = ElegantGoldAccent,
            start = Offset(crossXCenter, crossYCenter - crossVLength / 2),
            end = Offset(crossXCenter, crossYCenter + crossVLength / 2),
            strokeWidth = 6.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Horizontal Bar
        drawLine(
            color = ElegantGoldAccent,
            start = Offset(crossXCenter - crossHLength / 2, crossYCenter - crossVLength * 0.1f),
            end = Offset(crossXCenter + crossHLength / 2, crossYCenter - crossVLength * 0.1f),
            strokeWidth = 6.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Delicate inner cross accents
        drawLine(
            color = ElegantRedAccent,
            start = Offset(crossXCenter, crossYCenter - crossVLength / 2 + 3f),
            end = Offset(crossXCenter, crossYCenter + crossVLength / 2 - 3f),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}

// ------------------- AUX COMPOSABLE: COHESTIVE LITURGICAL PULSING WAVEFORM -------------------
@Composable
fun PulsingWaveform(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_pulsing")
    
    val heightScaleColumn = (1..6).map { i ->
        infiniteTransition.animateFloat(
            initialValue = 0.15f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(400 + (i * 120), easing = FastOutLinearInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale_$i"
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heightScaleColumn.forEach { itemScale ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(itemScale.value)
                    .background(ElegantGoldAccent, RoundedCornerShape(1.dp))
            )
        }
    }
}

// ------------------- AUX COMPOSABLE: SACRED LATIN CROSS VECTOR ICON -------------------
@Composable
fun SacredCrossIcon(modifier: Modifier = Modifier, color: Color = ElegantGoldAccent) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cX = w / 2
        val cY = h * 0.42f
        
        // Vertical beam
        drawLine(
            color = color,
            start = Offset(cX, h * 0.05f),
            end = Offset(cX, h * 0.95f),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Square
        )
        // Horizontal beam
        drawLine(
            color = color,
            start = Offset(w * 0.15f, cY),
            end = Offset(w * 0.85f, cY),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Square
        )
        
        // Elegant diamond motif in center of cross intersection
        val path = Path().apply {
            moveTo(cX, cY - 4.dp.toPx())
            lineTo(cX + 4.dp.toPx(), cY)
            lineTo(cX, cY + 4.dp.toPx())
            lineTo(cX - 4.dp.toPx(), cY)
            close()
        }
        drawPath(path = path, color = color)
    }
}

// ------------------- AUX COMPOSABLE: HIGHLIGHTED SCROLLING TRANSCRIPT -------------------
@Composable
fun ScrollingTranscript(
    scriptText: String,
    currentParagraphIndex: Int,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val paragraphs = remember(scriptText) {
        scriptText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
    }
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Smooth autoscroll window to highlight the vocalizing section
    LaunchedEffect(currentParagraphIndex) {
        if (isPlaying && currentParagraphIndex in paragraphs.indices) {
            scope.launch {
                listState.animateScrollToItem(index = currentParagraphIndex)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(paragraphs.size) { index ->
            val isCurrent = isPlaying && index == currentParagraphIndex
            val textAlpha by animateFloatAsState(
                targetValue = if (isCurrent) 1f else if (isPlaying) 0.45f else 0.85f,
                label = "text_fade"
            )
            val textColor = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(textAlpha)
                    .background(
                        if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        else Color.Transparent,
                        RoundedCornerShape(6.dp)
                    )
                    .padding(8.dp)
            ) {
                if (isCurrent) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        SacredCrossIcon(modifier = Modifier.size(10.dp), color = ElegantGoldAccent)
                        Text(
                            text = "Vocalizando...",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = ElegantGoldAccent,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                
                Text(
                    text = paragraphs[index],
                    fontFamily = FontFamily.Serif,
                    fontSize = 13.5.sp,
                    lineHeight = 19.5.sp,
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                    color = textColor,
                )
            }
        }
    }
}

// ------------------- AUX COMPOSABLE: LITURGICAL LOADER WITH LATIN PHRASES -------------------
@Composable
fun LiturgicalLoader(modifier: Modifier = Modifier) {
    val phrases = listOf(
        "\"Spera in Deo, quoniam adhuc confitebor illi.\"",
        "\"Sicut desiderat cervus ad fontes aquarum...\"",
        "\"Regnum coelorum violentiam patitur et violenti rapiunt illud.\"",
        "\"Laudetur Iesus Christus in saecula saeculorum.\"",
        "\"Don Aurelio está consultando las sagradas profecías tradicionales...\"",
        "\"Redactando crónicas del Gran Monarca y el Pastor Angelicus...\""
    )
    
    var currentPhraseIdx by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(3800)
            currentPhraseIdx = (currentPhraseIdx + 1) % phrases.size
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "loader_anim")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .rotate(angle),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw a beautiful medieval sun rosette
                val radius = size.width / 2
                val center = Offset(radius, radius)
                
                // Rotative outer rays
                for (i in 0 until 12) {
                    val rotAngle = (i * (360f / 12)) * (Math.PI / 180f)
                    val rayStart = Offset(
                        (center.x + (radius * 0.45f) * Math.cos(rotAngle)).toFloat(),
                        (center.y + (radius * 0.45f) * Math.sin(rotAngle)).toFloat()
                    )
                    val rayEnd = Offset(
                        (center.x + (radius * 0.9f) * Math.cos(rotAngle)).toFloat(),
                        (center.y + (radius * 0.9f) * Math.sin(rotAngle)).toFloat()
                    )
                    drawLine(
                        color = ElegantGoldAccent,
                        start = rayStart,
                        end = rayEnd,
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }
                
                // Outer gold crown ring
                drawCircle(
                    color = ElegantGoldAccent,
                    radius = radius * 0.45f,
                    style = Stroke(width = 4f)
                )
                // Inner ruby halo core
                drawCircle(
                    color = ElegantDarkBg,
                    radius = radius * 0.25f
                )
            }
            // Inner mini cross
            SacredCrossIcon(modifier = Modifier.size(16.dp), color = ElegantTextColor)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "REDACTANDO GUION PRE-CONCILIAR",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.2.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Changing latin spiritual phrases
        AnimatedContent(
            targetState = phrases[currentPhraseIdx],
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
            },
            label = "phrase_slide"
        ) { text ->
            Text(
                text = text,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Light,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Don Aurelio está tejiendo con esmero el podcast. Esperando la iluminación del Scriptorium sagrado...",
            fontSize = 10.5.sp,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

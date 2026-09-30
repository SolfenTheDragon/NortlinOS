package com.nortlinos.wearos.presentation.screen

import android.app.Activity
import android.app.RemoteInput
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Forward30
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay30
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.HorizontalPageIndicator
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.ui.platform.LocalContext
import com.nortlinos.wearos.presentation.viewmodel.OutputPrompt
import com.nortlinos.wearos.presentation.viewmodel.PlaybackPosition
import com.nortlinos.wearos.presentation.viewmodel.ChapterOption
import androidx.wear.compose.material3.TitleCard
import androidx.wear.compose.material3.ConfirmationDialogDefaults
import androidx.wear.compose.material3.confirmationDialogCurvedText
import androidx.wear.compose.foundation.CurvedScope
import androidx.wear.compose.material3.FailureConfirmationDialog
import androidx.wear.compose.material3.SuccessConfirmationDialog
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.placeholderShimmer
import androidx.wear.compose.material3.rememberPlaceholderState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImagePainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.geometry.isUnspecified
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material.icons.automirrored.filled.List
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonColors
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.OutlinedIconButton
import androidx.wear.compose.material3.Picker
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.material3.rememberPickerState
import androidx.wear.input.RemoteInputIntentHelper
import com.nortlinos.wearos.R
import com.nortlinos.wearos.data.local.DownloadStatus
import com.nortlinos.wearos.data.local.ProgressDisplayMode
import com.nortlinos.wearos.presentation.viewmodel.LibraryViewModel
import com.nortlinos.wearos.presentation.viewmodel.LibraryBrowseState
import com.nortlinos.wearos.presentation.viewmodel.LibrarySeriesSearch
import com.nortlinos.wearos.presentation.viewmodel.LoginViewModel
import com.nortlinos.wearos.presentation.viewmodel.PlaybackSpeedFormat
import com.nortlinos.wearos.presentation.viewmodel.PlayerViewModel
import com.nortlinos.wearos.presentation.viewmodel.SettingsViewModel
import com.nortlinos.wearos.service.SleepTimerState
import com.nortlinos.wearos.service.SleepTimerMath
import com.nortlinos.wearos.data.api.ApiClient
import java.util.concurrent.TimeUnit
import android.net.Uri
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    hasDownloads: Boolean,
    onListenOffline: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val serverInput = textInput("Server URL", viewModel::serverUrl)
    val usernameInput = textInput("Username", viewModel::username)
    val passwordInput = textInput("Password", viewModel::password)
    // A brief failure animation makes a failed attempt unmistakable; the reason stays in the list.
    val showFailure = remember { mutableStateOf(false) }
    LaunchedEffect(state.error) {
        if (state.error != null) showFailure.value = true
    }
    FailureConfirmationDialog(
        visible = showFailure.value,
        onDismissRequest = { showFailure.value = false },
        curvedText = confirmationText("Sign-in failed")
    )

    WearList {
        item { ScreenTitle("Connect") }
        item { SupportingText("Sign in to your Audiobookshelf server") }
        item { InputChip("Server URL", state.serverUrl, serverInput) }
        item { InputChip("Username", state.username, usernameInput) }
        item { InputChip("Password", if (state.password.isBlank()) "" else "••••••", passwordInput) }
        state.error?.let { message ->
            item { StatusText(message, MaterialTheme.colorScheme.error) }
        }
        item {
            if (state.loading) {
                CircularProgressIndicator(modifier = Modifier.padding(12.dp).size(32.dp))
            } else {
                WearChip(
                    onClick = viewModel::login,
                    label = { Text("Sign in") },
                    icon = { Icon(Icons.AutoMirrored.Filled.Login, null) },
                    style = ChipStyle.PRIMARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        if (state.openIdAvailable) {
            item {
                if (state.openIdInProgress) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.padding(8.dp).size(32.dp))
                        SupportingText("Continue on your phone or browser")
                    }
                } else {
                    WearChip(
                        onClick = viewModel::loginWithOpenId,
                        label = { Text(state.openIdLabel) },
                        secondaryLabel = { Text("Opens on your phone") },
                        icon = { Icon(Icons.Default.Lock, null) },
                        style = ChipStyle.SECONDARY,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        if (hasDownloads) {
            item {
                WearChip(
                    onClick = onListenOffline,
                    label = { Text("Listen offline") },
                    secondaryLabel = { Text("Open downloaded books") },
                    icon = { Icon(Icons.Default.Download, null) },
                    style = ChipStyle.SECONDARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    settingsViewModel: SettingsViewModel,
    onLibraries: () -> Unit,
    onPodcasts: () -> Unit,
    onDownloaded: () -> Unit,
    onSearch: () -> Unit,
    onRecent: () -> Unit,
    onNowPlaying: () -> Unit,
    onSettings: () -> Unit
) {
    val player by playerViewModel.nowPlaying.collectAsState()
    val server by libraryViewModel.sessionRepository.session.collectAsState()
    val online by libraryViewModel.online.collectAsState()
    val searchVisible by settingsViewModel.homeSearchVisible.collectAsState()
    val podcastsVisible by settingsViewModel.homePodcastsVisible.collectAsState()
    WearList {
        item { ScreenTitle(stringResource(R.string.app_name)) }
        player.itemId?.let {
            item {
                NowPlayingChip(
                    title = player.title,
                    playing = player.playing,
                    originServerUrl = player.originServerUrl,
                    onClick = onNowPlaying
                )
            }
        }
        item {
            ConnectionStatus(
                online = online,
                serverUrl = server?.url,
                offlineDetail = "Cached library and downloads remain available"
            )
        }
        item {
            NavigationChip(
                "Recent books",
                "Play or download a recently played book",
                Icons.Default.PlayArrow,
                onRecent
            )
        }
        // Downloads come first: they play offline and cost far less battery than streaming.
        item {
            NavigationChip(
                "Downloaded",
                "Books and podcast episodes stored on this watch",
                Icons.Default.Download,
                onDownloaded
            )
        }
        item {
            NavigationChip(
                "Server library",
                "Browse books available to download",
                Icons.AutoMirrored.Filled.LibraryBooks,
                onLibraries
            )
        }
        if (podcastsVisible) {
            item {
                NavigationChip(
                    "Podcasts",
                    "Browse and listen to podcast episodes",
                    Icons.AutoMirrored.Filled.LibraryBooks,
                    onPodcasts
                )
            }
        }
        if (searchVisible) {
            item { NavigationChip("Search", "Search the local cache", Icons.Default.Search, onSearch) }
        }
        item { NavigationChip("Settings", "Progress display and account", Icons.Default.Settings, onSettings) }
    }
}

@Composable
fun RecentPlaybackScreen(
    libraryViewModel: LibraryViewModel,
    onItem: (String, String) -> Unit
) {
    val recent by libraryViewModel.recentPlayback.collectAsState()
    val online by libraryViewModel.online.collectAsState()
    val server by libraryViewModel.activeSession.collectAsState()
    WearList {
        item { ScreenTitle("Recent books") }
        item {
            SupportingText(
                if (online) "Recently played on this server"
                else "Downloaded books available offline"
            )
        }
        recent.error?.let { error ->
            item { StatusText(error, MaterialTheme.colorScheme.error) }
        }
        if (recent.books.isEmpty()) {
            item {
                EmptyState(
                    if (recent.error == null) "No recent books" else "Recent books unavailable",
                    if (recent.error != null) "Return and try again."
                    else if (online) "Start a book to see it here."
                    else "No recently played downloads are available."
                )
            }
        } else {
            items(
                recent.books,
                key = { "${it.originServerUrl}:${it.itemId}" }
            ) { book ->
                val matchingServer = server?.takeIf {
                    com.nortlinos.wearos.data.repository.ServerIdentity.matches(
                        it.url,
                        book.originServerUrl
                    )
                }
                BookChip(
                    title = book.title,
                    secondary = listOfNotNull(
                        book.author,
                        "${time(book.positionMs)} / ${time(book.durationMs)}"
                    ).joinToString(" · "),
                    baseUrl = matchingServer?.url,
                    coverPath = ApiClient.coverSource(
                        book.localCoverPath,
                        book.coverPath,
                        book.itemId
                    ),
                    authToken = matchingServer?.token,
                    status = if (book.downloaded) DownloadStatus.DOWNLOADED else null,
                    onClick = { onItem(book.itemId, book.originServerUrl) },
                    onLongClick = downloadLongPressAction(
                        status = if (book.downloaded) DownloadStatus.DOWNLOADED else null,
                        onStart = { libraryViewModel.startDownload(book.itemId) },
                        onPause = {}
                    )
                )
            }
        }
    }
}

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    podcastsOnly: Boolean,
    onLibrary: (String, String) -> Unit
) {
    val libraries by viewModel.libraries.collectAsState()
    val visibleLibraries = libraries.filter { (it.mediaType == "podcast") == podcastsOnly }
    val error by viewModel.error.collectAsState()
    val refreshing by viewModel.refreshingLibraries.collectAsState()
    LaunchedEffect(Unit) { viewModel.refreshLibraries() }
    WearList {
        item { ScreenTitle(if (podcastsOnly) "Podcast libraries" else "Book libraries") }
        error?.let { message ->
            item { StatusText(message, MaterialTheme.colorScheme.error) }
            item {
                WearChip(
                    onClick = viewModel::refreshLibraries,
                    label = { Text("Retry server") },
                    icon = { Icon(Icons.Default.Refresh, null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        items(visibleLibraries, key = { "${it.originServerUrl}:${it.id}" }) { library ->
            NavigationChip(
                label = library.name,
                secondary = library.mediaType.replaceFirstChar { it.uppercase() },
                icon = Icons.AutoMirrored.Filled.LibraryBooks,
                onClick = { onLibrary(library.id, library.originServerUrl) }
            )
        }
        if (visibleLibraries.isEmpty() && refreshing) {
            item { CircularProgressIndicator(modifier = Modifier.padding(12.dp).size(32.dp)) }
        } else if (visibleLibraries.isEmpty()) {
            item {
                EmptyState(
                    if (podcastsOnly) "No podcast libraries" else "No book libraries",
                    "Retry while connected to load your catalog."
                )
            }
        }
    }
}

@Composable
fun LibraryItemsScreen(
    libraryId: String,
    originServerUrl: String,
    viewModel: LibraryViewModel,
    onItem: (String) -> Unit,
    onNowPlaying: () -> Unit,
    playerViewModel: PlayerViewModel,
    settingsViewModel: SettingsViewModel
) {
    val seriesViewDefault by settingsViewModel.seriesView.collectAsState()
    val libraries by viewModel.libraries.collectAsState()
    val isPodcastLibrary = libraries.firstOrNull {
        it.id == libraryId &&
            com.nortlinos.wearos.data.repository.ServerIdentity.matches(
                it.originServerUrl,
                originServerUrl
            )
    }?.mediaType == "podcast"
    val showSeries = remember(libraryId, originServerUrl) {
        mutableStateOf(seriesViewDefault && !isPodcastLibrary)
    }
    // Picking up a later settings change keeps the preference authoritative without overriding a
    // manual toggle made while this screen is open.
    LaunchedEffect(libraryId, originServerUrl, seriesViewDefault, isPodcastLibrary) {
        showSeries.value = seriesViewDefault && !isPodcastLibrary
    }
    val selectedSeriesName = remember(libraryId, originServerUrl) { mutableStateOf<String?>(null) }
    val browse by viewModel.libraryBrowse.collectAsState()
    val seriesBrowse by viewModel.librarySeries.collectAsState()
    val seriesDownload by viewModel.seriesDownload.collectAsState()
    val activeBrowse = if (
        browse.libraryId == libraryId &&
        browse.originServerUrl == com.nortlinos.wearos.data.repository.ServerIdentity.normalize(
            originServerUrl
        )
    ) browse else LibraryBrowseState()
    val books = activeBrowse.items
    val downloads by viewModel.downloadStatuses.collectAsState()
    val server by viewModel.sessionRepository.session.collectAsState()
    val player by playerViewModel.nowPlaying.collectAsState()
    val statuses = remember(downloads) {
        downloads.associate { (it.itemId to it.originServerUrl) to it.status }
    }
    val searchInput = textInput("Search this library") {
        viewModel.searchLibrary(libraryId, originServerUrl, it)
    }
    val seriesQuery = remember(libraryId, originServerUrl) { mutableStateOf("") }
    val seriesSearchInput = textInput("Search series") { seriesQuery.value = it }
    val listState = rememberTransformingLazyColumnState()
    LaunchedEffect(libraryId, originServerUrl) {
        viewModel.openLibrary(libraryId, originServerUrl)
    }
    LaunchedEffect(libraryId, originServerUrl, showSeries.value, isPodcastLibrary) {
        if (showSeries.value && !isPodcastLibrary) viewModel.loadSeries(libraryId, originServerUrl)
    }
    DisposableEffect(libraryId, originServerUrl, viewModel) {
        onDispose { viewModel.cancelSeriesLoad(libraryId, originServerUrl) }
    }
    val activeSeries = if (
        seriesBrowse.libraryId == libraryId &&
        seriesBrowse.originServerUrl == com.nortlinos.wearos.data.repository.ServerIdentity.normalize(
            originServerUrl
        )
    ) seriesBrowse else com.nortlinos.wearos.presentation.viewmodel.LibrarySeriesState()
    val selectedSeries = activeSeries.series.firstOrNull { it.name == selectedSeriesName.value }
    val visibleSeries = remember(activeSeries.series, seriesQuery.value) {
        LibrarySeriesSearch.filter(activeSeries.series, seriesQuery.value)
    }

    WearList(listState = listState) {
        if (!isPodcastLibrary) item {
            WearChip(
                onClick = {
                    val nextShowSeries = !showSeries.value
                    showSeries.value = nextShowSeries
                    if (!nextShowSeries) {
                        viewModel.cancelSeriesLoad(libraryId, originServerUrl)
                        seriesQuery.value = ""
                    }
                    selectedSeriesName.value = null
                },
                label = { Text(if (showSeries.value) "Show books" else "Show series", maxLines = 1) },
                secondaryLabel = { Text(if (showSeries.value) "Browse individual books" else "Group books into series") },
                icon = {
                    Icon(
                        if (showSeries.value) Icons.AutoMirrored.Filled.LibraryBooks else Icons.AutoMirrored.Filled.List,
                        null
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (!showSeries.value) {
            item { ScreenTitle(if (isPodcastLibrary) "Podcasts" else "Server books") }
            item {
                WearChip(
                    onClick = searchInput,
                    label = {
                        Text(
                            activeBrowse.query.ifBlank {
                                if (isPodcastLibrary) "Search podcasts" else "Search this library"
                            },
                            maxLines = 1
                        )
                    },
                    secondaryLabel = { Text("Title, author, series, narrator") },
                    icon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            player.itemId?.let {
                item {
                    NowPlayingChip(
                        player.title,
                        player.playing,
                        player.originServerUrl,
                        onNowPlaying
                    )
                }
            }
            itemsIndexed(books, key = { _, book -> book.id }) { index, book ->
                if (LibraryPaging.shouldLoadNext(
                        visibleBookIndex = index,
                        loadedBooks = books.size,
                        hasMore = activeBrowse.hasMore,
                        loading = activeBrowse.loading,
                        searching = activeBrowse.query.isNotBlank()
                    )
                ) {
                    LaunchedEffect(index, activeBrowse.page) { viewModel.loadNextLibraryPage() }
                }
                BookChip(
                    title = book.title,
                    secondary = book.author ?: book.series
                        ?: if (isPodcastLibrary) "Podcast" else "Unknown author",
                    baseUrl = server?.url,
                    coverPath = ApiClient.coverSource(book.localCoverPath, book.coverPath, book.id),
                    authToken = server?.token,
                    status = statuses[book.id to book.originServerUrl],
                    onClick = { onItem(book.id) },
                    // Podcast libraries list shows here, not individually downloadable items;
                    // only offer long-press download for actual books.
                    onLongClick = if (isPodcastLibrary) null else downloadLongPressAction(
                        status = statuses[book.id to book.originServerUrl],
                        onStart = { viewModel.startDownload(book.id) },
                        onPause = { viewModel.pauseDownload(book.id, originServerUrl) }
                    )
                )
            }
            if (activeBrowse.loading) {
                item { CircularProgressIndicator(modifier = Modifier.padding(12.dp).size(32.dp)) }
            }
            activeBrowse.error?.let { message ->
                item { StatusText(message, MaterialTheme.colorScheme.error) }
                if (activeBrowse.query.isBlank()) {
                    item {
                        WearChip(
                            onClick = viewModel::retryLibraryPage,
                            label = { Text("Retry loading books") },
                            icon = { Icon(Icons.Default.Refresh, null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            if (activeBrowse.query.isNotBlank()) {
                item {
                    WearChip(
                        onClick = { viewModel.searchLibrary(libraryId, originServerUrl, "") },
                        label = { Text("Clear search") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            if (books.isEmpty() && !activeBrowse.loading) {
                item {
                    EmptyState(
                        if (activeBrowse.query.isBlank()) "No books" else "No matches",
                        if (activeBrowse.query.isBlank()) "This library has no available books."
                        else "Try another title, author, series, or narrator."
                    )
                }
            }
        } else if (selectedSeries == null) {
            item { ScreenTitle("Series") }
            item {
                WearChip(
                    onClick = seriesSearchInput,
                    label = { Text(seriesQuery.value.ifBlank { "Search series" }, maxLines = 1) },
                    secondaryLabel = { Text("Series name, title, author, narrator") },
                    icon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (seriesQuery.value.isNotBlank()) {
                item {
                    WearChip(
                        onClick = { seriesQuery.value = "" },
                        label = { Text("Clear search") },
                        secondaryLabel = {
                            Text("${visibleSeries.size} of ${activeSeries.series.size} series")
                        },
                        icon = { Icon(Icons.Default.Close, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            activeSeries.error?.let { message ->
                item {
                    StatusText(
                        "$message. Cached series may be incomplete; bulk download stays unavailable until the full library loads.",
                        MaterialTheme.colorScheme.error
                    )
                }
                item {
                    WearChip(
                        onClick = { viewModel.loadSeries(libraryId, originServerUrl) },
                        label = { Text("Retry loading series") },
                        icon = { Icon(Icons.Default.Refresh, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            if (activeSeries.loading) {
                item {
                    SupportingText(
                        if (activeSeries.totalItemCount > 0) {
                            "Loading ${activeSeries.loadedItemCount} of ${activeSeries.totalItemCount} books…"
                        } else if (activeSeries.series.isEmpty()) {
                            "Loading all books to find series…"
                        } else {
                            "Loaded ${activeSeries.loadedItemCount} books; checking for more…"
                        }
                    )
                }
                item { CircularProgressIndicator(modifier = Modifier.padding(12.dp).size(32.dp)) }
            }
            items(visibleSeries, key = { it.name }) { series ->
                WearChip(
                    onClick = { selectedSeriesName.value = series.name },
                    label = { Text(series.name, maxLines = 2) },
                    secondaryLabel = {
                        Text("${series.books.size} ${if (series.books.size == 1) "book" else "books"}")
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.LibraryBooks, null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (visibleSeries.isEmpty() && !activeSeries.loading && activeSeries.error == null) {
                item {
                    if (seriesQuery.value.isNotBlank()) {
                        EmptyState(
                            "No matches",
                            "No series matched \"${seriesQuery.value}\". Try another series, title, author, or narrator."
                        )
                    } else {
                        EmptyState(
                            "No series found",
                            "This library has no books with series information."
                        )
                    }
                }
            }
        } else {
            item {
                WearChip(
                    onClick = { selectedSeriesName.value = null },
                    label = { Text("All series") },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item { ScreenTitle(selectedSeries.name) }
            val seriesStatus = seriesDownload.takeIf {
                it.libraryId == libraryId &&
                    it.originServerUrl == com.nortlinos.wearos.data.repository.ServerIdentity.normalize(
                        originServerUrl
                    ) &&
                    it.seriesName == selectedSeries.name
            }
            val bulkQueueing = seriesDownload.inProgress
            val completedCount = selectedSeries.books.count {
                statuses[it.id to it.originServerUrl] == DownloadStatus.DOWNLOADED
            }
            item {
                if (activeSeries.loaded) {
                    WearChip(
                        onClick = {
                            viewModel.downloadSeries(
                                libraryId,
                                selectedSeries.name,
                                selectedSeries.books.map { it.id },
                                originServerUrl
                            )
                        },
                        label = {
                            Text(
                                if (bulkQueueing) "Queueing ${seriesDownload.seriesName}…"
                                else if (completedCount == selectedSeries.books.size) "All books downloaded"
                                else "Download all books"
                            )
                        },
                        secondaryLabel = {
                            Text(
                                if (bulkQueueing) {
                                    "${seriesDownload.queued + seriesDownload.failed} of ${seriesDownload.total} queued"
                                } else seriesStatus?.let {
                                    if (it.inProgress) "${it.queued + it.failed} of ${it.total} queued"
                                    else "${it.queued} queued · ${it.skipped} already downloaded or active"
                                } ?: "$completedCount of ${selectedSeries.books.size} downloaded"
                            )
                        },
                        icon = { Icon(Icons.Default.Download, null) },
                        style = ChipStyle.PRIMARY,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !bulkQueueing &&
                            completedCount < selectedSeries.books.size
                    )
                } else {
                    SupportingText("Download all is available after the complete series list loads.")
                }
            }
            seriesStatus?.error?.let { message ->
                item {
                    StatusText(
                        "Some downloads could not be queued: $message",
                        MaterialTheme.colorScheme.error
                    )
                }
            }
            if (activeSeries.loading) {
                item { CircularProgressIndicator(modifier = Modifier.padding(12.dp).size(32.dp)) }
            }
            items(
                selectedSeries.books,
                key = { it.id }
            ) { book ->
                BookChip(
                    title = book.title,
                    secondary = book.author ?: book.series ?: "Unknown author",
                    baseUrl = server?.url,
                    coverPath = ApiClient.coverSource(book.localCoverPath, book.coverPath, book.id),
                    authToken = server?.token,
                    status = statuses[book.id to book.originServerUrl],
                    onClick = { onItem(book.id) },
                    onLongClick = downloadLongPressAction(
                        status = statuses[book.id to book.originServerUrl],
                        onStart = { viewModel.startDownload(book.id) },
                        onPause = { viewModel.pauseDownload(book.id, originServerUrl) }
                    )
                )
            }
        }
    }
}

@Composable
fun BookDetailScreen(
    itemId: String,
    originServerUrl: String,
    viewModel: LibraryViewModel,
    onPlay: (String) -> Unit,
    onEpisode: (String) -> Unit = {}
) {
    val itemFlow = remember(itemId, originServerUrl) {
        viewModel.item(itemId, originServerUrl)
    }
    val downloadFlow = remember(itemId, originServerUrl) {
        viewModel.download(itemId, originServerUrl)
    }
    val episodeFlow = remember(itemId, originServerUrl) {
        viewModel.podcastEpisodes(itemId, originServerUrl)
    }
    val item by itemFlow.collectAsState(initial = null)
    val download by downloadFlow.collectAsState(initial = null)
    val episodes by episodeFlow.collectAsState(initial = emptyList())
    val downloadStatuses by viewModel.downloadStatuses.collectAsState()
    val podcastSeriesDownload by viewModel.podcastSeriesDownload.collectAsState()
    val online by viewModel.online.collectAsState()
    val server by viewModel.sessionRepository.session.collectAsState()
    val refreshingItemId by viewModel.refreshingItemId.collectAsState()
    val confirmDelete = remember { mutableStateOf(false) }
    val confirmCancel = remember { mutableStateOf(false) }
    val confirmPodcastSeriesDelete = remember { mutableStateOf(false) }
    LaunchedEffect(itemId, originServerUrl) {
        viewModel.refreshItem(itemId, originServerUrl)
    }
    val isPodcast = item?.mediaType == "podcast"
    if (!isPodcast) DownloadOutcomeConfirmation(download?.status)
    val podcastSeriesOperation = podcastSeriesDownload.takeIf {
        it.podcastId == itemId &&
            com.nortlinos.wearos.data.repository.ServerIdentity.matches(
                it.originServerUrl.orEmpty(),
                originServerUrl
            )
    }
    val podcastEpisodeDownloads = downloadStatuses.filter { status ->
        episodes.any { it.id == status.itemId } &&
            com.nortlinos.wearos.data.repository.ServerIdentity.matches(
                status.originServerUrl,
                originServerUrl
            )
    }
    val downloadedEpisodeCount = podcastEpisodeDownloads.count {
        it.status == DownloadStatus.DOWNLOADED
    }
    val allEpisodesScheduled = episodes.isNotEmpty() &&
        podcastEpisodeDownloads.count {
            it.status in setOf(
                DownloadStatus.QUEUED,
                DownloadStatus.DOWNLOADING,
                DownloadStatus.DOWNLOADED
            )
        } == episodes.size
    val hasEpisodeDownloads = podcastEpisodeDownloads.isNotEmpty()

    AlertDialog(
        visible = isPodcast && podcastSeriesOperation?.requiresSpaceConfirmation == true,
        onDismissRequest = viewModel::dismissPodcastSeriesSpaceWarning,
        icon = { Icon(Icons.Default.CloudDownload, null) },
        title = {
            Text(
                when {
                    podcastSeriesOperation != null &&
                        podcastSeriesOperation.requiredBytes > podcastSeriesOperation.availableBytes ->
                        "Not enough space"
                    podcastSeriesOperation != null && podcastSeriesOperation.unknownSizeCount > 0 ->
                        "Episode sizes unknown"
                    else -> "Check available space"
                },
                textAlign = TextAlign.Center
            )
        },
        text = {
            val operation = podcastSeriesOperation
            val storageWarning = if (
                operation != null && operation.requiredBytes > operation.availableBytes
            ) {
                "About ${formatBytes(operation.requiredBytes)} is needed, but only " +
                    "${formatBytes(operation.availableBytes)} is free."
            } else {
                "Known-size episodes need about ${formatBytes(operation?.requiredBytes ?: 0)}; " +
                    "the watch reports ${formatBytes(operation?.availableBytes ?: 0)} free."
            }
            val unknownWarning = operation?.unknownSizeCount
                ?.takeIf { it > 0 }
                ?.let { " The size of $it episode${if (it == 1) " is" else "s are"} unknown." }
                .orEmpty()
            Text(
                "$storageWarning$unknownWarning You can continue, but some downloads may fail.",
                textAlign = TextAlign.Center
            )
        }
    ) {
        item {
            WearChip(
                onClick = viewModel::confirmPodcastSeriesDownload,
                label = { Text("Download anyway") },
                icon = { Icon(Icons.Default.Download, null) },
                style = ChipStyle.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            WearChip(
                onClick = viewModel::dismissPodcastSeriesSpaceWarning,
                label = { Text("Cancel") },
                style = ChipStyle.SECONDARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    AlertDialog(
        visible = isPodcast && confirmPodcastSeriesDelete.value,
        onDismissRequest = { confirmPodcastSeriesDelete.value = false },
        icon = { Icon(Icons.Default.Delete, null) },
        title = { Text("Delete all episodes?", textAlign = TextAlign.Center) },
        text = {
            Text(
                "This removes all episode downloads for this podcast from the watch.",
                textAlign = TextAlign.Center
            )
        }
    ) {
        item {
            WearChip(
                onClick = {
                    viewModel.deletePodcastSeriesDownloads(
                        podcastId = itemId,
                        seriesName = item?.title ?: "Podcast",
                        episodeIds = episodes.map { it.id },
                        originServerUrl = originServerUrl
                    )
                    confirmPodcastSeriesDelete.value = false
                },
                label = { Text("Delete all downloads") },
                icon = { Icon(Icons.Default.Delete, null) },
                style = ChipStyle.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            WearChip(
                onClick = { confirmPodcastSeriesDelete.value = false },
                label = { Text("Keep downloads") },
                style = ChipStyle.SECONDARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    WearList {
        item {
            CoverImage(
                baseUrl = server?.url,
                coverPath = ApiClient.coverSource(
                    item?.localCoverPath,
                    item?.coverPath,
                    itemId
                ),
                authToken = server?.token,
                modifier = Modifier.size(72.dp)
            )
        }
        item {
            ScreenTitle(
                item?.title ?: if (isPodcast) "Podcast" else "Book"
            )
        }
        item { SupportingText(item?.author ?: "Unknown author") }
        if (isPodcast) {
            item {
                Text(
                    text = item?.description ?: "No description cached.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
            item { ScreenTitle("Episodes") }
            item {
                WearChip(
                    onClick = {
                        viewModel.downloadPodcastSeries(
                            podcastId = itemId,
                            seriesName = item?.title ?: "Podcast",
                            episodes = episodes,
                            originServerUrl = originServerUrl
                        )
                    },
                    label = {
                        Text(
                            if (podcastSeriesOperation?.inProgress == true &&
                                !podcastSeriesOperation.deleting
                            ) {
                                "Queueing episodes…"
                            } else if (episodes.isNotEmpty() &&
                                downloadedEpisodeCount == episodes.size
                            ) {
                                "All episodes downloaded"
                            } else if (allEpisodesScheduled) {
                                "All episodes queued"
                            } else {
                                "Download all episodes"
                            }
                        )
                    },
                    secondaryLabel = {
                        Text(
                            when {
                                podcastSeriesOperation?.inProgress == true &&
                                    !podcastSeriesOperation.deleting ->
                                    "${podcastSeriesOperation.queued + podcastSeriesOperation.failed} of " +
                                        "${podcastSeriesOperation.total} queued"
                                podcastSeriesOperation?.error != null ->
                                    podcastSeriesOperation.error
                                else -> "$downloadedEpisodeCount of ${episodes.size} downloaded"
                            },
                            maxLines = 1
                        )
                    },
                    icon = { Icon(Icons.Default.Download, null) },
                    style = ChipStyle.PRIMARY,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = episodes.isNotEmpty() &&
                        podcastSeriesOperation?.inProgress != true &&
                        !allEpisodesScheduled
                )
            }
            if (hasEpisodeDownloads) {
                item {
                    WearChip(
                        onClick = { confirmPodcastSeriesDelete.value = true },
                        label = {
                            Text(
                                if (podcastSeriesOperation?.inProgress == true &&
                                    podcastSeriesOperation.deleting
                                ) {
                                    "Deleting episodes…"
                                } else {
                                    "Delete all episode downloads"
                                }
                            )
                        },
                        secondaryLabel = {
                            Text(
                                if (podcastSeriesOperation?.inProgress == true &&
                                    podcastSeriesOperation.deleting
                                ) {
                                    "${podcastSeriesOperation.deleted + podcastSeriesOperation.failed} of " +
                                        "${podcastSeriesOperation.total} removed"
                                } else {
                                    "$downloadedEpisodeCount downloaded"
                                },
                                maxLines = 1
                            )
                        },
                        icon = { Icon(Icons.Default.Delete, null) },
                        style = ChipStyle.SECONDARY,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = podcastSeriesOperation?.inProgress != true
                    )
                }
            }
            podcastSeriesOperation?.error?.let { message ->
                item { StatusText(message, MaterialTheme.colorScheme.error) }
            }
            if (episodes.isEmpty()) {
                item {
                    when {
                        refreshingItemId == itemId ->
                            CircularProgressIndicator(modifier = Modifier.padding(12.dp).size(32.dp))
                        item?.mediaType == "podcast" ->
                            EmptyState(
                                if (online) "No episodes found" else "Episodes not cached",
                                if (online) {
                                    "This podcast has no episodes available."
                                } else {
                                    "Connect to load this show's episodes for offline browsing."
                                }
                            )
                        else -> EmptyState("Loading podcast", "Episode details will appear when available.")
                    }
                }
            }
            itemsIndexed(episodes, key = { _, it -> it.id }) { _, episode ->
                val status = downloadStatuses.firstOrNull {
                    it.itemId == episode.id &&
                        com.nortlinos.wearos.data.repository.ServerIdentity.matches(
                            it.originServerUrl,
                            originServerUrl
                        )
                }
                DownloadOutcomeConfirmation(status?.status)
                WearChip(
                    onClick = { onEpisode(episode.id) },
                    onLongClick = downloadLongPressAction(
                        status = status?.status,
                        onStart = { viewModel.startDownload(episode.id) },
                        onPause = { viewModel.pauseDownload(episode.id, originServerUrl) }
                    ),
                    onLongClickLabel = "Download episode",
                    label = { Text(episode.title, maxLines = 2) },
                    secondaryLabel = {
                        Text(
                            listOfNotNull(
                                episode.author,
                                episode.durationMs.takeIf { it > 0 }?.let(::time),
                                episodeStatusLabel(status?.status, status?.downloadedBytes ?: 0, status?.fileSizeBytes ?: 0)
                            ).joinToString(" · ").ifBlank { "Podcast episode" },
                            maxLines = 1
                        )
                    },
                    icon = { Icon(episodeStatusIcon(status?.status), null) },
                    style = ChipStyle.PRIMARY,
                    colors = episodeChipColors(status?.status),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            // Deliberately a list button rather than an EdgeButton: an EdgeButton stays collapsed
            // until the end of the list, which puts Play below a multi-paragraph description.
            item {
                WearChip(
                    onClick = { onPlay(itemId) },
                    label = { Text("Play or resume") },
                    icon = { Icon(Icons.Default.PlayArrow, null) },
                    style = ChipStyle.PRIMARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                DownloadAction(
                    status = download?.status,
                    downloadedBytes = download?.downloadedBytes ?: 0,
                    totalBytes = download?.fileSizeBytes ?: 0,
                    error = download?.error,
                    onStart = { viewModel.startDownload(itemId) },
                    onPause = { viewModel.pauseDownload(itemId, originServerUrl) },
                    onDelete = {
                        if (confirmDelete.value) {
                            viewModel.deleteDownload(itemId, originServerUrl)
                            confirmDelete.value = false
                        } else {
                            confirmDelete.value = true
                        }
                    },
                    deleteConfirmationPending = confirmDelete.value
                )
            }
            if (download?.status != null && download?.status != DownloadStatus.DOWNLOADED) {
                item {
                    WearChip(
                        onClick = {
                            if (confirmCancel.value) {
                                viewModel.cancelDownload(itemId, originServerUrl)
                                confirmCancel.value = false
                            } else {
                                confirmCancel.value = true
                            }
                        },
                        label = { Text(if (confirmCancel.value) "Tap again to cancel" else "Cancel download") },
                        icon = { Icon(Icons.Default.Delete, null) },
                        style = ChipStyle.SECONDARY,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                when {
                    item == null && refreshingItemId == itemId ->
                        CircularProgressIndicator(modifier = Modifier.padding(12.dp).size(32.dp))
                    else -> Text(
                        text = item?.description ?: "No description cached.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * A single podcast episode: cover, title, podcast/duration, play, download, and description.
 * Reached by a short press on an episode row in [BookDetailScreen]; a long press there instead
 * starts (or pauses) the download directly without leaving the list.
 */
@Composable
fun EpisodeDetailScreen(
    episodeId: String,
    originServerUrl: String,
    viewModel: LibraryViewModel,
    onPlay: (String) -> Unit
) {
    val itemFlow = remember(episodeId, originServerUrl) {
        viewModel.item(episodeId, originServerUrl)
    }
    val downloadFlow = remember(episodeId, originServerUrl) {
        viewModel.download(episodeId, originServerUrl)
    }
    val episode by itemFlow.collectAsState(initial = null)
    val download by downloadFlow.collectAsState(initial = null)
    val server by viewModel.sessionRepository.session.collectAsState()
    val confirmDelete = remember { mutableStateOf(false) }
    val confirmCancel = remember { mutableStateOf(false) }
    DownloadOutcomeConfirmation(download?.status)

    WearList {
        item {
            CoverImage(
                baseUrl = server?.url,
                coverPath = ApiClient.coverSource(
                    episode?.localCoverPath,
                    episode?.coverPath,
                    episodeId
                ),
                authToken = server?.token,
                modifier = Modifier.size(72.dp)
            )
        }
        item { ScreenTitle(episode?.title ?: "Episode") }
        item {
            SupportingText(
                listOfNotNull(
                    // The podcast's title is stashed in `series` when episodes are cached.
                    episode?.series,
                    episode?.durationMs?.takeIf { it > 0 }?.let(::time)
                ).joinToString(" · ").ifBlank { "Podcast episode" }
            )
        }
        item {
            WearChip(
                onClick = { onPlay(episodeId) },
                label = { Text("Play or resume") },
                icon = { Icon(Icons.Default.PlayArrow, null) },
                style = ChipStyle.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            DownloadAction(
                status = download?.status,
                downloadedBytes = download?.downloadedBytes ?: 0,
                totalBytes = download?.fileSizeBytes ?: 0,
                error = download?.error,
                onStart = { viewModel.startDownload(episodeId) },
                onPause = { viewModel.pauseDownload(episodeId, originServerUrl) },
                onDelete = {
                    if (confirmDelete.value) {
                        viewModel.deleteDownload(episodeId, originServerUrl)
                        confirmDelete.value = false
                    } else {
                        confirmDelete.value = true
                    }
                },
                deleteConfirmationPending = confirmDelete.value
            )
        }
        if (download?.status != null && download?.status != DownloadStatus.DOWNLOADED) {
            item {
                WearChip(
                    onClick = {
                        if (confirmCancel.value) {
                            viewModel.cancelDownload(episodeId, originServerUrl)
                            confirmCancel.value = false
                        } else {
                            confirmCancel.value = true
                        }
                    },
                    label = { Text(if (confirmCancel.value) "Tap again to cancel" else "Cancel download") },
                    icon = { Icon(Icons.Default.Delete, null) },
                    style = ChipStyle.SECONDARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            Text(
                text = episode?.description ?: "No description cached.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )
        }
    }
}

/** Curved caption along the bottom of an M3 confirmation, in the recommended style. */
@Composable
private fun confirmationText(text: String): CurvedScope.() -> Unit {
    val style = ConfirmationDialogDefaults.curvedTextStyle
    return { confirmationDialogCurvedText(text, style) }
}

internal enum class DownloadOutcome { COMPLETED, FAILED, DELETED }

internal object DownloadOutcomes {
    /**
     * What, if anything, to confirm when a book's download status changes while its page is
     * open. Only real transitions count: opening the page on an already-finished or failed
     * download shows nothing, and a cancelled download (active to none) needs no fanfare.
     */
    fun of(previous: DownloadStatus?, current: DownloadStatus?): DownloadOutcome? {
        val wasActive = previous == DownloadStatus.DOWNLOADING || previous == DownloadStatus.QUEUED
        return when {
            wasActive && current == DownloadStatus.DOWNLOADED -> DownloadOutcome.COMPLETED
            wasActive && current == DownloadStatus.FAILED -> DownloadOutcome.FAILED
            previous == DownloadStatus.DOWNLOADED && current == null -> DownloadOutcome.DELETED
            else -> null
        }
    }
}

/** Full-screen M3 confirmation for a download finishing, failing, or being deleted. */
@Composable
private fun DownloadOutcomeConfirmation(status: DownloadStatus?) {
    val previous = remember { mutableStateOf(status) }
    val outcome = remember { mutableStateOf<DownloadOutcome?>(null) }
    LaunchedEffect(status) {
        DownloadOutcomes.of(previous.value, status)?.let { outcome.value = it }
        previous.value = status
    }
    val dismiss = { outcome.value = null }
    SuccessConfirmationDialog(
        visible = outcome.value == DownloadOutcome.COMPLETED || outcome.value == DownloadOutcome.DELETED,
        onDismissRequest = dismiss,
        curvedText = confirmationText(
            if (outcome.value == DownloadOutcome.DELETED) "Download deleted" else "Downloaded"
        )
    )
    FailureConfirmationDialog(
        visible = outcome.value == DownloadOutcome.FAILED,
        onDismissRequest = dismiss,
        curvedText = confirmationText("Download failed")
    )
}

@Composable
fun DownloadedScreen(viewModel: LibraryViewModel, onItem: (String, String) -> Unit) {
    val books by viewModel.downloaded.collectAsState()
    val server by viewModel.sessionRepository.session.collectAsState()
    WearList {
        item { ScreenTitle("Downloaded") }
        val grouped = books.groupBy {
            it.download?.originServerUrl?.takeIf(String::isNotBlank) ?: ""
        }.toSortedMap()
        grouped.forEach { (origin, serverBooks) ->
            item {
                SupportingText(
                    if (origin.isBlank()) "Unknown source · local only"
                    else serverLabel(origin)
                )
            }
            items(serverBooks, key = { "${it.download?.originServerUrl}:${it.item.id}" }) { book ->
                BookChip(
                    title = book.item.title,
                    secondary = book.item.author ?: "Available offline",
                    baseUrl = server?.url,
                    coverPath = ApiClient.coverSource(
                        book.item.localCoverPath,
                        book.item.coverPath,
                        book.item.id
                    ),
                    authToken = server?.token,
                    status = DownloadStatus.DOWNLOADED,
                    onClick = { onItem(book.item.id, book.item.originServerUrl) }
                )
            }
        }
        if (books.isEmpty()) {
            item { EmptyState("No downloads", "Download a book to listen without a connection.") }
        }
    }
}

@Composable
fun SearchScreen(viewModel: LibraryViewModel, onItem: (String, String) -> Unit) {
    val query = remember { mutableStateOf("") }
    val input = textInput("Search title, author, or series") { query.value = it }
    val flow = remember(query.value) { viewModel.search(query.value) }
    val results by flow.collectAsState(initial = emptyList())
    val downloads by viewModel.downloadStatuses.collectAsState()
    val server by viewModel.sessionRepository.session.collectAsState()
    val statuses = remember(downloads) {
        downloads.associate { (it.itemId to it.originServerUrl) to it.status }
    }
    WearList {
        item { ScreenTitle("Search") }
        item {
            WearChip(
                onClick = input,
                label = { Text(query.value.ifBlank { "Search books" }, maxLines = 1) },
                secondaryLabel = { Text("Title, author, series, narrator") },
                icon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        items(results, key = { it.id }) { book ->
            BookChip(
                title = book.title,
                secondary = book.author ?: book.series ?: book.narrator ?: "Unknown author",
                baseUrl = server?.url,
                coverPath = ApiClient.coverSource(book.localCoverPath, book.coverPath, book.id),
                authToken = server?.token,
                status = statuses[book.id to book.originServerUrl],
                onClick = { onItem(book.id, book.originServerUrl) },
                onLongClick = downloadLongPressAction(
                    status = statuses[book.id to book.originServerUrl],
                    onStart = { viewModel.startDownload(book.id) },
                    onPause = { viewModel.pauseDownload(book.id, book.originServerUrl) }
                )
            )
        }
        if (query.value.isNotBlank() && results.isEmpty()) {
            item { EmptyState("No matches", "Try another title, author, or series.") }
        }
    }
}

@Composable
fun PlayerScreen(viewModel: PlayerViewModel, isAmbient: Boolean = false) {
    val state by viewModel.state.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val sleepTimer by viewModel.sleepTimer.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val outputPrompt by viewModel.outputPrompt.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(isAmbient) {
        viewModel.setAmbientMode(isAmbient)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.setPlayerScreenActive(true)
                Lifecycle.Event.ON_STOP -> viewModel.setPlayerScreenActive(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        viewModel.setPlayerScreenActive(
            lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        )
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.setPlayerScreenActive(false)
        }
    }
    when {
        state.loading -> CenteredStatus { CircularProgressIndicator(modifier = Modifier.size(40.dp)) }
        state.error != null -> CenteredStatus {
            EmptyState("Playback unavailable", state.error ?: "Playback failed")
        }
        state.itemId == null -> CenteredStatus {
            EmptyState("Nothing playing", "Choose a book from Library or Downloaded.")
        }
        // The player hides the clock: its title row sits where the time text would.
        else -> ScreenScaffold(timeText = {}) { _ ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                val coverServer = activeSession?.takeIf { server ->
                    state.originServerUrl?.let { origin ->
                        com.nortlinos.wearos.data.repository.ServerIdentity.matches(
                            server.url,
                            origin
                        )
                    } == true
                }
                // Always-on mode gets a pure black screen: a dimmed cover still lights most of the
                // OLED panel for as long as the watch sits in ambient, and risks burn-in.
                if (!isAmbient) {
                    BlurredCoverBackground(
                        baseUrl = coverServer?.url,
                        coverPath = state.coverPath,
                        authToken = coverServer?.token,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.58f))
                    )
                }
                val pages = PlaybackPage.entries
                val pagerState = rememberPagerState(
                    initialPage = pages.indexOf(PlaybackPage.NOW_PLAYING),
                    pageCount = { pages.size }
                )
                val pagerScope = rememberCoroutineScope()
                // A tile tap should land on the controls even if the tools page was left open.
                val controlsPageRequests by viewModel.controlsPageRequests.collectAsState()
                LaunchedEffect(controlsPageRequests) {
                    pagerState.scrollToPage(pages.indexOf(PlaybackPage.NOW_PLAYING))
                }
                HorizontalPager(
                    state = pagerState,
                    key = { pages[it] },
                    userScrollEnabled = false,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    // The Wear pager makes only the settled page an active focus group, so the
                    // crown drives volume on Now Playing and scrolls the list on the tools page.
                    when (pages[page]) {
                        PlaybackPage.TOOLS -> PlaybackToolsPage(
                            sleepTimer = sleepTimer,
                            chapterTitle = state.currentChapterTitle,
                            chapterNumber = state.chapterNumber,
                            chapterCount = state.chapterCount,
                            hasNextChapter = state.hasNextChapter,
                            hasPreviousChapter = state.hasPreviousChapter,
                            onNextChapter = viewModel::nextChapter,
                            onPreviousChapter = viewModel::previousChapter,
                            chapters = state.chapters,
                            onSelectChapter = viewModel::seekToChapter,
                            onSetSleepTimer = viewModel::setSleepTimer,
                            onCancelSleepTimer = viewModel::cancelSleepTimer,
                            playbackSpeed = playbackSpeed,
                            onSetPlaybackSpeed = viewModel::setPlaybackSpeed,
                            modifier = Modifier.consumeSwipe(SwipeDirection.RIGHT) {
                                pagerScope.launch {
                                    pagerState.animateScrollToPage(
                                        pages.indexOf(PlaybackPage.NOW_PLAYING)
                                    )
                                }
                            }
                        )
                        PlaybackPage.NOW_PLAYING -> PlayerControlsPage(
                            title = state.title,
                            playing = state.playing,
                            position = viewModel.position,
                            chapterMarks = state.chapterMarks,
                            ambient = isAmbient,
                            onSeekBack = { viewModel.seekBy(-30_000) },
                            onToggle = viewModel::toggle,
                            onSeekForward = { viewModel.seekBy(30_000) },
                            modifier = Modifier.consumeSwipe(SwipeDirection.LEFT) {
                                pagerScope.launch {
                                    pagerState.animateScrollToPage(
                                        pages.indexOf(PlaybackPage.TOOLS)
                                    )
                                }
                            }
                        )
                    }
                }
                // Two dots tell the user there is a second page; always-on keeps only the essentials.
                if (!isAmbient) {
                    HorizontalPageIndicator(
                        pagerState = pagerState,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
                ProgressConflictDialog(
                    serverPositionMs = state.conflictPositionMs,
                    position = viewModel.position,
                    visible = !isAmbient,
                    onKeepServer = viewModel::keepServerProgress,
                    onKeepWatch = viewModel::keepLocalProgress
                )
                HeadphonePromptDialog(
                    prompt = outputPrompt,
                    visible = !isAmbient,
                    onConnect = { viewModel.connectHeadphones(context) },
                    onUseSpeaker = viewModel::useWatchSpeaker,
                    onDismiss = viewModel::dismissOutputPrompt
                )
            }
        }
    }
}

/**
 * Asks which position wins when sync found newer progress on the server while this watch also
 * had unsynced listening. Until the user answers, sync holds the watch's push so neither
 * position is lost. Swiping the dialog away defers the choice for this visit only.
 */
@Composable
private fun ProgressConflictDialog(
    serverPositionMs: Long?,
    position: StateFlow<PlaybackPosition>,
    visible: Boolean,
    onKeepServer: () -> Unit,
    onKeepWatch: () -> Unit
) {
    val dismissedFor = remember { mutableStateOf<Long?>(null) }
    AlertDialog(
        visible = visible && serverPositionMs != null && dismissedFor.value != serverPositionMs,
        onDismissRequest = { dismissedFor.value = serverPositionMs },
        title = { Text("Progress differs", textAlign = TextAlign.Center) },
        text = {
            Text(
                "Another device saved newer progress for this book. Keep which position?",
                textAlign = TextAlign.Center
            )
        }
    ) {
        item {
            WearChip(
                onClick = onKeepServer,
                label = { Text("Server") },
                secondaryLabel = { Text(time(serverPositionMs ?: 0)) },
                icon = { Icon(Icons.Default.CloudDownload, null) },
                style = ChipStyle.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            WearChip(
                onClick = onKeepWatch,
                label = { Text("This watch") },
                secondaryLabel = {
                    // Collected here so the position only recomposes while the dialog is shown.
                    val watchPosition by position.collectAsState()
                    Text(time(watchPosition.bookPositionMs))
                },
                icon = { Icon(Icons.Default.Watch, null) },
                style = ChipStyle.SECONDARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Shown when playback is about to start with no headphones connected. Wear OS guidance is to
 * offer a way to connect rather than fail: "Connect headphones" opens the system output switcher
 * (or Bluetooth settings) and playback resumes once a headset connects. The speaker option only
 * appears on watches that have one.
 */
@Composable
private fun HeadphonePromptDialog(
    prompt: OutputPrompt?,
    visible: Boolean,
    onConnect: () -> Unit,
    onUseSpeaker: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        visible = visible && prompt != null,
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Headphones, null) },
        title = { Text("No headphones", textAlign = TextAlign.Center) },
        text = {
            Text(
                if (prompt?.speakerAvailable == true) {
                    "Connect Bluetooth headphones, or play through the watch speaker."
                } else {
                    "Connect Bluetooth headphones to listen."
                },
                textAlign = TextAlign.Center
            )
        }
    ) {
        item {
            WearChip(
                onClick = onConnect,
                label = { Text("Connect headphones") },
                icon = { Icon(Icons.Default.Bluetooth, null) },
                style = ChipStyle.PRIMARY
            )
        }
        if (prompt?.speakerAvailable == true) {
            item {
                WearChip(
                    onClick = onUseSpeaker,
                    label = { Text("Use watch speaker") },
                    icon = { Icon(Icons.AutoMirrored.Filled.VolumeUp, null) },
                    style = ChipStyle.SECONDARY
                )
            }
        }
    }
}

private enum class PlaybackPage {
    NOW_PLAYING,
    TOOLS
}

@Composable
private fun PlayerControlsPage(
    title: String,
    playing: Boolean,
    position: StateFlow<PlaybackPosition>,
    chapterMarks: List<Float>,
    ambient: Boolean,
    onSeekBack: () -> Unit,
    onToggle: () -> Unit,
    onSeekForward: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Round watches get the whole-book ring around the edge; square ones keep the linear bar.
    val round = LocalConfiguration.current.isScreenRound
    Box(modifier = modifier.fillMaxSize()) {
        if (round) {
            ChapterRing(
                position = position,
                chapterMarks = chapterMarks,
                ambient = ambient,
                modifier = Modifier.fillMaxSize()
            )
        }
        PlayerControlsColumn(
            title = title,
            playing = playing,
            position = position,
            showBar = !round,
            ambient = ambient,
            onSeekBack = onSeekBack,
            onToggle = onToggle,
            onSeekForward = onSeekForward
        )
    }
}

@Composable
private fun PlayerControlsColumn(
    title: String,
    playing: Boolean,
    position: StateFlow<PlaybackPosition>,
    showBar: Boolean,
    ambient: Boolean,
    onSeekBack: () -> Unit,
    onToggle: () -> Unit,
    onSeekForward: () -> Unit
) {
    // Round-screen layout: the top and bottom rows are inset by a share of the screen width
    // because the circle narrows there, while the transport row in the middle uses nearly the
    // full width. Fixed 28 dp insets pushed three 52 dp buttons past the edge of 192 dp screens.
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val buttonSize = if (isLargeRoundScreen()) 52.dp else 48.dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .crownVolume()
            .padding(vertical = screenWidth * 0.14f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = screenWidth * 0.15f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = screenWidth * 0.04f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlaybackButton(
                onClick = onSeekBack,
                icon = Icons.Default.Replay30,
                description = "Back 30 seconds",
                ambient = ambient,
                size = buttonSize
            )
            // Ambient swaps filled surfaces for outlines so only a few pixels stay lit.
            val toggleIcon: @Composable BoxScope.() -> Unit = {
                Icon(
                    if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                    if (playing) "Pause" else "Play"
                )
            }
            if (ambient) {
                OutlinedIconButton(
                    onClick = onToggle,
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        contentColor = Color.LightGray
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(
                        enabled = true,
                        borderColor = Color.Gray
                    ),
                    modifier = Modifier.size(buttonSize),
                    content = toggleIcon
                )
            } else {
                FilledIconButton(
                    onClick = onToggle,
                    modifier = Modifier.size(buttonSize),
                    content = toggleIcon
                )
            }
            PlaybackButton(
                onClick = onSeekForward,
                icon = Icons.Default.Forward30,
                description = "Forward 30 seconds",
                ambient = ambient,
                size = buttonSize
            )
        }
        PlaybackProgress(
            position = position,
            showBar = showBar,
            ambient = ambient,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = screenWidth * 0.17f)
        )
    }
}

/**
 * Whole-book progress around the edge of a round screen, split at chapter boundaries with each
 * arc as long as its chapter. The position is read only in the draw phase, so the per-second tick
 * repaints the ring without recomposing or re-measuring anything.
 */
@Composable
private fun ChapterRing(
    position: StateFlow<PlaybackPosition>,
    chapterMarks: List<Float>,
    ambient: Boolean,
    modifier: Modifier = Modifier
) {
    val current = position.collectAsState()
    // Ambient keeps a thin grey outline only, so few pixels stay lit.
    val fill = if (ambient) Color.Gray else MaterialTheme.colorScheme.primary
    val track = if (ambient) Color.DarkGray else Color.White.copy(alpha = 0.22f)
    val strokeWidth = if (ambient) 2.dp else 5.dp
    Spacer(
        modifier = modifier.drawBehind {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2 + 2.dp.toPx()
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            val topLeft = Offset(inset, inset)
            val bounds = RingGeometry.bounds(chapterMarks)
            val gap = RingGeometry.gapDegrees(bounds.size - 1)
            val progress = current.value.bookProgress
            val style = Stroke(width = stroke, cap = StrokeCap.Butt)
            for (i in 0 until bounds.size - 1) {
                val from = bounds[i]
                val to = bounds[i + 1]
                val sweep = (to - from) * 360f - gap
                if (sweep <= 0f) continue
                val start = -90f + from * 360f + gap / 2
                drawArc(track, start, sweep, useCenter = false, topLeft = topLeft, size = arcSize, style = style)
                val filled = ((progress - from) / (to - from)).coerceIn(0f, 1f) * sweep
                if (filled > 0f) {
                    drawArc(fill, start, filled, useCenter = false, topLeft = topLeft, size = arcSize, style = style)
                }
            }
        }
    )
}

internal object RingGeometry {
    /** 0, each chapter start, 1: the edges of the ring's segments. */
    fun bounds(chapterMarks: List<Float>): List<Float> = buildList {
        add(0f)
        chapterMarks.filterTo(this) { it > 0f && it < 1f }
        add(1f)
    }

    /**
     * Gap between segments in degrees: 2° normally, shrinking for books with so many chapters
     * that fixed gaps would eat the ring. A single segment has no gap at all.
     */
    fun gapDegrees(segments: Int): Float =
        if (segments <= 1) 0f else minOf(2f, 360f / segments * 0.3f)
}

/**
 * Progress bar and elapsed/total text: the only part of the player that changes every second.
 * It collects the position itself so those ticks recompose one Text, and the bar reads the
 * fraction in the draw phase so it never re-measures.
 */
@Composable
private fun PlaybackProgress(
    position: StateFlow<PlaybackPosition>,
    showBar: Boolean,
    ambient: Boolean,
    modifier: Modifier = Modifier
) {
    val current = position.collectAsState()
    val fill = if (ambient) Color.Gray else Color.White
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (showBar) Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.78f))
                .drawBehind {
                    drawRect(
                        color = fill,
                        size = size.copy(width = size.width * current.value.displayProgress)
                    )
                }
        )
        if (!ambient) {
            val value = current.value
            Text(
                text = "${time(value.displayPositionMs)} / ${time(value.displayDurationMs)}",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun PlaybackButton(
    onClick: () -> Unit,
    icon: ImageVector,
    description: String,
    enabled: Boolean = true,
    ambient: Boolean = false,
    size: Dp = 52.dp
) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = Color.Black.copy(alpha = 0.82f),
            contentColor = if (ambient) Color.Gray else Color.White
        ),
        modifier = Modifier.size(size)
    ) {
        Icon(icon, description)
    }
}

@Composable
private fun PlaybackToolsPage(
    sleepTimer: SleepTimerState,
    chapterTitle: String?,
    chapterNumber: Int,
    chapterCount: Int,
    hasNextChapter: Boolean,
    hasPreviousChapter: Boolean,
    onNextChapter: () -> Unit,
    onPreviousChapter: () -> Unit,
    chapters: List<ChapterOption>,
    onSelectChapter: (Int) -> Unit,
    onSetSleepTimer: (Int) -> Unit,
    onCancelSleepTimer: () -> Unit,
    playbackSpeed: Float,
    onSetPlaybackSpeed: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val showCustomTimer = remember { mutableStateOf(false) }
    val showChapters = remember { mutableStateOf(false) }
    if (showChapters.value) {
        ChapterList(
            chapters = chapters,
            currentIndex = chapterNumber - 1,
            onSelect = { index ->
                showChapters.value = false
                onSelectChapter(index)
            },
            modifier = modifier
        )
        return
    }
    if (showCustomTimer.value) {
        SleepTimerPicker(
            initialMinutes = (sleepTimer.durationMs / 60_000L)
                .coerceIn(1, MAX_SLEEP_TIMER_MINUTES.toLong())
                .toInt()
                .takeIf { sleepTimer.active } ?: DEFAULT_CUSTOM_TIMER_MINUTES,
            onConfirm = { minutes ->
                showCustomTimer.value = false
                onSetSleepTimer(minutes)
            },
            onDismiss = { showCustomTimer.value = false },
            modifier = modifier
        )
        return
    }
    Box(modifier = modifier.fillMaxSize()) {
        WearList(showTime = false) {
            item { ScreenTitle("Playback tools") }
            // chapterNumber is 0 when the position precedes the first chapter, which server
            // metadata does allow; there is no "chapter 0" to name in that case.
            val numbered = chapterNumber in 1..chapterCount
            item(key = "chapter-status") {
                SupportingText(
                    when {
                        chapterCount == 0 -> "Chapters unavailable"
                        chapterTitle != null -> chapterTitle
                        numbered -> "Chapter $chapterNumber of $chapterCount"
                        else -> "$chapterCount chapters"
                    }
                )
            }
            if (chapterCount > 0) {
                if (chapterTitle != null && numbered) {
                    item(key = "chapter-count") {
                        SupportingText("Chapter $chapterNumber of $chapterCount")
                    }
                }
                item(key = "chapter-controls") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlaybackButton(
                            onClick = onPreviousChapter,
                            icon = Icons.Filled.SkipPrevious,
                            description = "Previous chapter",
                            enabled = hasPreviousChapter
                        )
                        PlaybackButton(
                            onClick = onNextChapter,
                            icon = Icons.Filled.SkipNext,
                            description = "Next chapter",
                            enabled = hasNextChapter
                        )
                    }
                }
                item(key = "chapter-list") {
                    WearChip(
                        onClick = { showChapters.value = true },
                        label = { Text("All chapters") },
                        secondaryLabel = { Text("Jump to any chapter") },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item(key = "sleep-status") {
                SupportingText(
                    if (sleepTimer.active) {
                        "Sleep timer \u00b7 ${sleepTimerMinutes(sleepTimer.remainingMs)} remaining"
                    } else {
                        "Sleep timer"
                    }
                )
            }
            listOf(15, 30, 45, 60).forEach { minutes ->
                item(key = "sleep-$minutes") {
                    WearChip(
                        onClick = { onSetSleepTimer(minutes) },
                        label = { Text("$minutes minutes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item(key = "sleep-custom") {
                WearChip(
                    onClick = { showCustomTimer.value = true },
                    label = { Text("Custom timer") },
                    secondaryLabel = { Text("Choose 1 to $MAX_SLEEP_TIMER_MINUTES minutes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (sleepTimer.active) {
                item(key = "sleep-reset") {
                    WearChip(
                        onClick = {
                            onSetSleepTimer(
                                (sleepTimer.durationMs / 60_000)
                                    .coerceAtLeast(1)
                                    .toInt()
                            )
                        },
                        label = { Text("Reset timer") },
                        secondaryLabel = {
                            Text("${sleepTimerMinutes(sleepTimer.durationMs)} from now")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item(key = "sleep-cancel") {
                    WearChip(
                        onClick = onCancelSleepTimer,
                        label = { Text("Cancel sleep timer") },
                        style = ChipStyle.SECONDARY,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item(key = "speed-status") {
                SupportingText("Playback speed \u00b7 ${PlaybackSpeedFormat.label(playbackSpeed)}x")
            }
            PlaybackSpeedFormat.PRESETS.forEach { speed ->
                item(key = "speed-$speed") {
                    ChoiceChip(
                        label = "${PlaybackSpeedFormat.label(speed)}x",
                        selected = speed == playbackSpeed,
                        onClick = { onSetPlaybackSpeed(speed) }
                    )
                }
            }
        }
    }
}

/**
 * Every chapter with its start time, opened scrolled to the current one. Choosing a chapter seeks
 * there and returns to the tools list; swiping right (handled by the tools page) also returns.
 */
@Composable
private fun ChapterList(
    chapters: List<ChapterOption>,
    currentIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Title is item 0, so chapter i is item i + 1.
    val listState = rememberTransformingLazyColumnState(
        initialAnchorItemIndex = if (currentIndex >= 0) currentIndex + 1 else -1
    )
    Box(modifier = modifier.fillMaxSize()) {
        WearList(showTime = false, listState = listState) {
            item { ScreenTitle("Chapters") }
            itemsIndexed(chapters, key = { index, _ -> index }) { index, chapter ->
                WearChip(
                    onClick = { onSelect(index) },
                    label = { Text(chapter.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                    secondaryLabel = {
                        Text("${time(chapter.startMs)} \u00b7 ${time(chapter.durationMs)} long")
                    },
                    style = if (index == currentIndex) ChipStyle.PRIMARY else ChipStyle.TONAL,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Crown-scrollable picker for an arbitrary sleep-timer length.
 *
 * A watch has no comfortable keyboard, so the duration is dialled in with the rotating side button
 * rather than typed. The picker covers every supported value, which keeps the choice unconstrained
 * without needing validation or an error state.
 */
@Composable
private fun SleepTimerPicker(
    initialMinutes: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pickerState = rememberPickerState(
        initialNumberOfOptions = MAX_SLEEP_TIMER_MINUTES,
        initiallySelectedIndex = initialMinutes - 1,
        shouldRepeatOptions = false
    )
    val selectedMinutes = pickerState.selectedOptionIndex + 1
    ScreenScaffold(timeText = {}) { _ ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "Sleep timer",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Picker(
                state = pickerState,
                contentDescription = { "Sleep timer length in minutes" },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { option ->
                val minutes = option + 1
                Text(
                    if (minutes == 1) "1 minute" else "$minutes minutes",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (option == pickerState.selectedOptionIndex) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    }
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlaybackButton(
                    onClick = onDismiss,
                    icon = Icons.Filled.Close,
                    description = "Cancel"
                )
                PlaybackButton(
                    onClick = { onConfirm(selectedMinutes) },
                    icon = Icons.Filled.Check,
                    description = "Start sleep timer"
                )
            }
        }
    }
}

/** Sleep timer bounds. 12 hours comfortably exceeds any single listening session. */
private const val MAX_SLEEP_TIMER_MINUTES = 720
private const val DEFAULT_CUSTOM_TIMER_MINUTES = 20

private enum class SwipeDirection {
    LEFT,
    RIGHT
}

private fun Modifier.consumeSwipe(
    direction: SwipeDirection,
    onSwipe: () -> Unit
): Modifier = pointerInput(direction, onSwipe) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        awaitHorizontalTouchSlopOrCancellation(down.id) { change, overSlop ->
            val matches = when (direction) {
                SwipeDirection.LEFT -> overSlop < 0
                SwipeDirection.RIGHT -> overSlop > 0
            }
            if (matches) {
                change.consume()
                onSwipe()
            }
        }
    }
}

private fun sleepTimerMinutes(remainingMs: Long): String {
    val minutes = SleepTimerMath.displayedMinutes(remainingMs)
    return if (minutes == 1L) "1 minute" else "$minutes minutes"
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val mode by viewModel.mode.collectAsState()
    val storage by viewModel.storageStats.collectAsState()
    val downloadedBooks by viewModel.downloadedBooks.collectAsState()
    val confirmLogout = remember { mutableStateOf(false) }
    val seriesView by viewModel.seriesView.collectAsState()
    val searchVisible by viewModel.homeSearchVisible.collectAsState()
    val podcastsVisible by viewModel.homePodcastsVisible.collectAsState()
    WearList {
        item { ScreenTitle("Settings") }
        item { SupportingText("Downloads") }
        item {
            SupportingText(
                when {
                    storage.error != null -> storage.error.orEmpty()
                    storage.totalBytes <= 0 -> "Calculating storage"
                    else -> "${downloadedBooks.size} downloaded " +
                        (if (downloadedBooks.size == 1) "book · " else "books · ") +
                        "${formatBytes(storage.booksBytes)} of " +
                        "${formatBytes(storage.totalBytes)} used by stored books · " +
                        "${formatBytes(storage.availableBytes)} free"
                }
            )
        }
        item { SupportingText("Library") }
        item {
            SettingSwitch(
                label = "Show series",
                description = "Open libraries grouped into series. Loads the whole library, so it uses more battery than the book list.",
                checked = seriesView,
                onCheckedChange = viewModel::setSeriesView
            )
        }
        item {
            SettingSwitch(
                label = "Search on main menu",
                description = "Show the Search entry on the main menu.",
                checked = searchVisible,
                onCheckedChange = viewModel::setHomeSearchVisible
            )
        }
        item {
            SettingSwitch(
                label = "Podcasts on main menu",
                description = "Show the Podcasts entry on the main menu.",
                checked = podcastsVisible,
                onCheckedChange = viewModel::setHomePodcastsVisible
            )
        }
        item { SupportingText("Show playback progress as") }
        item {
            ChoiceChip(
                label = "Whole book",
                selected = mode == ProgressDisplayMode.BOOK,
                onClick = { viewModel.setMode(ProgressDisplayMode.BOOK) }
            )
        }
        item {
            ChoiceChip(
                label = "Current chapter",
                selected = mode == ProgressDisplayMode.CHAPTER,
                onClick = { viewModel.setMode(ProgressDisplayMode.CHAPTER) }
            )
        }
        item {
            WearChip(
                onClick = {
                    if (confirmLogout.value) viewModel.logout()
                    else confirmLogout.value = true
                },
                label = { Text(if (confirmLogout.value) "Tap again to log out" else "Log out") },
                secondaryLabel = {
                    Text(
                        if (confirmLogout.value) "This removes the saved server session"
                        else "Downloads and progress stay on watch"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Standard scrolling screen: a [TransformingLazyColumn] inside a [ScreenScaffold], so the time
 * text, scroll indicator, crown scrolling, and edge-shrinking item transformations all come from
 * the platform instead of being hand-wired per screen.
 */
@Composable
private fun WearList(
    showTime: Boolean = true,
    listState: TransformingLazyColumnState? = null,
    content: WearListScope.() -> Unit
) {
    val rememberedState = rememberTransformingLazyColumnState()
    val state = listState ?: rememberedState
    val spec = rememberTransformationSpec()
    ScreenScaffold(
        scrollState = state,
        timeText = if (showTime) null else ({})
    ) { contentPadding ->
        TransformingLazyColumn(
            state = state,
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize(),
            content = { WearListScope(this, spec).content() }
        )
    }
}

/**
 * Wraps [TransformingLazyColumnScope] so every item publishes its transformation through
 * [LocalListItemTransform]. List surfaces ([WearChip], [ChoiceChip], [ScreenTitle]) pick it up and
 * morph as they scroll to the curved edge; plain text and images are left untouched.
 */
private class WearListScope(
    private val scope: TransformingLazyColumnScope,
    private val spec: TransformationSpec
) {
    fun item(key: Any? = null, content: @Composable () -> Unit) {
        scope.item(key = key) { TransformedItem(this, spec, content) }
    }

    fun <T> items(items: List<T>, key: ((T) -> Any)? = null, content: @Composable (T) -> Unit) {
        scope.items(
            count = items.size,
            key = key?.let { itemKey -> { index: Int -> itemKey(items[index]) } }
        ) { index -> TransformedItem(this, spec) { content(items[index]) } }
    }

    fun <T> itemsIndexed(
        items: List<T>,
        key: ((Int, T) -> Any)? = null,
        content: @Composable (Int, T) -> Unit
    ) {
        scope.items(
            count = items.size,
            key = key?.let { itemKey -> { index: Int -> itemKey(index, items[index]) } }
        ) { index -> TransformedItem(this, spec) { content(index, items[index]) } }
    }
}

private class ListItemTransform(
    val scope: TransformingLazyColumnItemScope,
    val spec: TransformationSpec
) {
    val surface: SurfaceTransformation = with(scope) { SurfaceTransformation(spec) }
    val modifier: Modifier = Modifier.transformedHeight(scope, spec)
}

private val LocalListItemTransform = compositionLocalOf<ListItemTransform?> { null }

@Composable
private fun TransformedItem(
    scope: TransformingLazyColumnItemScope,
    spec: TransformationSpec,
    content: @Composable () -> Unit
) {
    val transform = remember(scope, spec) { ListItemTransform(scope, spec) }
    CompositionLocalProvider(LocalListItemTransform provides transform, content = content)
}

private enum class ChipStyle {
    /** Default list action: dark tonal container. */
    TONAL,
    /** The screen's main action, in the primary color. */
    PRIMARY,
    /** Lower-emphasis or destructive-adjacent action, outline only. */
    SECONDARY
}

/** Full-width list button that applies the enclosing [WearList] item transformation. */
@Composable
private fun WearChip(
    onClick: () -> Unit,
    label: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    secondaryLabel: (@Composable RowScope.() -> Unit)? = null,
    icon: (@Composable BoxScope.() -> Unit)? = null,
    style: ChipStyle = ChipStyle.TONAL,
    enabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
    colors: ButtonColors? = null
) {
    val transform = LocalListItemTransform.current
    val itemModifier = modifier
        .fillMaxWidth()
        .then(transform?.modifier ?: Modifier)
    when (style) {
        ChipStyle.PRIMARY -> Button(
            onClick = onClick,
            modifier = itemModifier,
            onLongClick = onLongClick,
            onLongClickLabel = onLongClickLabel,
            colors = colors ?: ButtonDefaults.buttonColors(),
            secondaryLabel = secondaryLabel,
            icon = icon,
            enabled = enabled,
            transformation = transform?.surface,
            label = label
        )
        ChipStyle.TONAL -> FilledTonalButton(
            onClick = onClick,
            modifier = itemModifier,
            onLongClick = onLongClick,
            onLongClickLabel = onLongClickLabel,
            colors = colors ?: ButtonDefaults.filledTonalButtonColors(),
            secondaryLabel = secondaryLabel,
            icon = icon,
            enabled = enabled,
            transformation = transform?.surface,
            label = label
        )
        ChipStyle.SECONDARY -> OutlinedButton(
            onClick = onClick,
            modifier = itemModifier,
            onLongClick = onLongClick,
            onLongClickLabel = onLongClickLabel,
            colors = colors ?: ButtonDefaults.outlinedButtonColors(),
            secondaryLabel = secondaryLabel,
            icon = icon,
            enabled = enabled,
            transformation = transform?.surface,
            label = label
        )
    }
}

internal object LibraryPaging {
    private const val PREFETCH_DISTANCE = 6

    fun shouldLoadNext(
        visibleBookIndex: Int,
        loadedBooks: Int,
        hasMore: Boolean,
        loading: Boolean,
        searching: Boolean
    ): Boolean =
        hasMore && !loading && !searching &&
            visibleBookIndex >= (loadedBooks - PREFETCH_DISTANCE).coerceAtLeast(0)
}

@Composable
private fun NowPlayingChip(
    title: String,
    playing: Boolean,
    originServerUrl: String?,
    onClick: () -> Unit
) {
    WearChip(
        onClick = onClick,
        label = { Text(if (playing) "Now playing" else "Paused") },
        secondaryLabel = {
            Text(
                listOfNotNull(title, originServerUrl?.let(::serverLabel)).joinToString(" · "),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        icon = { Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, null) },
        style = ChipStyle.PRIMARY,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun NavigationChip(
    label: String,
    secondary: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    WearChip(
        onClick = onClick,
        label = { Text(label) },
        secondaryLabel = { Text(secondary, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        icon = { Icon(icon, null) },
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * A book row: an M3 [TitleCard] over the book's cover (under the standard scrim) with title,
 * author and download state. While the cover is loading the card shimmers; the shimmer stops as
 * soon as the image arrives or fails, so it never animates on a screen that is just sitting idle.
 */
@Composable
private fun BookChip(
    title: String,
    secondary: String,
    baseUrl: String?,
    coverPath: String?,
    authToken: String?,
    status: DownloadStatus?,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val transform = LocalListItemTransform.current
    val context = LocalContext.current
    val request = remember(context, baseUrl, coverPath, authToken) {
        coverRequest(context, baseUrl, coverPath, authToken, decodeSizePx = CARD_COVER_DECODE_PX)
    }
    val cardModifier = Modifier
        .fillMaxWidth()
        .then(transform?.modifier ?: Modifier)
    val cardTitle: @Composable RowScope.() -> Unit = {
        Text(title, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
    val cardSubtitle: @Composable ColumnScope.() -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DownloadStatusIcon(status)
            Spacer(Modifier.width(4.dp))
            Text(secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
    if (request == null) {
        TitleCard(
            onClick = onClick,
            onLongClick = onLongClick,
            onLongClickLabel = onLongClick?.let { "Download" },
            title = cardTitle,
            subtitle = cardSubtitle,
            transformation = transform?.surface,
            modifier = cardModifier
        )
        return
    }
    val painter = rememberAsyncImagePainter(model = request, contentScale = ContentScale.Crop)
    val cropped = remember(painter) { CenterCropPainter(painter) }
    val painterState = painter.state
    val placeholder = rememberPlaceholderState(
        isVisible = painterState is AsyncImagePainter.State.Loading ||
            painterState is AsyncImagePainter.State.Empty
    )
    TitleCard(
        onClick = onClick,
        onLongClick = onLongClick,
        onLongClickLabel = onLongClick?.let { "Download" },
        containerPainter = CardDefaults.containerPainter(image = cropped),
        title = cardTitle,
        subtitle = cardSubtitle,
        transformation = transform?.surface,
        modifier = cardModifier.placeholderShimmer(placeholder)
    )
}

/**
 * Draws [inner] centre-cropped to whatever size it is given. M3's container painter scales from
 * its own (unspecified) intrinsic size, which stretches a square cover across a wide card.
 */
private class CenterCropPainter(private val inner: Painter) : Painter() {
    override val intrinsicSize: Size get() = Size.Unspecified

    override fun DrawScope.onDraw() {
        val src = inner.intrinsicSize
        if (src.isUnspecified || src.width <= 0f || src.height <= 0f) {
            with(inner) { draw(size) }
            return
        }
        val scale = maxOf(size.width / src.width, size.height / src.height)
        val scaled = Size(src.width * scale, src.height * scale)
        clipRect {
            translate((size.width - scaled.width) / 2, (size.height - scaled.height) / 2) {
                with(inner) { draw(scaled) }
            }
        }
    }
}

/** Covers arrive at 160 px wide; decoding at that size keeps one small bitmap per card. */
private const val CARD_COVER_DECODE_PX = 160

@Composable
private fun DownloadStatusIcon(status: DownloadStatus?) {
    val (icon, description, color) = when (status) {
        DownloadStatus.DOWNLOADED -> Triple(Icons.Default.CheckCircle, "Downloaded", MaterialTheme.colorScheme.secondary)
        DownloadStatus.QUEUED ->
            Triple(Icons.Default.CloudDownload, "Queued to download", MaterialTheme.colorScheme.primary)
        DownloadStatus.DOWNLOADING ->
            Triple(Icons.Default.CloudDownload, "Downloading", MaterialTheme.colorScheme.primary)
        DownloadStatus.PAUSED -> Triple(Icons.Default.Pause, "Download paused", MaterialTheme.colorScheme.onSurface)
        DownloadStatus.FAILED -> Triple(Icons.Default.Error, "Download failed", MaterialTheme.colorScheme.error)
        null -> Triple(Icons.Default.CloudDownload, "Not downloaded", MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
    }
    Icon(icon, description, tint = color, modifier = Modifier.size(16.dp))
}

/**
 * Long-press-to-download action shared by book tiles (and episode tiles): starts the download if
 * it isn't running, pauses it if it's in flight, and does nothing once it's already downloaded —
 * that state is managed from the item's own detail page instead.
 */
private fun downloadLongPressAction(
    status: DownloadStatus?,
    onStart: () -> Unit,
    onPause: () -> Unit
): () -> Unit = {
    when (status) {
        null, DownloadStatus.PAUSED, DownloadStatus.FAILED -> onStart()
        DownloadStatus.DOWNLOADING, DownloadStatus.QUEUED -> onPause()
        DownloadStatus.DOWNLOADED -> {}
    }
}

private fun episodeStatusIcon(status: DownloadStatus?) = when (status) {
    DownloadStatus.DOWNLOADED -> Icons.Default.CheckCircle
    DownloadStatus.DOWNLOADING, DownloadStatus.QUEUED -> Icons.Default.CloudDownload
    DownloadStatus.PAUSED -> Icons.Default.Pause
    DownloadStatus.FAILED -> Icons.Default.Error
    null -> Icons.Default.Download
}

private fun episodeStatusLabel(status: DownloadStatus?, downloadedBytes: Long, totalBytes: Long): String? =
    when (status) {
        DownloadStatus.DOWNLOADED -> "Downloaded"
        DownloadStatus.DOWNLOADING -> downloadProgress(downloadedBytes, totalBytes)
        DownloadStatus.QUEUED -> "Queued"
        DownloadStatus.PAUSED -> "Paused"
        DownloadStatus.FAILED -> "Download failed"
        null -> null
    }

// Episode tile palette: a downloaded episode reads as "done" (orange fill, black text, green
// check), everything still needing a download reads as "actionable" (dark fill, white text,
// orange download icon) regardless of in-progress/paused/failed state.
private val EpisodeNotDownloadedContainer = Color(0xFF2E2E2E)
private val EpisodeNotDownloadedContent = Color.White
private val EpisodeNotDownloadedIcon = Color(0xFFFF9800)
private val EpisodeDownloadedContainer = Color(0xFFFF9800)
private val EpisodeDownloadedContent = Color.Black
private val EpisodeDownloadedIcon = Color(0xFF4CAF50)

@Composable
private fun episodeChipColors(status: DownloadStatus?): ButtonColors {
    val downloaded = status == DownloadStatus.DOWNLOADED
    return ButtonDefaults.buttonColors(
        containerColor = if (downloaded) EpisodeDownloadedContainer else EpisodeNotDownloadedContainer,
        contentColor = if (downloaded) EpisodeDownloadedContent else EpisodeNotDownloadedContent,
        secondaryContentColor = if (downloaded) EpisodeDownloadedContent else EpisodeNotDownloadedContent,
        iconColor = if (downloaded) EpisodeDownloadedIcon else EpisodeNotDownloadedIcon
    )
}

@Composable
private fun DownloadAction(
    status: DownloadStatus?,
    downloadedBytes: Long,
    totalBytes: Long,
    error: String?,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onDelete: () -> Unit,
    deleteConfirmationPending: Boolean,
    // Podcast name and episode position, e.g. "My Show · Episode 3 of 12". Only supplied for
    // per-episode download buttons, where every button in the list otherwise shares an identical
    // label and would be indistinguishable when navigating by rotary bezel or TalkBack.
    episodeContext: String? = null
) {
    fun withContext(text: String) = listOfNotNull(episodeContext, text).joinToString(" · ")
    when (status) {
        DownloadStatus.DOWNLOADED -> WearChip(
            onClick = onDelete,
            label = { Text(if (deleteConfirmationPending) "Tap again to delete" else "Delete download") },
            secondaryLabel = {
                Text(
                    withContext(
                        if (deleteConfirmationPending) "Audio files will be removed" else formatBytes(totalBytes)
                    )
                )
            },
            icon = { Icon(Icons.Default.Delete, null) },
            modifier = Modifier.fillMaxWidth()
        )
        DownloadStatus.DOWNLOADING, DownloadStatus.QUEUED -> WearChip(
            onClick = onPause,
            label = { Text("Pause download") },
            secondaryLabel = {
                Text(
                    withContext(
                        if (status == DownloadStatus.QUEUED) {
                            "Queued behind other downloads"
                        } else {
                            downloadProgress(downloadedBytes, totalBytes)
                        }
                    ),
                    maxLines = 1
                )
            },
            icon = { Icon(Icons.Default.Pause, null) },
            modifier = Modifier.fillMaxWidth()
        )
        DownloadStatus.PAUSED, DownloadStatus.FAILED -> WearChip(
            onClick = onStart,
            label = { Text("Resume download") },
            secondaryLabel = {
                Text(withContext(error ?: downloadProgress(downloadedBytes, totalBytes)), maxLines = 1)
            },
            icon = { Icon(Icons.Default.Download, null) },
            modifier = Modifier.fillMaxWidth()
        )
        null -> WearChip(
            onClick = onStart,
            label = { Text("Download for offline") },
            secondaryLabel = episodeContext?.let { context -> { Text(context, maxLines = 1) } },
            icon = { Icon(Icons.Default.Download, null) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val transform = LocalListItemTransform.current
    RadioButton(
        selected = selected,
        onSelect = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(transform?.modifier ?: Modifier),
        transformation = transform?.surface,
        label = { Text(label) }
    )
}

@Composable
private fun SettingSwitch(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val transform = LocalListItemTransform.current
    SwitchButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = Modifier
            .fillMaxWidth()
            .then(transform?.modifier ?: Modifier),
        transformation = transform?.surface,
        label = { Text(label, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        secondaryLabel = { Text(description, maxLines = 2, overflow = TextOverflow.Ellipsis) }
    )
}

@Composable
private fun ScreenTitle(text: String) {
    val transform = LocalListItemTransform.current
    ListHeader(
        modifier = Modifier
            .fillMaxWidth()
            .then(transform?.modifier ?: Modifier),
        transformation = transform?.surface
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SupportingText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
private fun StatusText(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun EmptyState(title: String, detail: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ConnectionStatus(
    online: Boolean,
    serverUrl: String?,
    offlineDetail: String
) {
    val connected = online && !serverUrl.isNullOrBlank()
    WearChip(
        onClick = {},
        enabled = false,
        label = {
            Text(
                if (connected) serverLabel(serverUrl.orEmpty()) else "Offline",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        secondaryLabel = {
            Text(
                if (connected) "Connected" else offlineDetail,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        icon = {
            Icon(
                if (connected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                null,
                tint = if (connected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CenteredStatus(content: @Composable () -> Unit) {
    ScreenScaffold { _ ->
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun InputChip(label: String, value: String, onClick: () -> Unit) {
    WearChip(
        onClick = onClick,
        label = { Text(value.ifBlank { label }, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        secondaryLabel = if (value.isBlank()) null else ({ Text(label) }),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun textInput(label: String, result: (String) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val value = it.data?.let(RemoteInput::getResultsFromIntent)
                ?.getCharSequence("text")?.toString()
            if (!value.isNullOrBlank()) result(value)
        }
    }
    return {
        val input = RemoteInput.Builder("text").setLabel(label).build()
        val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
        RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(input))
        launcher.launch(intent)
    }
}

private fun time(ms: Long): String {
    val total = TimeUnit.MILLISECONDS.toSeconds(ms.coerceAtLeast(0))
    return if (total >= 3600) "%d:%02d:%02d".format(total / 3600, total / 60 % 60, total % 60)
    else "%d:%02d".format(total / 60, total % 60)
}

private fun downloadProgress(downloaded: Long, total: Long): String =
    if (total <= 0) formatBytes(downloaded) else "${downloaded * 100 / total}% · ${formatBytes(downloaded)}"

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1_073_741_824 -> "%.1f GB".format(bytes / 1_073_741_824.0)
    bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
    else -> "${bytes / 1024} KB"
}

private fun serverLabel(serverUrl: String): String =
    runCatching { Uri.parse(serverUrl).host }.getOrNull().orEmpty().ifBlank { serverUrl }

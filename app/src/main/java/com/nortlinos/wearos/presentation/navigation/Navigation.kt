package com.nortlinos.wearos.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.nortlinos.wearos.presentation.screen.BookDetailScreen
import com.nortlinos.wearos.presentation.screen.DownloadedScreen
import com.nortlinos.wearos.presentation.screen.HomeScreen
import com.nortlinos.wearos.presentation.screen.LibraryItemsScreen
import com.nortlinos.wearos.presentation.screen.LibraryScreen
import com.nortlinos.wearos.presentation.screen.LoginScreen
import com.nortlinos.wearos.presentation.screen.PlayerScreen
import com.nortlinos.wearos.presentation.screen.RecentPlaybackScreen
import com.nortlinos.wearos.presentation.screen.SearchScreen
import com.nortlinos.wearos.presentation.screen.SettingsScreen
import com.nortlinos.wearos.presentation.viewmodel.LibraryViewModel
import com.nortlinos.wearos.presentation.viewmodel.LoginViewModel
import com.nortlinos.wearos.presentation.viewmodel.PlayerViewModel
import com.nortlinos.wearos.presentation.viewmodel.SettingsViewModel
import com.nortlinos.wearos.tile.TileLaunch

private object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val LIBRARIES = "libraries"
    const val PODCAST_LIBRARIES = "podcasts"
    const val ITEMS = "items/{origin}/{libraryId}"
    const val DETAIL = "detail/{origin}/{itemId}"
    const val DOWNLOADED = "downloaded"
    const val SEARCH = "search"
    const val PLAYER = "player"
    const val RECENT = "recent"
    const val SETTINGS = "settings"
    fun items(id: String, origin: String) = "items/${Uri.encode(origin)}/$id"
    fun detail(id: String, origin: String) = "detail/${Uri.encode(origin)}/$id"
}

@Composable
fun AppNavHost(
    isAmbient: Boolean = false,
    tileLaunch: TileLaunch? = null,
    onTileLaunchHandled: () -> Unit = {}
) {
    val nav = rememberSwipeDismissableNavController()
    val libraryViewModel: LibraryViewModel = hiltViewModel()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val session by libraryViewModel.sessionRepository.session.collectAsState()

    LaunchedEffect(session) {
        if (session == null) {
            nav.navigate(Routes.LOGIN) { popUpTo(0) }
        } else if (nav.currentDestination?.route == Routes.LOGIN) {
            nav.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
        }
    }

    // A tap on the Now Playing tile opens the player on top of Home, so swiping back lands
    // somewhere sensible; "Resume" also starts the book (through the headphone check).
    LaunchedEffect(tileLaunch) {
        val launch = tileLaunch ?: return@LaunchedEffect
        playerViewModel.resume(launch.itemId, launch.originServerUrl, autoPlay = launch.resume)
        playerViewModel.showControlsPage()
        if (nav.currentDestination?.route != Routes.PLAYER) nav.navigate(Routes.PLAYER)
        onTileLaunchHandled()
    }

    SwipeDismissableNavHost(
        navController = nav,
        startDestination = if (session == null) Routes.LOGIN else Routes.HOME
    ) {
        composable(Routes.LOGIN) {
            val downloaded by libraryViewModel.downloaded.collectAsState()
            LoginScreen(
                viewModel = hiltViewModel(),
                hasDownloads = downloaded.isNotEmpty(),
                onListenOffline = { nav.navigate(Routes.DOWNLOADED) }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                libraryViewModel = libraryViewModel,
                playerViewModel = playerViewModel,
                settingsViewModel = settingsViewModel,
                onLibraries = {
                    val libraries = libraryViewModel.libraries.value.filter { it.mediaType != "podcast" }
                    if (libraries.size == 1) {
                        val library = libraries.single()
                        nav.navigate(Routes.items(library.id, library.originServerUrl))
                    }
                    else nav.navigate(Routes.LIBRARIES)
                },
                onPodcasts = {
                    val libraries = libraryViewModel.libraries.value.filter { it.mediaType == "podcast" }
                    if (libraries.size == 1) {
                        val library = libraries.single()
                        nav.navigate(Routes.items(library.id, library.originServerUrl))
                    } else nav.navigate(Routes.PODCAST_LIBRARIES)
                },
                onDownloaded = { nav.navigate(Routes.DOWNLOADED) },
                onSearch = { nav.navigate(Routes.SEARCH) },
                onRecent = { nav.navigate(Routes.RECENT) },
                onNowPlaying = { nav.navigate(Routes.PLAYER) },
                onSettings = { nav.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.LIBRARIES) {
            LibraryScreen(libraryViewModel, podcastsOnly = false) { id, origin ->
                nav.navigate(Routes.items(id, origin))
            }
        }
        composable(Routes.PODCAST_LIBRARIES) {
            LibraryScreen(libraryViewModel, podcastsOnly = true) { id, origin ->
                nav.navigate(Routes.items(id, origin))
            }
        }
        composable(
            Routes.ITEMS,
            arguments = listOf(
                navArgument("origin") { type = NavType.StringType },
                navArgument("libraryId") { type = NavType.StringType }
            )
        ) {
            val origin = Uri.decode(it.arguments?.getString("origin").orEmpty())
            LibraryItemsScreen(
                libraryId = it.arguments?.getString("libraryId").orEmpty(),
                originServerUrl = origin,
                viewModel = libraryViewModel,
                onItem = { id -> nav.navigate(Routes.detail(id, origin)) },
                onNowPlaying = { nav.navigate(Routes.PLAYER) },
                playerViewModel = playerViewModel,
                settingsViewModel = settingsViewModel
            )
        }
        composable(
            Routes.DETAIL,
            arguments = listOf(
                navArgument("origin") { type = NavType.StringType },
                navArgument("itemId") { type = NavType.StringType }
            )
        ) {
            val origin = Uri.decode(it.arguments?.getString("origin").orEmpty())
            BookDetailScreen(
                itemId = it.arguments?.getString("itemId").orEmpty(),
                originServerUrl = origin,
                viewModel = libraryViewModel,
                onPlay = { id ->
                    playerViewModel.play(id, origin)
                    nav.navigate(Routes.PLAYER)
                }
            )
        }
        composable(Routes.DOWNLOADED) {
            DownloadedScreen(
                viewModel = libraryViewModel,
                onItem = { id, origin -> nav.navigate(Routes.detail(id, origin)) }
            )
        }
        composable(Routes.SEARCH) {
            SearchScreen(libraryViewModel) { id, origin ->
                nav.navigate(Routes.detail(id, origin))
            }
        }
        composable(Routes.RECENT) {
            RecentPlaybackScreen(
                libraryViewModel = libraryViewModel,
                onItem = { id, origin -> nav.navigate(Routes.detail(id, origin)) }
            )
        }
        composable(Routes.PLAYER) {
            PlayerScreen(playerViewModel, isAmbient)
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(settingsViewModel)
        }
    }
}

package com.nortlinos.wearos.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.nortlinos.wearos.data.model.Server
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

private val Context.settingsDataStore by preferencesDataStore("display_settings")

enum class ProgressDisplayMode { CHAPTER, BOOK }

@Singleton
class SessionStore @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "encrypted_session",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun get(): Server? {
        val url = prefs.getString(URL, null) ?: return null
        val token = prefs.getString(TOKEN, null) ?: return null
        val userId = prefs.getString(USER_ID, null) ?: return null
        val username = prefs.getString(USERNAME, null) ?: return null
        return Server(url, token, userId, username, prefs.getString(REFRESH_TOKEN, null))
    }

    fun save(server: Server) {
        prefs.edit()
            .putString(URL, server.url)
            .putString(TOKEN, server.token)
            .putString(USER_ID, server.userId)
            .putString(USERNAME, server.username)
            .putString(REFRESH_TOKEN, server.refreshToken)
            .apply()
    }

    fun clear() = prefs.edit().clear().apply()

    private companion object {
        const val URL = "url"
        const val TOKEN = "token"
        const val USER_ID = "user_id"
        const val USERNAME = "username"
        const val REFRESH_TOKEN = "refresh_token"
    }
}

@Singleton
class SettingsStore @Inject constructor(@ApplicationContext private val context: Context) {
    private val chapterMode = booleanPreferencesKey("chapter_progress")
    private val playbackSpeedKey = floatPreferencesKey("playback_speed")
    private val seriesViewKey = booleanPreferencesKey("library_series_view")
    private val homeSearchKey = booleanPreferencesKey("home_search_visible")

    val progressDisplayMode: Flow<ProgressDisplayMode> = context.settingsDataStore.data.map {
        if (it[chapterMode] == true) ProgressDisplayMode.CHAPTER else ProgressDisplayMode.BOOK
    }

    suspend fun setProgressDisplayMode(mode: ProgressDisplayMode) {
        context.settingsDataStore.edit { it[chapterMode] = mode == ProgressDisplayMode.CHAPTER }
    }

    /** Defaults to 1.0x (normal speed) when nothing has been chosen yet. */
    val playbackSpeed: Flow<Float> = context.settingsDataStore.data.map {
        it[playbackSpeedKey] ?: 1.0f
    }

    suspend fun setPlaybackSpeed(speed: Float) {
        context.settingsDataStore.edit { it[playbackSpeedKey] = speed }
    }

    /**
     * Opens libraries in the Series view. Off by default: series browsing must load every page of
     * the library, which costs far more radio time than the paged book list.
     */
    val librarySeriesView: Flow<Boolean> = context.settingsDataStore.data.map {
        it[seriesViewKey] == true
    }

    suspend fun setLibrarySeriesView(enabled: Boolean) {
        context.settingsDataStore.edit { it[seriesViewKey] = enabled }
    }

    /** Shows the Search entry on the main menu. Defaults to on. */
    val homeSearchVisible: Flow<Boolean> = context.settingsDataStore.data.map {
        it[homeSearchKey] ?: true
    }

    suspend fun setHomeSearchVisible(visible: Boolean) {
        context.settingsDataStore.edit { it[homeSearchKey] = visible }
    }
}

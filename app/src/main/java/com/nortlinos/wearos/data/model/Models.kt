package com.nortlinos.wearos.data.model

/**
 * Represents a configured Audiobookshelf server connection.
 */
data class Server(
    val url: String,
    val token: String,
    val userId: String,
    val username: String,
    /**
     * Audiobookshelf 2.26+ issues short-lived access tokens alongside a rotating refresh token.
     * Null on older servers, which hand out a single non-expiring token instead.
     */
    val refreshToken: String? = null
)

data class Library(
    val id: String,
    val name: String,
    val mediaType: String
)

data class LibraryItem(
    val id: String,
    val libraryId: String,
    val mediaType: String,
    val media: Media,
    val updatedAt: Long
)

data class Media(
    val metadata: Metadata,
    val coverPath: String?,
    val tracks: List<AudioTrack>?,
    val duration: Double?,
    val chapters: List<Chapter>? = null,
    val episodes: List<PodcastEpisode>? = null
) {
    /**
     * Gson allocates Kotlin objects without invoking their constructors, so a default value never
     * runs for a key the server omitted. Library pages return minified items with no `chapters`
     * or `tracks` keys at all, which made a declared non-null `List` arrive as null and throw at
     * the first use. Always read collections through these accessors.
     */
    val chapterList: List<Chapter> get() = chapters.orEmpty()

    val trackList: List<AudioTrack> get() = tracks.orEmpty()
    val episodeList: List<PodcastEpisode> get() = episodes.orEmpty()
}

data class PodcastEpisode(
    val id: String,
    val title: String?,
    val subtitle: String? = null,
    val description: String? = null,
    val publishedAt: Long? = null,
    val audioFile: PodcastAudioFile? = null,
    val enclosure: PodcastEnclosure? = null
)

data class PodcastAudioFile(
    val duration: Double? = null,
    val metadata: PodcastAudioMetadata? = null
)

data class PodcastAudioMetadata(val size: Long? = null)

data class PodcastEnclosure(val length: String? = null) {
    val sizeBytes: Long? get() = length?.toLongOrNull()?.takeIf { it > 0 }
}

data class Metadata(
    val title: String?,
    val authorName: String?,
    val description: String?,
    val author: String? = null,
    val seriesName: String? = null,
    val series: List<SeriesRef>? = null,
    val narratorName: String? = null,
    val narrators: List<String>? = null
) {
    val displayAuthor: String?
        get() = authorName?.takeIf { it.isNotBlank() }
            ?: author?.takeIf { it.isNotBlank() }

    /**
     * Minified responses flatten the series to `seriesName`; expanded responses send a `series`
     * array of objects instead. Prefer the flattened string and rebuild "Name #sequence" from the
     * array otherwise.
     */
    val displaySeries: String?
        get() = seriesName?.takeIf { it.isNotBlank() }
            ?: series
                ?.mapNotNull { ref ->
                    ref.name?.takeIf { it.isNotBlank() }?.let { name ->
                        ref.sequence?.takeIf { it.isNotBlank() }?.let { "$name #$it" } ?: name
                    }
                }
                ?.joinToString()
                ?.ifBlank { null }

    /** Expanded responses send `narrators` as plain strings, not objects. */
    val displayNarrator: String?
        get() = narratorName?.takeIf { it.isNotBlank() }
            ?: narrators
                ?.filter { it.isNotBlank() }
                ?.joinToString()
                ?.ifBlank { null }
}

data class SeriesRef(
    val id: String? = null,
    val name: String? = null,
    val sequence: String? = null
)

data class Chapter(
    val id: Int = 0,
    val title: String = "",
    val start: Double = 0.0,
    val end: Double = 0.0
)

data class AudioTrack(
    val index: Int,
    val startOffset: Double,
    val duration: Double,
    val title: String?,
    val contentUrl: String,
    val mimeType: String,
    val metadata: AudioFileMetadata? = null
)

data class AudioFileMetadata(val size: Long = 0)

data class PlaybackSession(
    val id: String,
    val libraryItemId: String,
    val mediaPlayer: String,
    val currentTime: Double,
    val audioTracks: List<AudioTrack>,
    val duration: Double,
    val coverPath: String?
)

data class LoginResponse(
    val user: User,
    val userDefaultLibraryId: String?
)

data class User(
    val id: String,
    val username: String,
    /** Legacy non-expiring token; still populated by older servers, deprecated on 2.26+. */
    val token: String = "",
    /** Short-lived bearer token on Audiobookshelf 2.26+. */
    val accessToken: String? = null,
    /** Long-lived rotating token; only returned to API clients, not browser sessions. */
    val refreshToken: String? = null
) {
    /** The token to authenticate API calls with, preferring the modern short-lived one. */
    val bearerToken: String get() = accessToken?.takeIf { it.isNotBlank() } ?: token
}

data class DownloadedBook(
    val itemId: String,
    val title: String,
    val author: String?,
    val coverPath: String?,
    val tracks: List<DownloadedTrack>
)

data class DownloadedTrack(
    val index: Int,
    val filePath: String,
    val startOffset: Double,
    val duration: Double
)

data class PlaybackProgress(
    val itemId: String,
    val currentTime: Double,
    val duration: Double,
    val needsSync: Boolean,
    val updatedAtEpochMs: Long
)

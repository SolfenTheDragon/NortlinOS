package com.nortlinos.wearos.data.repository

import com.nortlinos.wearos.data.api.ApiClient
import com.nortlinos.wearos.data.api.AudiobookshelfApi
import com.nortlinos.wearos.data.api.ListeningStatsDto
import com.nortlinos.wearos.data.api.MediaProgressDto
import com.nortlinos.wearos.service.ConnectivityMonitor
import java.io.IOException
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/** What the stats screen shows, already reduced to watch-sized pieces. */
data class ListeningStatsSummary(
    val totalSeconds: Long,
    val todaySeconds: Long,
    val daysListened: Int,
    /** Consecutive days with listening, ending today (or yesterday, if not yet today). */
    val currentStreakDays: Int,
    /** Null when the server could not say (the progress request failed). */
    val itemsFinished: Int?,
    /** The last 7 days, oldest first, ending today. */
    val lastSevenDays: List<DayTotal>,
    val topItems: List<TitledTotal>,
    val recentSessions: List<TitledTotal>
) {
    val weekSeconds: Long get() = lastSevenDays.sumOf { it.seconds }
}

data class DayTotal(val date: LocalDate, val label: String, val seconds: Long)

data class TitledTotal(
    val title: String,
    val author: String?,
    val seconds: Long,
    /** The day the session happened; null for per-title totals. */
    val date: LocalDate? = null
)

/** A summary plus when it was fetched, so the screen can say how fresh it is. */
data class TimedStats(val summary: ListeningStatsSummary, val fetchedAt: Long)

/**
 * Reads the account's listening statistics from Audiobookshelf, only when the stats screen asks,
 * so the feature costs no battery otherwise.
 *
 * The last result is kept in memory (never on disk) for the signed-in account. Reopening the
 * screen shortly afterwards then shows it instantly without another request, and losing the
 * connection still leaves the last known figures on screen, labelled with their age.
 */
@Singleton
class ListeningStatsRepository @Inject constructor(
    private val apiClient: ApiClient,
    private val sessionRepository: SessionRepository,
    private val connectivityMonitor: ConnectivityMonitor
) {
    @Volatile private var cache: Pair<String, TimedStats>? = null

    /** The last stats loaded for the account that is signed in now, if any. */
    fun cached(): TimedStats? {
        val key = accountKey() ?: return null
        return cache?.takeIf { it.first == key }?.second
    }

    suspend fun load(): Result<TimedStats> = withContext(Dispatchers.IO) {
        // Taken before the request, so a sign-in change mid-request cannot file one account's
        // figures under another's.
        val key = accountKey()
        fetchSummary().map { summary ->
            val timed = TimedStats(summary, System.currentTimeMillis())
            if (key != null && key == accountKey()) cache = key to timed
            timed
        }
    }

    private fun accountKey(): String? = sessionRepository.session.value?.let {
        "${ServerIdentity.normalize(it.url)}\u0000${it.userId}"
    }

    private suspend fun fetchSummary(): Result<ListeningStatsSummary> = withContext(Dispatchers.IO) {
        if (!connectivityMonitor.hasActiveNetwork()) return@withContext Result.failure(NoNetworkException())
        runCatching {
            val server = sessionRepository.requireServer()
            val first = fetch(apiClient.scopedApi(server.url, server.token))
            val (stats, finished) = if (first.code == 401 || first.code == 403) {
                // Access tokens last an hour; renew once and retry, as other screens do.
                if (!sessionRepository.recoverFromUnauthorized(server.url, server.token)) {
                    throw IOException("Sign in again to load stats")
                }
                val renewed = sessionRepository.requireServer()
                fetch(apiClient.scopedApi(renewed.url, renewed.token)).requireStats()
            } else {
                first.requireStats()
            }
            ListeningStatsMath.summarize(stats, finished, LocalDate.now(), Locale.getDefault())
        }
    }

    private class Fetched(val code: Int, val stats: ListeningStatsDto?, val finished: Int?) {
        fun requireStats(): Pair<ListeningStatsDto, Int?> {
            if (stats == null) throw IOException("Stats: HTTP $code")
            return stats to finished
        }
    }

    private suspend fun fetch(api: AudiobookshelfApi): Fetched = coroutineScope {
        val finished = async { runCatching { finishedCount(api) }.getOrNull() }
        val response = api.getListeningStats()
        val stats = response.body()?.takeIf { response.isSuccessful }
        Fetched(response.code(), stats, finished.await())
    }

    /**
     * Finished books and episodes come from media progress, not sessions. Audiobookshelf 2.36+
     * has a dedicated endpoint; older servers only carry progress inside `/api/me`.
     */
    private suspend fun finishedCount(api: AudiobookshelfApi): Int? {
        val dedicated = api.getAllProgress()
        val progress: List<MediaProgressDto> = when {
            dedicated.isSuccessful -> dedicated.body()?.mediaProgress
            dedicated.code() == 404 -> api.getCurrentUser().takeIf { it.isSuccessful }?.body()?.mediaProgress
            else -> null
        } ?: return null
        return progress.count { it.isFinished }
    }
}

internal object ListeningStatsMath {
    const val TOP_ITEMS = 5
    const val RECENT_SESSIONS = 5

    fun summarize(
        stats: ListeningStatsDto,
        itemsFinished: Int?,
        today: LocalDate,
        locale: Locale
    ): ListeningStatsSummary {
        val days = stats.days.orEmpty()
            .mapNotNull { (key, seconds) ->
                runCatching { LocalDate.parse(key) }.getOrNull()?.let { it to seconds }
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, values) -> values.sum() }
        val lastSeven = (6 downTo 0).map { back ->
            val day = today.minusDays(back.toLong())
            DayTotal(
                date = day,
                label = day.dayOfWeek.getDisplayName(TextStyle.NARROW, locale),
                seconds = (days[day] ?: 0.0).toLong()
            )
        }
        return ListeningStatsSummary(
            totalSeconds = (stats.totalTime ?: 0.0).toLong(),
            // From the same per-day totals as the chart, so "Today" always matches today's bar.
            // (The server's own `today` uses its time zone's date, which can differ.)
            todaySeconds = (days[today] ?: 0.0).toLong(),
            daysListened = days.count { it.value > 0 },
            currentStreakDays = streak(days.filterValues { it > 0 }.keys, today),
            itemsFinished = itemsFinished,
            lastSevenDays = lastSeven,
            topItems = stats.items.orEmpty().values
                .filter { (it.timeListening ?: 0.0) > 0 }
                .sortedByDescending { it.timeListening ?: 0.0 }
                .take(TOP_ITEMS)
                .map { item ->
                    val metadata = item.mediaMetadata
                    TitledTotal(
                        title = metadata?.title?.takeIf { it.isNotBlank() } ?: "Unknown title",
                        author = metadata?.author?.takeIf { it.isNotBlank() }
                            ?: metadata?.authors.orEmpty().mapNotNull { it.name }
                                .joinToString(", ").takeIf { it.isNotBlank() },
                        seconds = (item.timeListening ?: 0.0).toLong()
                    )
                },
            recentSessions = stats.recentSessions.orEmpty()
                .take(RECENT_SESSIONS)
                .map { session ->
                    TitledTotal(
                        title = session.displayTitle?.takeIf { it.isNotBlank() } ?: "Unknown title",
                        author = session.displayAuthor?.takeIf { it.isNotBlank() },
                        seconds = (session.timeListening ?: 0.0).toLong(),
                        date = session.date?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                    )
                }
        )
    }

    /**
     * Counts back from today; a day with no listening yet today does not break a streak that
     * ran through yesterday.
     */
    fun streak(listenedDays: Set<LocalDate>, today: LocalDate): Int {
        var day = if (today in listenedDays) today else today.minusDays(1)
        var count = 0
        while (day in listenedDays) {
            count++
            day = day.minusDays(1)
        }
        return count
    }

    /** Hours above which minutes are dropped: "412h" reads faster than "412h 7m". */
    private const val HOURS_ONLY_FROM = 100

    /** Compact, for display: "0m", "<1m", "42m", "3h 5m", "3h", "412h". */
    fun duration(seconds: Long): String {
        val minutes = seconds.coerceAtLeast(0) / 60
        val hours = minutes / 60
        val rest = minutes % 60
        return when {
            seconds <= 0 -> "0m"
            minutes == 0L -> "<1m"
            hours == 0L -> "${minutes}m"
            hours >= HOURS_ONLY_FROM || rest == 0L -> "${hours}h"
            else -> "${hours}h ${rest}m"
        }
    }

    /** For screen readers, which read "3h 5m" letter by letter: "3 hours 5 minutes". */
    fun spokenDuration(seconds: Long): String {
        val minutes = seconds.coerceAtLeast(0) / 60
        val hours = minutes / 60
        val rest = minutes % 60
        fun unit(n: Long, one: String, many: String) = "$n ${if (n == 1L) one else many}"
        return when {
            seconds <= 0 -> "no listening"
            minutes == 0L -> "less than a minute"
            hours == 0L -> unit(minutes, "minute", "minutes")
            hours >= HOURS_ONLY_FROM || rest == 0L -> unit(hours, "hour", "hours")
            else -> "${unit(hours, "hour", "hours")} ${unit(rest, "minute", "minutes")}"
        }
    }

    /** "Today", "Yesterday", a weekday within the last week, otherwise [olderDate]'s format. */
    fun relativeDay(
        date: LocalDate,
        today: LocalDate,
        locale: Locale,
        olderDate: java.time.format.DateTimeFormatter
    ): String {
        val daysAgo = java.time.temporal.ChronoUnit.DAYS.between(date, today)
        return when {
            daysAgo == 0L -> "Today"
            daysAgo == 1L -> "Yesterday"
            daysAgo in 2..6 -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
            else -> date.format(olderDate)
        }
    }

    /** "just now", "5 min ago", "3 h ago", then a date. */
    fun updatedAgo(fetchedAt: Long, now: Long): String {
        val minutes = (now - fetchedAt).coerceAtLeast(0) / 60_000
        return when {
            minutes < 1 -> "just now"
            minutes < 60 -> "$minutes min ago"
            minutes < 24 * 60 -> "${minutes / 60} h ago"
            else -> "${minutes / (24 * 60)} d ago"
        }
    }
}

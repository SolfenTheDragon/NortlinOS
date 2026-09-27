package com.nortlinos.wearos.presentation.viewmodel

import com.nortlinos.wearos.data.local.DownloadStatus
import com.nortlinos.wearos.data.local.LibraryItemEntity
import java.util.Locale

data class LibrarySeries(
    val name: String,
    val books: List<LibraryItemEntity>
)

/** Groups cached series labels such as "Middle-earth #2" under the shared series name. */
object LibrarySeriesGrouping {
    private val sequenceSuffix = Regex("^(.*?)\\s+#\\s*([0-9]+(?:\\.[0-9]+)?)$")

    fun group(items: List<LibraryItemEntity>): List<LibrarySeries> =
        items.mapNotNull { item ->
            val label = item.series?.trim()?.takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val match = sequenceSuffix.matchEntire(label)
            val name = match?.groupValues?.get(1)?.trim()?.takeIf(String::isNotEmpty) ?: label
            val sequence = match?.groupValues?.get(2)?.toDoubleOrNull()
            Triple(name, sequence, item)
        }
            .groupBy { it.first.lowercase(Locale.ROOT) }
            .values
            .map { entries ->
                val name = entries.first().first
                LibrarySeries(
                    name = name,
                    books = entries.sortedWith(
                        compareBy<Triple<String, Double?, LibraryItemEntity>> { it.second == null }
                            .thenBy { it.second ?: Double.MAX_VALUE }
                            .thenBy { it.third.title.lowercase(Locale.ROOT) }
                    ).map { it.third }
                )
            }
            .sortedBy { it.name.lowercase(Locale.ROOT) }
}

/**
 * Filters the already-loaded series list. Series browsing holds every book in memory, so this
 * needs no server round trip and keeps working offline. A series matches on its own name or on
 * any of its books, so searching a title people remember still finds the series it belongs to.
 */
object LibrarySeriesSearch {
    fun filter(series: List<LibrarySeries>, query: String): List<LibrarySeries> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return series
        return series.filter { candidate ->
            candidate.name.contains(trimmed, ignoreCase = true) ||
                candidate.books.any { book ->
                    book.title.contains(trimmed, ignoreCase = true) ||
                        book.author?.contains(trimmed, ignoreCase = true) == true ||
                        book.narrator?.contains(trimmed, ignoreCase = true) == true
                }
        }
    }
}

data class SeriesDownloadSelection(
    val itemIdsToQueue: List<String>,
    val skippedCount: Int
)

object LibrarySeriesDownloadSelection {
    fun create(
        itemIds: List<String>,
        statuses: Map<String, DownloadStatus>
    ): SeriesDownloadSelection {
        val uniqueIds = itemIds.distinct()
        val pending = uniqueIds.filter { id ->
            statuses[id] !in setOf(
                DownloadStatus.DOWNLOADED,
                DownloadStatus.QUEUED,
                DownloadStatus.DOWNLOADING
            )
        }
        return SeriesDownloadSelection(
            itemIdsToQueue = pending,
            skippedCount = uniqueIds.size - pending.size
        )
    }
}

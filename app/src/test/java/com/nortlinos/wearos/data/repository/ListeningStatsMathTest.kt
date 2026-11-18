package com.nortlinos.wearos.data.repository

import com.google.gson.Gson
import com.nortlinos.wearos.data.api.ListeningStatsDto
import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ListeningStatsMathTest {
    private val today = LocalDate.of(2026, 11, 18) // a Wednesday

    /** Shaped like `GET /api/me/listening-stats` from Audiobookshelf 2.38. */
    private val json = """
        {
          "totalTime": 7530.5,
          "today": 999,
          "days": {"2026-11-18": 600, "2026-11-17": 1200, "2026-11-16": 300,
                   "2026-11-10": 5430.5, "not-a-date": 99},
          "dayOfWeek": {"Wednesday": 600},
          "items": {
            "li1": {"id": "li1", "timeListening": 5000,
                    "mediaMetadata": {"title": "The Hobbit", "authors": [{"id": "a1", "name": "J.R.R. Tolkien"}]}},
            "li2": {"id": "li2", "timeListening": 2530.5,
                    "mediaMetadata": {"title": "Weekly Show", "author": "Host"}},
            "li3": {"id": "li3", "timeListening": 0, "mediaMetadata": {"title": "Opened only"}}
          },
          "recentSessions": [
            {"id": "s1", "displayTitle": "The Hobbit", "displayAuthor": "J.R.R. Tolkien",
             "timeListening": "600", "date": "2026-11-18", "updatedAt": 1763460000000}
          ]
        }
    """

    private fun summary(finished: Int? = 4) = ListeningStatsMath.summarize(
        Gson().fromJson(json, ListeningStatsDto::class.java), finished, today, Locale.US
    )

    @Test
    fun headlineTotals() {
        val s = summary()
        assertEquals(7530L, s.totalSeconds)
        // From the per-day map (matching the chart), not the server-time-zone "today": 999.
        assertEquals(600L, s.todaySeconds)
        assertEquals(2100L, s.weekSeconds)
        assertEquals(4, s.daysListened) // the malformed key is ignored
        assertEquals(3, s.currentStreakDays)
        assertEquals(4, s.itemsFinished)
    }

    @Test
    fun lastSevenDaysEndTodayOldestFirst() {
        val week = summary().lastSevenDays
        assertEquals(listOf("T", "F", "S", "S", "M", "T", "W"), week.map { it.label })
        assertEquals(today, week.last().date)
        assertEquals(listOf(0L, 0L, 0L, 0L, 300L, 1200L, 600L), week.map { it.seconds })
    }

    @Test
    fun topItemsSortedAndNamedForBooksAndPodcasts() {
        val top = summary().topItems
        assertEquals(listOf("The Hobbit", "Weekly Show"), top.map { it.title })
        assertEquals("J.R.R. Tolkien", top[0].author)
        assertEquals("Host", top[1].author)
    }

    @Test
    fun recentSessionsAcceptStringTimes() {
        val recent = summary().recentSessions.single()
        assertEquals(600L, recent.seconds)
        assertEquals(today, recent.date)
        assertEquals("J.R.R. Tolkien", recent.author)
    }

    @Test
    fun unknownFinishedCountStaysUnknown() {
        assertNull(summary(finished = null).itemsFinished)
    }

    @Test
    fun emptyResponseIsAllZeros() {
        val s = ListeningStatsMath.summarize(ListeningStatsDto(), null, today, Locale.US)
        assertEquals(0L, s.totalSeconds)
        assertEquals(0, s.currentStreakDays)
        assertEquals(7, s.lastSevenDays.size)
    }

    @Test
    fun streakSurvivesNoListeningYetToday() {
        val days = setOf(today.minusDays(1), today.minusDays(2), today.minusDays(4))
        assertEquals(2, ListeningStatsMath.streak(days, today))
        assertEquals(0, ListeningStatsMath.streak(setOf(today.minusDays(2)), today))
    }

    @Test
    fun durationFormatting() {
        assertEquals("0m", ListeningStatsMath.duration(0))
        assertEquals("<1m", ListeningStatsMath.duration(59))
        assertEquals("42m", ListeningStatsMath.duration(42 * 60))
        assertEquals("3h 5m", ListeningStatsMath.duration(3 * 3600 + 5 * 60))
        assertEquals("3h", ListeningStatsMath.duration(3 * 3600 + 30))
        assertEquals("412h", ListeningStatsMath.duration(412 * 3600 + 7 * 60))
    }

    @Test
    fun spokenDurationAvoidsAbbreviations() {
        assertEquals("no listening", ListeningStatsMath.spokenDuration(0))
        assertEquals("less than a minute", ListeningStatsMath.spokenDuration(30))
        assertEquals("1 minute", ListeningStatsMath.spokenDuration(60))
        assertEquals("1 hour 5 minutes", ListeningStatsMath.spokenDuration(3600 + 300))
        assertEquals("2 hours", ListeningStatsMath.spokenDuration(7200))
    }

    @Test
    fun relativeDays() {
        val older = java.time.format.DateTimeFormatter.ofPattern("MMM d", Locale.US)
        fun day(back: Long) =
            ListeningStatsMath.relativeDay(today.minusDays(back), today, Locale.US, older)
        assertEquals("Today", day(0))
        assertEquals("Yesterday", day(1))
        assertEquals("Mon", day(2))
        assertEquals("Thu", day(6))
        assertEquals("Nov 11", day(7))
    }

    @Test
    fun updatedAgo() {
        val now = 10_000_000_000L
        assertEquals("just now", ListeningStatsMath.updatedAgo(now - 30_000, now))
        assertEquals("5 min ago", ListeningStatsMath.updatedAgo(now - 5 * 60_000, now))
        assertEquals("3 h ago", ListeningStatsMath.updatedAgo(now - 3 * 3_600_000, now))
        assertEquals("2 d ago", ListeningStatsMath.updatedAgo(now - 2 * 86_400_000, now))
    }
}

package com.nortlinos.wearos.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Upsert
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "libraries", primaryKeys = ["id", "originServerUrl"])
data class LibraryEntity(
    val id: String,
    val originServerUrl: String,
    val name: String,
    val mediaType: String
)

@Entity(
    tableName = "library_items",
    primaryKeys = ["id", "originServerUrl"],
    indices = [
        Index(value = ["libraryId", "originServerUrl"]),
        Index(value = ["parentItemId", "originServerUrl"]),
        Index("title"),
        Index("author"),
        Index("series"),
        Index("narrator")
    ]
)
data class LibraryItemEntity(
    val id: String,
    val originServerUrl: String,
    val libraryId: String,
    val mediaType: String,
    val title: String,
    val author: String?,
    val series: String?,
    val narrator: String?,
    val description: String?,
    val coverPath: String?,
    val localCoverPath: String?,
    val durationMs: Long,
    val updatedAt: Long,
    val parentItemId: String? = null,
    val remoteSizeBytes: Long = 0
)

@Entity(
    tableName = "chapters",
    primaryKeys = ["itemId", "originServerUrl", "chapterId"],
    foreignKeys = [
        ForeignKey(
            entity = LibraryItemEntity::class,
            parentColumns = ["id", "originServerUrl"],
            childColumns = ["itemId", "originServerUrl"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["itemId", "originServerUrl"])]
)
data class ChapterEntity(
    val itemId: String,
    val originServerUrl: String,
    val chapterId: Int,
    val title: String,
    val startMs: Long,
    val endMs: Long
)

enum class DownloadStatus { QUEUED, DOWNLOADING, PAUSED, DOWNLOADED, FAILED }

@Entity(
    tableName = "downloads",
    primaryKeys = ["itemId", "originServerUrl"],
    foreignKeys = [
        ForeignKey(
            entity = LibraryItemEntity::class,
            parentColumns = ["id", "originServerUrl"],
            childColumns = ["itemId", "originServerUrl"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["itemId", "originServerUrl"])]
)
data class DownloadedItemEntity(
    val itemId: String,
    val originServerUrl: String,
    val status: DownloadStatus,
    val localDirectory: String,
    val fileSizeBytes: Long,
    val downloadedBytes: Long,
    val error: String? = null
)

@Entity(
    tableName = "download_tracks",
    primaryKeys = ["itemId", "originServerUrl", "trackIndex"],
    indices = [Index(value = ["itemId", "originServerUrl"])]
)
data class DownloadTrackEntity(
    val itemId: String,
    val originServerUrl: String,
    val trackIndex: Int,
    val localPath: String,
    val contentUrl: String,
    val mimeType: String,
    val startOffsetMs: Long,
    val durationMs: Long,
    val sizeBytes: Long
)

@Entity(tableName = "progress", primaryKeys = ["itemId", "originServerUrl"])
data class ProgressEntity(
    val itemId: String,
    val originServerUrl: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long,
    val dirty: Boolean,
    val conflictPositionMs: Long? = null,
    val conflictUpdatedAt: Long? = null
)

data class RecentPlaybackItem(
    val itemId: String,
    val originServerUrl: String,
    val title: String,
    val author: String?,
    val coverPath: String?,
    val localCoverPath: String?,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long,
    val downloaded: Boolean
)

data class DownloadedBookData(
    val item: LibraryItemEntity,
    val download: DownloadedItemEntity?,
    val tracks: List<DownloadTrackEntity>,
    val chapters: List<ChapterEntity>
)

@Dao
interface LibraryDao {
    @Query("SELECT * FROM libraries WHERE originServerUrl = :originServerUrl ORDER BY name COLLATE NOCASE")
    fun observeLibraries(originServerUrl: String): Flow<List<LibraryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLibraries(libraries: List<LibraryEntity>)

    @Query("SELECT * FROM library_items WHERE libraryId = :libraryId AND originServerUrl = :originServerUrl AND parentItemId IS NULL ORDER BY title COLLATE NOCASE")
    fun observeItems(libraryId: String, originServerUrl: String): Flow<List<LibraryItemEntity>>

    @Query("SELECT * FROM library_items WHERE libraryId = :libraryId AND originServerUrl = :originServerUrl AND parentItemId IS NULL ORDER BY title COLLATE NOCASE")
    suspend fun getAllItems(libraryId: String, originServerUrl: String): List<LibraryItemEntity>

    @Query(
        """SELECT * FROM library_items
           WHERE libraryId = :libraryId AND originServerUrl = :originServerUrl AND parentItemId IS NULL
           ORDER BY title COLLATE NOCASE
           LIMIT :limit OFFSET :offset"""
    )
    suspend fun getItemsPage(
        libraryId: String,
        originServerUrl: String,
        limit: Int,
        offset: Int
    ): List<LibraryItemEntity>

    @Query("SELECT * FROM library_items WHERE id = :itemId AND originServerUrl = :originServerUrl")
    fun observeItem(itemId: String, originServerUrl: String): Flow<LibraryItemEntity?>

    @Query("SELECT * FROM library_items WHERE id = :itemId AND originServerUrl = :originServerUrl")
    suspend fun getItem(itemId: String, originServerUrl: String): LibraryItemEntity?

    @Query(
        """SELECT * FROM library_items
           WHERE parentItemId = :podcastId AND originServerUrl = :originServerUrl
           ORDER BY updatedAt DESC, title COLLATE NOCASE"""
    )
    fun observePodcastEpisodes(
        podcastId: String,
        originServerUrl: String
    ): Flow<List<LibraryItemEntity>>

    @Query("SELECT * FROM library_items WHERE originServerUrl = :originServerUrl AND id IN (:itemIds)")
    suspend fun getItems(itemIds: List<String>, originServerUrl: String): List<LibraryItemEntity>

    @Query("UPDATE library_items SET localCoverPath = :path WHERE id = :itemId AND originServerUrl = :originServerUrl")
    suspend fun setLocalCoverPath(itemId: String, originServerUrl: String, path: String?)

    @Query(
        """SELECT * FROM library_items
           WHERE originServerUrl = :originServerUrl
             AND parentItemId IS NULL AND mediaType != 'podcast' AND (
              title LIKE '%' || :query || '%' COLLATE NOCASE
              OR author LIKE '%' || :query || '%' COLLATE NOCASE
              OR series LIKE '%' || :query || '%' COLLATE NOCASE
              OR narrator LIKE '%' || :query || '%' COLLATE NOCASE)
           ORDER BY title COLLATE NOCASE"""
    )
    fun search(query: String, originServerUrl: String): Flow<List<LibraryItemEntity>>

    @Query(
        """SELECT * FROM library_items
           WHERE libraryId = :libraryId AND originServerUrl = :originServerUrl
              AND parentItemId IS NULL AND (
              title LIKE '%' || :query || '%' COLLATE NOCASE
              OR author LIKE '%' || :query || '%' COLLATE NOCASE
              OR series LIKE '%' || :query || '%' COLLATE NOCASE
              OR narrator LIKE '%' || :query || '%' COLLATE NOCASE)
           ORDER BY title COLLATE NOCASE
           LIMIT :limit"""
    )
    suspend fun searchLibrary(
        libraryId: String,
        originServerUrl: String,
        query: String,
        limit: Int
    ): List<LibraryItemEntity>

    @Upsert
    suspend fun upsertItems(items: List<LibraryItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChapters(chapters: List<ChapterEntity>)

    @Query("DELETE FROM chapters WHERE itemId = :itemId AND originServerUrl = :originServerUrl")
    suspend fun deleteChapters(itemId: String, originServerUrl: String)

    @Query("SELECT * FROM chapters WHERE itemId = :itemId AND originServerUrl = :originServerUrl ORDER BY startMs")
    fun observeChapters(itemId: String, originServerUrl: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE itemId = :itemId AND originServerUrl = :originServerUrl ORDER BY startMs")
    suspend fun getChapters(itemId: String, originServerUrl: String): List<ChapterEntity>

    @Transaction
    suspend fun replaceChapters(itemId: String, originServerUrl: String, chapters: List<ChapterEntity>) {
        deleteChapters(itemId, originServerUrl)
        upsertChapters(chapters)
    }
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads")
    fun observeAll(): Flow<List<DownloadedItemEntity>>

    @Query(
        """SELECT * FROM library_items WHERE EXISTS (
             SELECT 1 FROM downloads d
             WHERE d.itemId = library_items.id
               AND d.originServerUrl = library_items.originServerUrl
               AND d.status = 'DOWNLOADED'
           )
           ORDER BY title COLLATE NOCASE"""
    )
    fun observeDownloadedItems(): Flow<List<LibraryItemEntity>>

    @Query("SELECT * FROM download_tracks WHERE itemId = :itemId AND originServerUrl = :originServerUrl ORDER BY trackIndex")
    suspend fun getTracks(itemId: String, originServerUrl: String): List<DownloadTrackEntity>

    @Query("SELECT * FROM downloads WHERE itemId = :itemId AND originServerUrl = :originServerUrl")
    fun observeDownload(itemId: String, originServerUrl: String): Flow<DownloadedItemEntity?>

    @Query("SELECT * FROM downloads WHERE itemId = :itemId AND originServerUrl = :originServerUrl")
    suspend fun getDownload(itemId: String, originServerUrl: String): DownloadedItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDownload(download: DownloadedItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTracks(tracks: List<DownloadTrackEntity>)

    @Query("DELETE FROM download_tracks WHERE itemId = :itemId AND originServerUrl = :originServerUrl")
    suspend fun deleteTracks(itemId: String, originServerUrl: String)

    @Query("DELETE FROM downloads WHERE itemId = :itemId AND originServerUrl = :originServerUrl")
    suspend fun deleteDownload(itemId: String, originServerUrl: String)

}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress WHERE itemId = :itemId AND originServerUrl = :originServerUrl")
    fun observe(itemId: String, originServerUrl: String): Flow<ProgressEntity?>

    @Query("SELECT * FROM progress WHERE itemId = :itemId AND originServerUrl = :originServerUrl")
    suspend fun get(itemId: String, originServerUrl: String): ProgressEntity?

    @Query("SELECT * FROM progress WHERE originServerUrl = :originServerUrl")
    suspend fun allForServer(originServerUrl: String): List<ProgressEntity>

    @Query(
        """SELECT * FROM progress
           WHERE originServerUrl = :originServerUrl
             AND positionMs > 0
             AND (durationMs <= 0 OR positionMs < durationMs)
           ORDER BY updatedAt DESC
           LIMIT :limit"""
    )
    fun observeRecentProgressForServer(
        originServerUrl: String,
        limit: Int
    ): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM progress WHERE conflictPositionMs IS NOT NULL")
    fun observeConflicts(): Flow<List<ProgressEntity>>

    @Query(
        """SELECT p.itemId, p.originServerUrl, i.title, i.author, i.coverPath,
                  i.localCoverPath, p.positionMs, p.durationMs, p.updatedAt,
                  EXISTS (
                      SELECT 1 FROM downloads d
                      WHERE d.itemId = p.itemId
                        AND d.originServerUrl = p.originServerUrl
                        AND d.status = 'DOWNLOADED'
                  ) AS downloaded
           FROM progress p
           INNER JOIN library_items i
             ON i.id = p.itemId AND i.originServerUrl = p.originServerUrl
           WHERE p.originServerUrl = :originServerUrl
             AND p.positionMs > 0
             AND i.mediaType != 'podcastEpisode'
             AND (p.durationMs <= 0 OR p.positionMs < p.durationMs)
           ORDER BY p.updatedAt DESC
           LIMIT :limit"""
    )
    fun observeRecentForServer(
        originServerUrl: String,
        limit: Int
    ): Flow<List<RecentPlaybackItem>>

    @Query(
        """SELECT p.itemId, p.originServerUrl, i.title, i.author, i.coverPath,
                  i.localCoverPath, p.positionMs, p.durationMs, p.updatedAt,
                  1 AS downloaded
           FROM progress p
           INNER JOIN library_items i
             ON i.id = p.itemId AND i.originServerUrl = p.originServerUrl
           INNER JOIN downloads d
             ON d.itemId = p.itemId
            AND d.originServerUrl = p.originServerUrl
            AND d.status = 'DOWNLOADED'
           WHERE p.positionMs > 0
             AND i.mediaType != 'podcastEpisode'
             AND (p.durationMs <= 0 OR p.positionMs < p.durationMs)
           ORDER BY p.updatedAt DESC
           LIMIT :limit"""
    )
    fun observeRecentDownloaded(limit: Int): Flow<List<RecentPlaybackItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: ProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(progress: List<ProgressEntity>)

    @Query(
        """UPDATE progress SET dirty = 0
           WHERE itemId = :itemId AND originServerUrl = :originServerUrl
           AND updatedAt = :expectedUpdatedAt"""
    )
    suspend fun markCleanIfUnchanged(
        itemId: String,
        originServerUrl: String,
        expectedUpdatedAt: Long
    ): Int
}

@Database(
    entities = [
        LibraryEntity::class,
        LibraryItemEntity::class,
        ChapterEntity::class,
        DownloadedItemEntity::class,
        DownloadTrackEntity::class,
        ProgressEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao
    abstract fun downloadDao(): DownloadDao
    abstract fun progressDao(): ProgressDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE downloads ADD COLUMN originServerUrl TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    """CREATE TABLE progress_new (
                       itemId TEXT NOT NULL,
                       originServerUrl TEXT NOT NULL,
                       positionMs INTEGER NOT NULL,
                       durationMs INTEGER NOT NULL,
                       updatedAt INTEGER NOT NULL,
                       dirty INTEGER NOT NULL,
                       conflictPositionMs INTEGER,
                       conflictUpdatedAt INTEGER,
                       PRIMARY KEY(itemId, originServerUrl)
                    )"""
                )
                db.execSQL(
                    """INSERT INTO progress_new (
                       itemId, originServerUrl, positionMs, durationMs, updatedAt, dirty,
                       conflictPositionMs, conflictUpdatedAt
                    )
                    SELECT itemId, '', positionMs, durationMs, updatedAt, dirty,
                           conflictPositionMs, conflictUpdatedAt
                    FROM progress"""
                )
                db.execSQL("DROP TABLE progress")
                db.execSQL("ALTER TABLE progress_new RENAME TO progress")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE libraries_new (
                       id TEXT NOT NULL,
                       originServerUrl TEXT NOT NULL,
                       name TEXT NOT NULL,
                       mediaType TEXT NOT NULL,
                       PRIMARY KEY(id, originServerUrl)
                    )"""
                )
                db.execSQL(
                    """INSERT INTO libraries_new
                       SELECT id, '', name, mediaType FROM libraries"""
                )
                db.execSQL(
                    """CREATE TABLE library_items_new (
                       id TEXT NOT NULL,
                       originServerUrl TEXT NOT NULL,
                       libraryId TEXT NOT NULL,
                       mediaType TEXT NOT NULL,
                       title TEXT NOT NULL,
                       author TEXT,
                       series TEXT,
                       narrator TEXT,
                       description TEXT,
                       coverPath TEXT,
                       localCoverPath TEXT,
                       durationMs INTEGER NOT NULL,
                       updatedAt INTEGER NOT NULL,
                       PRIMARY KEY(id, originServerUrl)
                    )"""
                )
                db.execSQL(
                    """INSERT INTO library_items_new
                       SELECT li.id, COALESCE(d.originServerUrl, ''), li.libraryId, li.mediaType,
                              li.title, li.author, li.series, li.narrator, li.description,
                              li.coverPath, li.localCoverPath, li.durationMs, li.updatedAt
                       FROM library_items li
                       LEFT JOIN downloads d ON d.itemId = li.id"""
                )
                db.execSQL(
                    """CREATE TABLE chapters_new (
                       itemId TEXT NOT NULL,
                       originServerUrl TEXT NOT NULL,
                       chapterId INTEGER NOT NULL,
                       title TEXT NOT NULL,
                       startMs INTEGER NOT NULL,
                       endMs INTEGER NOT NULL,
                       PRIMARY KEY(itemId, originServerUrl, chapterId),
                       FOREIGN KEY(itemId, originServerUrl)
                         REFERENCES library_items_new(id, originServerUrl) ON DELETE CASCADE
                    )"""
                )
                db.execSQL(
                    """INSERT INTO chapters_new
                       SELECT c.itemId, COALESCE(d.originServerUrl, ''), c.chapterId,
                              c.title, c.startMs, c.endMs
                       FROM chapters c
                       LEFT JOIN downloads d ON d.itemId = c.itemId"""
                )
                db.execSQL(
                    """CREATE TABLE downloads_new (
                       itemId TEXT NOT NULL,
                       originServerUrl TEXT NOT NULL,
                       status TEXT NOT NULL,
                       localDirectory TEXT NOT NULL,
                       fileSizeBytes INTEGER NOT NULL,
                       downloadedBytes INTEGER NOT NULL,
                       error TEXT,
                       PRIMARY KEY(itemId, originServerUrl),
                       FOREIGN KEY(itemId, originServerUrl)
                         REFERENCES library_items_new(id, originServerUrl) ON DELETE CASCADE
                    )"""
                )
                db.execSQL(
                    """INSERT INTO downloads_new
                       SELECT itemId, originServerUrl, status, localDirectory,
                              fileSizeBytes, downloadedBytes, error
                       FROM downloads"""
                )
                db.execSQL(
                    """CREATE TABLE download_tracks_new (
                       itemId TEXT NOT NULL,
                       originServerUrl TEXT NOT NULL,
                       trackIndex INTEGER NOT NULL,
                       localPath TEXT NOT NULL,
                       contentUrl TEXT NOT NULL,
                       mimeType TEXT NOT NULL,
                       startOffsetMs INTEGER NOT NULL,
                       durationMs INTEGER NOT NULL,
                       sizeBytes INTEGER NOT NULL,
                       PRIMARY KEY(itemId, originServerUrl, trackIndex)
                    )"""
                )
                db.execSQL(
                    """INSERT INTO download_tracks_new
                       SELECT t.itemId, COALESCE(d.originServerUrl, ''), t.trackIndex,
                              t.localPath, t.contentUrl, t.mimeType, t.startOffsetMs,
                              t.durationMs, t.sizeBytes
                       FROM download_tracks t
                       LEFT JOIN downloads d ON d.itemId = t.itemId"""
                )
                db.execSQL("DROP TABLE chapters")
                db.execSQL("DROP TABLE download_tracks")
                db.execSQL("DROP TABLE downloads")
                db.execSQL("DROP TABLE library_items")
                db.execSQL("DROP TABLE libraries")
                db.execSQL("ALTER TABLE libraries_new RENAME TO libraries")
                db.execSQL("ALTER TABLE library_items_new RENAME TO library_items")
                db.execSQL("ALTER TABLE chapters_new RENAME TO chapters")
                db.execSQL("ALTER TABLE downloads_new RENAME TO downloads")
                db.execSQL("ALTER TABLE download_tracks_new RENAME TO download_tracks")
                db.execSQL("CREATE INDEX index_library_items_libraryId_originServerUrl ON library_items(libraryId, originServerUrl)")
                db.execSQL("CREATE INDEX index_library_items_title ON library_items(title)")
                db.execSQL("CREATE INDEX index_library_items_author ON library_items(author)")
                db.execSQL("CREATE INDEX index_library_items_series ON library_items(series)")
                db.execSQL("CREATE INDEX index_library_items_narrator ON library_items(narrator)")
                db.execSQL("CREATE INDEX index_chapters_itemId_originServerUrl ON chapters(itemId, originServerUrl)")
                db.execSQL("CREATE INDEX index_downloads_itemId_originServerUrl ON downloads(itemId, originServerUrl)")
                db.execSQL("CREATE INDEX index_download_tracks_itemId_originServerUrl ON download_tracks(itemId, originServerUrl)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE library_items ADD COLUMN parentItemId TEXT")
                db.execSQL(
                    "CREATE INDEX index_library_items_parentItemId_originServerUrl ON library_items(parentItemId, originServerUrl)"
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE library_items ADD COLUMN remoteSizeBytes INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "audiobookshelf.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
    }
}

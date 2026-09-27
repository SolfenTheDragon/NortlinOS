package com.nortlinos.wearos.`data`.local

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _libraryDao: Lazy<LibraryDao> = lazy {
    LibraryDao_Impl(this)
  }

  private val _downloadDao: Lazy<DownloadDao> = lazy {
    DownloadDao_Impl(this)
  }

  private val _progressDao: Lazy<ProgressDao> = lazy {
    ProgressDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(3, "7c520367a360a85452c1714f29c7b374", "907f8cdc525fe64613ed51da792deaad") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `libraries` (`id` TEXT NOT NULL, `originServerUrl` TEXT NOT NULL, `name` TEXT NOT NULL, `mediaType` TEXT NOT NULL, PRIMARY KEY(`id`, `originServerUrl`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `library_items` (`id` TEXT NOT NULL, `originServerUrl` TEXT NOT NULL, `libraryId` TEXT NOT NULL, `mediaType` TEXT NOT NULL, `title` TEXT NOT NULL, `author` TEXT, `series` TEXT, `narrator` TEXT, `description` TEXT, `coverPath` TEXT, `localCoverPath` TEXT, `durationMs` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`, `originServerUrl`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_library_items_libraryId_originServerUrl` ON `library_items` (`libraryId`, `originServerUrl`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_library_items_title` ON `library_items` (`title`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_library_items_author` ON `library_items` (`author`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_library_items_series` ON `library_items` (`series`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_library_items_narrator` ON `library_items` (`narrator`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `chapters` (`itemId` TEXT NOT NULL, `originServerUrl` TEXT NOT NULL, `chapterId` INTEGER NOT NULL, `title` TEXT NOT NULL, `startMs` INTEGER NOT NULL, `endMs` INTEGER NOT NULL, PRIMARY KEY(`itemId`, `originServerUrl`, `chapterId`), FOREIGN KEY(`itemId`, `originServerUrl`) REFERENCES `library_items`(`id`, `originServerUrl`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_chapters_itemId_originServerUrl` ON `chapters` (`itemId`, `originServerUrl`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `downloads` (`itemId` TEXT NOT NULL, `originServerUrl` TEXT NOT NULL, `status` TEXT NOT NULL, `localDirectory` TEXT NOT NULL, `fileSizeBytes` INTEGER NOT NULL, `downloadedBytes` INTEGER NOT NULL, `error` TEXT, PRIMARY KEY(`itemId`, `originServerUrl`), FOREIGN KEY(`itemId`, `originServerUrl`) REFERENCES `library_items`(`id`, `originServerUrl`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_downloads_itemId_originServerUrl` ON `downloads` (`itemId`, `originServerUrl`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `download_tracks` (`itemId` TEXT NOT NULL, `originServerUrl` TEXT NOT NULL, `trackIndex` INTEGER NOT NULL, `localPath` TEXT NOT NULL, `contentUrl` TEXT NOT NULL, `mimeType` TEXT NOT NULL, `startOffsetMs` INTEGER NOT NULL, `durationMs` INTEGER NOT NULL, `sizeBytes` INTEGER NOT NULL, PRIMARY KEY(`itemId`, `originServerUrl`, `trackIndex`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_download_tracks_itemId_originServerUrl` ON `download_tracks` (`itemId`, `originServerUrl`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `progress` (`itemId` TEXT NOT NULL, `originServerUrl` TEXT NOT NULL, `positionMs` INTEGER NOT NULL, `durationMs` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `dirty` INTEGER NOT NULL, `conflictPositionMs` INTEGER, `conflictUpdatedAt` INTEGER, PRIMARY KEY(`itemId`, `originServerUrl`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '7c520367a360a85452c1714f29c7b374')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `libraries`")
        connection.execSQL("DROP TABLE IF EXISTS `library_items`")
        connection.execSQL("DROP TABLE IF EXISTS `chapters`")
        connection.execSQL("DROP TABLE IF EXISTS `downloads`")
        connection.execSQL("DROP TABLE IF EXISTS `download_tracks`")
        connection.execSQL("DROP TABLE IF EXISTS `progress`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsLibraries: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsLibraries.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLibraries.put("originServerUrl", TableInfo.Column("originServerUrl", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLibraries.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLibraries.put("mediaType", TableInfo.Column("mediaType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysLibraries: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesLibraries: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoLibraries: TableInfo = TableInfo("libraries", _columnsLibraries, _foreignKeysLibraries, _indicesLibraries)
        val _existingLibraries: TableInfo = read(connection, "libraries")
        if (!_infoLibraries.equals(_existingLibraries)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |libraries(com.nortlinos.wearos.data.local.LibraryEntity).
              | Expected:
              |""".trimMargin() + _infoLibraries + """
              |
              | Found:
              |""".trimMargin() + _existingLibraries)
        }
        val _columnsLibraryItems: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsLibraryItems.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLibraryItems.put("originServerUrl", TableInfo.Column("originServerUrl", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLibraryItems.put("libraryId", TableInfo.Column("libraryId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLibraryItems.put("mediaType", TableInfo.Column("mediaType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLibraryItems.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLibraryItems.put("author", TableInfo.Column("author", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLibraryItems.put("series", TableInfo.Column("series", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLibraryItems.put("narrator", TableInfo.Column("narrator", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLibraryItems.put("description", TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLibraryItems.put("coverPath", TableInfo.Column("coverPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLibraryItems.put("localCoverPath", TableInfo.Column("localCoverPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLibraryItems.put("durationMs", TableInfo.Column("durationMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLibraryItems.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysLibraryItems: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesLibraryItems: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesLibraryItems.add(TableInfo.Index("index_library_items_libraryId_originServerUrl", false, listOf("libraryId", "originServerUrl"), listOf("ASC", "ASC")))
        _indicesLibraryItems.add(TableInfo.Index("index_library_items_title", false, listOf("title"), listOf("ASC")))
        _indicesLibraryItems.add(TableInfo.Index("index_library_items_author", false, listOf("author"), listOf("ASC")))
        _indicesLibraryItems.add(TableInfo.Index("index_library_items_series", false, listOf("series"), listOf("ASC")))
        _indicesLibraryItems.add(TableInfo.Index("index_library_items_narrator", false, listOf("narrator"), listOf("ASC")))
        val _infoLibraryItems: TableInfo = TableInfo("library_items", _columnsLibraryItems, _foreignKeysLibraryItems, _indicesLibraryItems)
        val _existingLibraryItems: TableInfo = read(connection, "library_items")
        if (!_infoLibraryItems.equals(_existingLibraryItems)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |library_items(com.nortlinos.wearos.data.local.LibraryItemEntity).
              | Expected:
              |""".trimMargin() + _infoLibraryItems + """
              |
              | Found:
              |""".trimMargin() + _existingLibraryItems)
        }
        val _columnsChapters: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsChapters.put("itemId", TableInfo.Column("itemId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChapters.put("originServerUrl", TableInfo.Column("originServerUrl", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChapters.put("chapterId", TableInfo.Column("chapterId", "INTEGER", true, 3, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChapters.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChapters.put("startMs", TableInfo.Column("startMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChapters.put("endMs", TableInfo.Column("endMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysChapters: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysChapters.add(TableInfo.ForeignKey("library_items", "CASCADE", "NO ACTION", listOf("itemId", "originServerUrl"), listOf("id", "originServerUrl")))
        val _indicesChapters: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesChapters.add(TableInfo.Index("index_chapters_itemId_originServerUrl", false, listOf("itemId", "originServerUrl"), listOf("ASC", "ASC")))
        val _infoChapters: TableInfo = TableInfo("chapters", _columnsChapters, _foreignKeysChapters, _indicesChapters)
        val _existingChapters: TableInfo = read(connection, "chapters")
        if (!_infoChapters.equals(_existingChapters)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |chapters(com.nortlinos.wearos.data.local.ChapterEntity).
              | Expected:
              |""".trimMargin() + _infoChapters + """
              |
              | Found:
              |""".trimMargin() + _existingChapters)
        }
        val _columnsDownloads: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDownloads.put("itemId", TableInfo.Column("itemId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloads.put("originServerUrl", TableInfo.Column("originServerUrl", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloads.put("status", TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloads.put("localDirectory", TableInfo.Column("localDirectory", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloads.put("fileSizeBytes", TableInfo.Column("fileSizeBytes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloads.put("downloadedBytes", TableInfo.Column("downloadedBytes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloads.put("error", TableInfo.Column("error", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDownloads: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysDownloads.add(TableInfo.ForeignKey("library_items", "CASCADE", "NO ACTION", listOf("itemId", "originServerUrl"), listOf("id", "originServerUrl")))
        val _indicesDownloads: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesDownloads.add(TableInfo.Index("index_downloads_itemId_originServerUrl", false, listOf("itemId", "originServerUrl"), listOf("ASC", "ASC")))
        val _infoDownloads: TableInfo = TableInfo("downloads", _columnsDownloads, _foreignKeysDownloads, _indicesDownloads)
        val _existingDownloads: TableInfo = read(connection, "downloads")
        if (!_infoDownloads.equals(_existingDownloads)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |downloads(com.nortlinos.wearos.data.local.DownloadedItemEntity).
              | Expected:
              |""".trimMargin() + _infoDownloads + """
              |
              | Found:
              |""".trimMargin() + _existingDownloads)
        }
        val _columnsDownloadTracks: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDownloadTracks.put("itemId", TableInfo.Column("itemId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadTracks.put("originServerUrl", TableInfo.Column("originServerUrl", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadTracks.put("trackIndex", TableInfo.Column("trackIndex", "INTEGER", true, 3, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadTracks.put("localPath", TableInfo.Column("localPath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadTracks.put("contentUrl", TableInfo.Column("contentUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadTracks.put("mimeType", TableInfo.Column("mimeType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadTracks.put("startOffsetMs", TableInfo.Column("startOffsetMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadTracks.put("durationMs", TableInfo.Column("durationMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDownloadTracks.put("sizeBytes", TableInfo.Column("sizeBytes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDownloadTracks: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDownloadTracks: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesDownloadTracks.add(TableInfo.Index("index_download_tracks_itemId_originServerUrl", false, listOf("itemId", "originServerUrl"), listOf("ASC", "ASC")))
        val _infoDownloadTracks: TableInfo = TableInfo("download_tracks", _columnsDownloadTracks, _foreignKeysDownloadTracks, _indicesDownloadTracks)
        val _existingDownloadTracks: TableInfo = read(connection, "download_tracks")
        if (!_infoDownloadTracks.equals(_existingDownloadTracks)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |download_tracks(com.nortlinos.wearos.data.local.DownloadTrackEntity).
              | Expected:
              |""".trimMargin() + _infoDownloadTracks + """
              |
              | Found:
              |""".trimMargin() + _existingDownloadTracks)
        }
        val _columnsProgress: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsProgress.put("itemId", TableInfo.Column("itemId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgress.put("originServerUrl", TableInfo.Column("originServerUrl", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgress.put("positionMs", TableInfo.Column("positionMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgress.put("durationMs", TableInfo.Column("durationMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgress.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgress.put("dirty", TableInfo.Column("dirty", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgress.put("conflictPositionMs", TableInfo.Column("conflictPositionMs", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgress.put("conflictUpdatedAt", TableInfo.Column("conflictUpdatedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysProgress: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesProgress: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoProgress: TableInfo = TableInfo("progress", _columnsProgress, _foreignKeysProgress, _indicesProgress)
        val _existingProgress: TableInfo = read(connection, "progress")
        if (!_infoProgress.equals(_existingProgress)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |progress(com.nortlinos.wearos.data.local.ProgressEntity).
              | Expected:
              |""".trimMargin() + _infoProgress + """
              |
              | Found:
              |""".trimMargin() + _existingProgress)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "libraries", "library_items", "chapters", "downloads", "download_tracks", "progress")
  }

  public override fun clearAllTables() {
    super.performClear(true, "libraries", "library_items", "chapters", "downloads", "download_tracks", "progress")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(LibraryDao::class, LibraryDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(DownloadDao::class, DownloadDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ProgressDao::class, ProgressDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun libraryDao(): LibraryDao = _libraryDao.value

  public override fun downloadDao(): DownloadDao = _downloadDao.value

  public override fun progressDao(): ProgressDao = _progressDao.value
}

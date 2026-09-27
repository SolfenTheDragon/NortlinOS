package com.nortlinos.wearos.`data`.local

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performInTransactionSuspending
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class LibraryDao_Impl(
  __db: RoomDatabase,
) : LibraryDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfLibraryEntity: EntityInsertAdapter<LibraryEntity>

  private val __insertAdapterOfChapterEntity: EntityInsertAdapter<ChapterEntity>

  private val __upsertAdapterOfLibraryItemEntity: EntityUpsertAdapter<LibraryItemEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfLibraryEntity = object : EntityInsertAdapter<LibraryEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `libraries` (`id`,`originServerUrl`,`name`,`mediaType`) VALUES (?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: LibraryEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.originServerUrl)
        statement.bindText(3, entity.name)
        statement.bindText(4, entity.mediaType)
      }
    }
    this.__insertAdapterOfChapterEntity = object : EntityInsertAdapter<ChapterEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `chapters` (`itemId`,`originServerUrl`,`chapterId`,`title`,`startMs`,`endMs`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ChapterEntity) {
        statement.bindText(1, entity.itemId)
        statement.bindText(2, entity.originServerUrl)
        statement.bindLong(3, entity.chapterId.toLong())
        statement.bindText(4, entity.title)
        statement.bindLong(5, entity.startMs)
        statement.bindLong(6, entity.endMs)
      }
    }
    this.__upsertAdapterOfLibraryItemEntity = EntityUpsertAdapter<LibraryItemEntity>(object : EntityInsertAdapter<LibraryItemEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `library_items` (`id`,`originServerUrl`,`libraryId`,`mediaType`,`title`,`author`,`series`,`narrator`,`description`,`coverPath`,`localCoverPath`,`durationMs`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: LibraryItemEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.originServerUrl)
        statement.bindText(3, entity.libraryId)
        statement.bindText(4, entity.mediaType)
        statement.bindText(5, entity.title)
        val _tmpAuthor: String? = entity.author
        if (_tmpAuthor == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpAuthor)
        }
        val _tmpSeries: String? = entity.series
        if (_tmpSeries == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpSeries)
        }
        val _tmpNarrator: String? = entity.narrator
        if (_tmpNarrator == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpNarrator)
        }
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpDescription)
        }
        val _tmpCoverPath: String? = entity.coverPath
        if (_tmpCoverPath == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpCoverPath)
        }
        val _tmpLocalCoverPath: String? = entity.localCoverPath
        if (_tmpLocalCoverPath == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, _tmpLocalCoverPath)
        }
        statement.bindLong(12, entity.durationMs)
        statement.bindLong(13, entity.updatedAt)
      }
    }, object : EntityDeleteOrUpdateAdapter<LibraryItemEntity>() {
      protected override fun createQuery(): String = "UPDATE `library_items` SET `id` = ?,`originServerUrl` = ?,`libraryId` = ?,`mediaType` = ?,`title` = ?,`author` = ?,`series` = ?,`narrator` = ?,`description` = ?,`coverPath` = ?,`localCoverPath` = ?,`durationMs` = ?,`updatedAt` = ? WHERE `id` = ? AND `originServerUrl` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: LibraryItemEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.originServerUrl)
        statement.bindText(3, entity.libraryId)
        statement.bindText(4, entity.mediaType)
        statement.bindText(5, entity.title)
        val _tmpAuthor: String? = entity.author
        if (_tmpAuthor == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpAuthor)
        }
        val _tmpSeries: String? = entity.series
        if (_tmpSeries == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpSeries)
        }
        val _tmpNarrator: String? = entity.narrator
        if (_tmpNarrator == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpNarrator)
        }
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpDescription)
        }
        val _tmpCoverPath: String? = entity.coverPath
        if (_tmpCoverPath == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpCoverPath)
        }
        val _tmpLocalCoverPath: String? = entity.localCoverPath
        if (_tmpLocalCoverPath == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, _tmpLocalCoverPath)
        }
        statement.bindLong(12, entity.durationMs)
        statement.bindLong(13, entity.updatedAt)
        statement.bindText(14, entity.id)
        statement.bindText(15, entity.originServerUrl)
      }
    })
  }

  public override suspend fun upsertLibraries(libraries: List<LibraryEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfLibraryEntity.insert(_connection, libraries)
  }

  public override suspend fun upsertChapters(chapters: List<ChapterEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfChapterEntity.insert(_connection, chapters)
  }

  public override suspend fun replaceChapters(
    itemId: String,
    originServerUrl: String,
    chapters: List<ChapterEntity>,
  ): Unit = performInTransactionSuspending(__db) {
    super@LibraryDao_Impl.replaceChapters(itemId, originServerUrl, chapters)
  }

  public override suspend fun upsertItems(items: List<LibraryItemEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfLibraryItemEntity.upsert(_connection, items)
  }

  public override fun observeLibraries(originServerUrl: String): Flow<List<LibraryEntity>> {
    val _sql: String = "SELECT * FROM libraries WHERE originServerUrl = ? ORDER BY name COLLATE NOCASE"
    return createFlow(__db, false, arrayOf("libraries")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, originServerUrl)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOriginServerUrl: Int = getColumnIndexOrThrow(_stmt, "originServerUrl")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfMediaType: Int = getColumnIndexOrThrow(_stmt, "mediaType")
        val _result: MutableList<LibraryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: LibraryEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpMediaType: String
          _tmpMediaType = _stmt.getText(_columnIndexOfMediaType)
          _item = LibraryEntity(_tmpId,_tmpOriginServerUrl,_tmpName,_tmpMediaType)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeItems(libraryId: String, originServerUrl: String): Flow<List<LibraryItemEntity>> {
    val _sql: String = "SELECT * FROM library_items WHERE libraryId = ? AND originServerUrl = ? ORDER BY title COLLATE NOCASE"
    return createFlow(__db, false, arrayOf("library_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, libraryId)
        _argIndex = 2
        _stmt.bindText(_argIndex, originServerUrl)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOriginServerUrl: Int = getColumnIndexOrThrow(_stmt, "originServerUrl")
        val _columnIndexOfLibraryId: Int = getColumnIndexOrThrow(_stmt, "libraryId")
        val _columnIndexOfMediaType: Int = getColumnIndexOrThrow(_stmt, "mediaType")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfAuthor: Int = getColumnIndexOrThrow(_stmt, "author")
        val _columnIndexOfSeries: Int = getColumnIndexOrThrow(_stmt, "series")
        val _columnIndexOfNarrator: Int = getColumnIndexOrThrow(_stmt, "narrator")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCoverPath: Int = getColumnIndexOrThrow(_stmt, "coverPath")
        val _columnIndexOfLocalCoverPath: Int = getColumnIndexOrThrow(_stmt, "localCoverPath")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<LibraryItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: LibraryItemEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpLibraryId: String
          _tmpLibraryId = _stmt.getText(_columnIndexOfLibraryId)
          val _tmpMediaType: String
          _tmpMediaType = _stmt.getText(_columnIndexOfMediaType)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpAuthor: String?
          if (_stmt.isNull(_columnIndexOfAuthor)) {
            _tmpAuthor = null
          } else {
            _tmpAuthor = _stmt.getText(_columnIndexOfAuthor)
          }
          val _tmpSeries: String?
          if (_stmt.isNull(_columnIndexOfSeries)) {
            _tmpSeries = null
          } else {
            _tmpSeries = _stmt.getText(_columnIndexOfSeries)
          }
          val _tmpNarrator: String?
          if (_stmt.isNull(_columnIndexOfNarrator)) {
            _tmpNarrator = null
          } else {
            _tmpNarrator = _stmt.getText(_columnIndexOfNarrator)
          }
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpCoverPath: String?
          if (_stmt.isNull(_columnIndexOfCoverPath)) {
            _tmpCoverPath = null
          } else {
            _tmpCoverPath = _stmt.getText(_columnIndexOfCoverPath)
          }
          val _tmpLocalCoverPath: String?
          if (_stmt.isNull(_columnIndexOfLocalCoverPath)) {
            _tmpLocalCoverPath = null
          } else {
            _tmpLocalCoverPath = _stmt.getText(_columnIndexOfLocalCoverPath)
          }
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item = LibraryItemEntity(_tmpId,_tmpOriginServerUrl,_tmpLibraryId,_tmpMediaType,_tmpTitle,_tmpAuthor,_tmpSeries,_tmpNarrator,_tmpDescription,_tmpCoverPath,_tmpLocalCoverPath,_tmpDurationMs,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllItems(libraryId: String, originServerUrl: String): List<LibraryItemEntity> {
    val _sql: String = "SELECT * FROM library_items WHERE libraryId = ? AND originServerUrl = ? ORDER BY title COLLATE NOCASE"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, libraryId)
        _argIndex = 2
        _stmt.bindText(_argIndex, originServerUrl)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOriginServerUrl: Int = getColumnIndexOrThrow(_stmt, "originServerUrl")
        val _columnIndexOfLibraryId: Int = getColumnIndexOrThrow(_stmt, "libraryId")
        val _columnIndexOfMediaType: Int = getColumnIndexOrThrow(_stmt, "mediaType")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfAuthor: Int = getColumnIndexOrThrow(_stmt, "author")
        val _columnIndexOfSeries: Int = getColumnIndexOrThrow(_stmt, "series")
        val _columnIndexOfNarrator: Int = getColumnIndexOrThrow(_stmt, "narrator")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCoverPath: Int = getColumnIndexOrThrow(_stmt, "coverPath")
        val _columnIndexOfLocalCoverPath: Int = getColumnIndexOrThrow(_stmt, "localCoverPath")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<LibraryItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: LibraryItemEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpLibraryId: String
          _tmpLibraryId = _stmt.getText(_columnIndexOfLibraryId)
          val _tmpMediaType: String
          _tmpMediaType = _stmt.getText(_columnIndexOfMediaType)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpAuthor: String?
          if (_stmt.isNull(_columnIndexOfAuthor)) {
            _tmpAuthor = null
          } else {
            _tmpAuthor = _stmt.getText(_columnIndexOfAuthor)
          }
          val _tmpSeries: String?
          if (_stmt.isNull(_columnIndexOfSeries)) {
            _tmpSeries = null
          } else {
            _tmpSeries = _stmt.getText(_columnIndexOfSeries)
          }
          val _tmpNarrator: String?
          if (_stmt.isNull(_columnIndexOfNarrator)) {
            _tmpNarrator = null
          } else {
            _tmpNarrator = _stmt.getText(_columnIndexOfNarrator)
          }
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpCoverPath: String?
          if (_stmt.isNull(_columnIndexOfCoverPath)) {
            _tmpCoverPath = null
          } else {
            _tmpCoverPath = _stmt.getText(_columnIndexOfCoverPath)
          }
          val _tmpLocalCoverPath: String?
          if (_stmt.isNull(_columnIndexOfLocalCoverPath)) {
            _tmpLocalCoverPath = null
          } else {
            _tmpLocalCoverPath = _stmt.getText(_columnIndexOfLocalCoverPath)
          }
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item = LibraryItemEntity(_tmpId,_tmpOriginServerUrl,_tmpLibraryId,_tmpMediaType,_tmpTitle,_tmpAuthor,_tmpSeries,_tmpNarrator,_tmpDescription,_tmpCoverPath,_tmpLocalCoverPath,_tmpDurationMs,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getItemsPage(
    libraryId: String,
    originServerUrl: String,
    limit: Int,
    offset: Int,
  ): List<LibraryItemEntity> {
    val _sql: String = """
        |SELECT * FROM library_items
        |           WHERE libraryId = ? AND originServerUrl = ?
        |           ORDER BY title COLLATE NOCASE
        |           LIMIT ? OFFSET ?
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, libraryId)
        _argIndex = 2
        _stmt.bindText(_argIndex, originServerUrl)
        _argIndex = 3
        _stmt.bindLong(_argIndex, limit.toLong())
        _argIndex = 4
        _stmt.bindLong(_argIndex, offset.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOriginServerUrl: Int = getColumnIndexOrThrow(_stmt, "originServerUrl")
        val _columnIndexOfLibraryId: Int = getColumnIndexOrThrow(_stmt, "libraryId")
        val _columnIndexOfMediaType: Int = getColumnIndexOrThrow(_stmt, "mediaType")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfAuthor: Int = getColumnIndexOrThrow(_stmt, "author")
        val _columnIndexOfSeries: Int = getColumnIndexOrThrow(_stmt, "series")
        val _columnIndexOfNarrator: Int = getColumnIndexOrThrow(_stmt, "narrator")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCoverPath: Int = getColumnIndexOrThrow(_stmt, "coverPath")
        val _columnIndexOfLocalCoverPath: Int = getColumnIndexOrThrow(_stmt, "localCoverPath")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<LibraryItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: LibraryItemEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpLibraryId: String
          _tmpLibraryId = _stmt.getText(_columnIndexOfLibraryId)
          val _tmpMediaType: String
          _tmpMediaType = _stmt.getText(_columnIndexOfMediaType)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpAuthor: String?
          if (_stmt.isNull(_columnIndexOfAuthor)) {
            _tmpAuthor = null
          } else {
            _tmpAuthor = _stmt.getText(_columnIndexOfAuthor)
          }
          val _tmpSeries: String?
          if (_stmt.isNull(_columnIndexOfSeries)) {
            _tmpSeries = null
          } else {
            _tmpSeries = _stmt.getText(_columnIndexOfSeries)
          }
          val _tmpNarrator: String?
          if (_stmt.isNull(_columnIndexOfNarrator)) {
            _tmpNarrator = null
          } else {
            _tmpNarrator = _stmt.getText(_columnIndexOfNarrator)
          }
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpCoverPath: String?
          if (_stmt.isNull(_columnIndexOfCoverPath)) {
            _tmpCoverPath = null
          } else {
            _tmpCoverPath = _stmt.getText(_columnIndexOfCoverPath)
          }
          val _tmpLocalCoverPath: String?
          if (_stmt.isNull(_columnIndexOfLocalCoverPath)) {
            _tmpLocalCoverPath = null
          } else {
            _tmpLocalCoverPath = _stmt.getText(_columnIndexOfLocalCoverPath)
          }
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item = LibraryItemEntity(_tmpId,_tmpOriginServerUrl,_tmpLibraryId,_tmpMediaType,_tmpTitle,_tmpAuthor,_tmpSeries,_tmpNarrator,_tmpDescription,_tmpCoverPath,_tmpLocalCoverPath,_tmpDurationMs,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeItem(itemId: String, originServerUrl: String): Flow<LibraryItemEntity?> {
    val _sql: String = "SELECT * FROM library_items WHERE id = ? AND originServerUrl = ?"
    return createFlow(__db, false, arrayOf("library_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, itemId)
        _argIndex = 2
        _stmt.bindText(_argIndex, originServerUrl)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOriginServerUrl: Int = getColumnIndexOrThrow(_stmt, "originServerUrl")
        val _columnIndexOfLibraryId: Int = getColumnIndexOrThrow(_stmt, "libraryId")
        val _columnIndexOfMediaType: Int = getColumnIndexOrThrow(_stmt, "mediaType")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfAuthor: Int = getColumnIndexOrThrow(_stmt, "author")
        val _columnIndexOfSeries: Int = getColumnIndexOrThrow(_stmt, "series")
        val _columnIndexOfNarrator: Int = getColumnIndexOrThrow(_stmt, "narrator")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCoverPath: Int = getColumnIndexOrThrow(_stmt, "coverPath")
        val _columnIndexOfLocalCoverPath: Int = getColumnIndexOrThrow(_stmt, "localCoverPath")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: LibraryItemEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpLibraryId: String
          _tmpLibraryId = _stmt.getText(_columnIndexOfLibraryId)
          val _tmpMediaType: String
          _tmpMediaType = _stmt.getText(_columnIndexOfMediaType)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpAuthor: String?
          if (_stmt.isNull(_columnIndexOfAuthor)) {
            _tmpAuthor = null
          } else {
            _tmpAuthor = _stmt.getText(_columnIndexOfAuthor)
          }
          val _tmpSeries: String?
          if (_stmt.isNull(_columnIndexOfSeries)) {
            _tmpSeries = null
          } else {
            _tmpSeries = _stmt.getText(_columnIndexOfSeries)
          }
          val _tmpNarrator: String?
          if (_stmt.isNull(_columnIndexOfNarrator)) {
            _tmpNarrator = null
          } else {
            _tmpNarrator = _stmt.getText(_columnIndexOfNarrator)
          }
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpCoverPath: String?
          if (_stmt.isNull(_columnIndexOfCoverPath)) {
            _tmpCoverPath = null
          } else {
            _tmpCoverPath = _stmt.getText(_columnIndexOfCoverPath)
          }
          val _tmpLocalCoverPath: String?
          if (_stmt.isNull(_columnIndexOfLocalCoverPath)) {
            _tmpLocalCoverPath = null
          } else {
            _tmpLocalCoverPath = _stmt.getText(_columnIndexOfLocalCoverPath)
          }
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _result = LibraryItemEntity(_tmpId,_tmpOriginServerUrl,_tmpLibraryId,_tmpMediaType,_tmpTitle,_tmpAuthor,_tmpSeries,_tmpNarrator,_tmpDescription,_tmpCoverPath,_tmpLocalCoverPath,_tmpDurationMs,_tmpUpdatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getItem(itemId: String, originServerUrl: String): LibraryItemEntity? {
    val _sql: String = "SELECT * FROM library_items WHERE id = ? AND originServerUrl = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, itemId)
        _argIndex = 2
        _stmt.bindText(_argIndex, originServerUrl)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOriginServerUrl: Int = getColumnIndexOrThrow(_stmt, "originServerUrl")
        val _columnIndexOfLibraryId: Int = getColumnIndexOrThrow(_stmt, "libraryId")
        val _columnIndexOfMediaType: Int = getColumnIndexOrThrow(_stmt, "mediaType")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfAuthor: Int = getColumnIndexOrThrow(_stmt, "author")
        val _columnIndexOfSeries: Int = getColumnIndexOrThrow(_stmt, "series")
        val _columnIndexOfNarrator: Int = getColumnIndexOrThrow(_stmt, "narrator")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCoverPath: Int = getColumnIndexOrThrow(_stmt, "coverPath")
        val _columnIndexOfLocalCoverPath: Int = getColumnIndexOrThrow(_stmt, "localCoverPath")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: LibraryItemEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpLibraryId: String
          _tmpLibraryId = _stmt.getText(_columnIndexOfLibraryId)
          val _tmpMediaType: String
          _tmpMediaType = _stmt.getText(_columnIndexOfMediaType)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpAuthor: String?
          if (_stmt.isNull(_columnIndexOfAuthor)) {
            _tmpAuthor = null
          } else {
            _tmpAuthor = _stmt.getText(_columnIndexOfAuthor)
          }
          val _tmpSeries: String?
          if (_stmt.isNull(_columnIndexOfSeries)) {
            _tmpSeries = null
          } else {
            _tmpSeries = _stmt.getText(_columnIndexOfSeries)
          }
          val _tmpNarrator: String?
          if (_stmt.isNull(_columnIndexOfNarrator)) {
            _tmpNarrator = null
          } else {
            _tmpNarrator = _stmt.getText(_columnIndexOfNarrator)
          }
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpCoverPath: String?
          if (_stmt.isNull(_columnIndexOfCoverPath)) {
            _tmpCoverPath = null
          } else {
            _tmpCoverPath = _stmt.getText(_columnIndexOfCoverPath)
          }
          val _tmpLocalCoverPath: String?
          if (_stmt.isNull(_columnIndexOfLocalCoverPath)) {
            _tmpLocalCoverPath = null
          } else {
            _tmpLocalCoverPath = _stmt.getText(_columnIndexOfLocalCoverPath)
          }
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _result = LibraryItemEntity(_tmpId,_tmpOriginServerUrl,_tmpLibraryId,_tmpMediaType,_tmpTitle,_tmpAuthor,_tmpSeries,_tmpNarrator,_tmpDescription,_tmpCoverPath,_tmpLocalCoverPath,_tmpDurationMs,_tmpUpdatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getItems(itemIds: List<String>, originServerUrl: String): List<LibraryItemEntity> {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT * FROM library_items WHERE originServerUrl = ")
    _stringBuilder.append("?")
    _stringBuilder.append(" AND id IN (")
    val _inputSize: Int = itemIds.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, originServerUrl)
        _argIndex = 2
        for (_item: String in itemIds) {
          _stmt.bindText(_argIndex, _item)
          _argIndex++
        }
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOriginServerUrl: Int = getColumnIndexOrThrow(_stmt, "originServerUrl")
        val _columnIndexOfLibraryId: Int = getColumnIndexOrThrow(_stmt, "libraryId")
        val _columnIndexOfMediaType: Int = getColumnIndexOrThrow(_stmt, "mediaType")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfAuthor: Int = getColumnIndexOrThrow(_stmt, "author")
        val _columnIndexOfSeries: Int = getColumnIndexOrThrow(_stmt, "series")
        val _columnIndexOfNarrator: Int = getColumnIndexOrThrow(_stmt, "narrator")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCoverPath: Int = getColumnIndexOrThrow(_stmt, "coverPath")
        val _columnIndexOfLocalCoverPath: Int = getColumnIndexOrThrow(_stmt, "localCoverPath")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<LibraryItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item_1: LibraryItemEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpLibraryId: String
          _tmpLibraryId = _stmt.getText(_columnIndexOfLibraryId)
          val _tmpMediaType: String
          _tmpMediaType = _stmt.getText(_columnIndexOfMediaType)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpAuthor: String?
          if (_stmt.isNull(_columnIndexOfAuthor)) {
            _tmpAuthor = null
          } else {
            _tmpAuthor = _stmt.getText(_columnIndexOfAuthor)
          }
          val _tmpSeries: String?
          if (_stmt.isNull(_columnIndexOfSeries)) {
            _tmpSeries = null
          } else {
            _tmpSeries = _stmt.getText(_columnIndexOfSeries)
          }
          val _tmpNarrator: String?
          if (_stmt.isNull(_columnIndexOfNarrator)) {
            _tmpNarrator = null
          } else {
            _tmpNarrator = _stmt.getText(_columnIndexOfNarrator)
          }
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpCoverPath: String?
          if (_stmt.isNull(_columnIndexOfCoverPath)) {
            _tmpCoverPath = null
          } else {
            _tmpCoverPath = _stmt.getText(_columnIndexOfCoverPath)
          }
          val _tmpLocalCoverPath: String?
          if (_stmt.isNull(_columnIndexOfLocalCoverPath)) {
            _tmpLocalCoverPath = null
          } else {
            _tmpLocalCoverPath = _stmt.getText(_columnIndexOfLocalCoverPath)
          }
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item_1 = LibraryItemEntity(_tmpId,_tmpOriginServerUrl,_tmpLibraryId,_tmpMediaType,_tmpTitle,_tmpAuthor,_tmpSeries,_tmpNarrator,_tmpDescription,_tmpCoverPath,_tmpLocalCoverPath,_tmpDurationMs,_tmpUpdatedAt)
          _result.add(_item_1)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun search(query: String, originServerUrl: String): Flow<List<LibraryItemEntity>> {
    val _sql: String = """
        |SELECT * FROM library_items
        |           WHERE originServerUrl = ? AND (
        |              title LIKE '%' || ? || '%' COLLATE NOCASE
        |              OR author LIKE '%' || ? || '%' COLLATE NOCASE
        |              OR series LIKE '%' || ? || '%' COLLATE NOCASE
        |              OR narrator LIKE '%' || ? || '%' COLLATE NOCASE)
        |           ORDER BY title COLLATE NOCASE
        """.trimMargin()
    return createFlow(__db, false, arrayOf("library_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, originServerUrl)
        _argIndex = 2
        _stmt.bindText(_argIndex, query)
        _argIndex = 3
        _stmt.bindText(_argIndex, query)
        _argIndex = 4
        _stmt.bindText(_argIndex, query)
        _argIndex = 5
        _stmt.bindText(_argIndex, query)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOriginServerUrl: Int = getColumnIndexOrThrow(_stmt, "originServerUrl")
        val _columnIndexOfLibraryId: Int = getColumnIndexOrThrow(_stmt, "libraryId")
        val _columnIndexOfMediaType: Int = getColumnIndexOrThrow(_stmt, "mediaType")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfAuthor: Int = getColumnIndexOrThrow(_stmt, "author")
        val _columnIndexOfSeries: Int = getColumnIndexOrThrow(_stmt, "series")
        val _columnIndexOfNarrator: Int = getColumnIndexOrThrow(_stmt, "narrator")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCoverPath: Int = getColumnIndexOrThrow(_stmt, "coverPath")
        val _columnIndexOfLocalCoverPath: Int = getColumnIndexOrThrow(_stmt, "localCoverPath")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<LibraryItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: LibraryItemEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpLibraryId: String
          _tmpLibraryId = _stmt.getText(_columnIndexOfLibraryId)
          val _tmpMediaType: String
          _tmpMediaType = _stmt.getText(_columnIndexOfMediaType)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpAuthor: String?
          if (_stmt.isNull(_columnIndexOfAuthor)) {
            _tmpAuthor = null
          } else {
            _tmpAuthor = _stmt.getText(_columnIndexOfAuthor)
          }
          val _tmpSeries: String?
          if (_stmt.isNull(_columnIndexOfSeries)) {
            _tmpSeries = null
          } else {
            _tmpSeries = _stmt.getText(_columnIndexOfSeries)
          }
          val _tmpNarrator: String?
          if (_stmt.isNull(_columnIndexOfNarrator)) {
            _tmpNarrator = null
          } else {
            _tmpNarrator = _stmt.getText(_columnIndexOfNarrator)
          }
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpCoverPath: String?
          if (_stmt.isNull(_columnIndexOfCoverPath)) {
            _tmpCoverPath = null
          } else {
            _tmpCoverPath = _stmt.getText(_columnIndexOfCoverPath)
          }
          val _tmpLocalCoverPath: String?
          if (_stmt.isNull(_columnIndexOfLocalCoverPath)) {
            _tmpLocalCoverPath = null
          } else {
            _tmpLocalCoverPath = _stmt.getText(_columnIndexOfLocalCoverPath)
          }
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item = LibraryItemEntity(_tmpId,_tmpOriginServerUrl,_tmpLibraryId,_tmpMediaType,_tmpTitle,_tmpAuthor,_tmpSeries,_tmpNarrator,_tmpDescription,_tmpCoverPath,_tmpLocalCoverPath,_tmpDurationMs,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun searchLibrary(
    libraryId: String,
    originServerUrl: String,
    query: String,
    limit: Int,
  ): List<LibraryItemEntity> {
    val _sql: String = """
        |SELECT * FROM library_items
        |           WHERE libraryId = ? AND originServerUrl = ? AND (
        |              title LIKE '%' || ? || '%' COLLATE NOCASE
        |              OR author LIKE '%' || ? || '%' COLLATE NOCASE
        |              OR series LIKE '%' || ? || '%' COLLATE NOCASE
        |              OR narrator LIKE '%' || ? || '%' COLLATE NOCASE)
        |           ORDER BY title COLLATE NOCASE
        |           LIMIT ?
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, libraryId)
        _argIndex = 2
        _stmt.bindText(_argIndex, originServerUrl)
        _argIndex = 3
        _stmt.bindText(_argIndex, query)
        _argIndex = 4
        _stmt.bindText(_argIndex, query)
        _argIndex = 5
        _stmt.bindText(_argIndex, query)
        _argIndex = 6
        _stmt.bindText(_argIndex, query)
        _argIndex = 7
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOriginServerUrl: Int = getColumnIndexOrThrow(_stmt, "originServerUrl")
        val _columnIndexOfLibraryId: Int = getColumnIndexOrThrow(_stmt, "libraryId")
        val _columnIndexOfMediaType: Int = getColumnIndexOrThrow(_stmt, "mediaType")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfAuthor: Int = getColumnIndexOrThrow(_stmt, "author")
        val _columnIndexOfSeries: Int = getColumnIndexOrThrow(_stmt, "series")
        val _columnIndexOfNarrator: Int = getColumnIndexOrThrow(_stmt, "narrator")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfCoverPath: Int = getColumnIndexOrThrow(_stmt, "coverPath")
        val _columnIndexOfLocalCoverPath: Int = getColumnIndexOrThrow(_stmt, "localCoverPath")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<LibraryItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: LibraryItemEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpLibraryId: String
          _tmpLibraryId = _stmt.getText(_columnIndexOfLibraryId)
          val _tmpMediaType: String
          _tmpMediaType = _stmt.getText(_columnIndexOfMediaType)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpAuthor: String?
          if (_stmt.isNull(_columnIndexOfAuthor)) {
            _tmpAuthor = null
          } else {
            _tmpAuthor = _stmt.getText(_columnIndexOfAuthor)
          }
          val _tmpSeries: String?
          if (_stmt.isNull(_columnIndexOfSeries)) {
            _tmpSeries = null
          } else {
            _tmpSeries = _stmt.getText(_columnIndexOfSeries)
          }
          val _tmpNarrator: String?
          if (_stmt.isNull(_columnIndexOfNarrator)) {
            _tmpNarrator = null
          } else {
            _tmpNarrator = _stmt.getText(_columnIndexOfNarrator)
          }
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpCoverPath: String?
          if (_stmt.isNull(_columnIndexOfCoverPath)) {
            _tmpCoverPath = null
          } else {
            _tmpCoverPath = _stmt.getText(_columnIndexOfCoverPath)
          }
          val _tmpLocalCoverPath: String?
          if (_stmt.isNull(_columnIndexOfLocalCoverPath)) {
            _tmpLocalCoverPath = null
          } else {
            _tmpLocalCoverPath = _stmt.getText(_columnIndexOfLocalCoverPath)
          }
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item = LibraryItemEntity(_tmpId,_tmpOriginServerUrl,_tmpLibraryId,_tmpMediaType,_tmpTitle,_tmpAuthor,_tmpSeries,_tmpNarrator,_tmpDescription,_tmpCoverPath,_tmpLocalCoverPath,_tmpDurationMs,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeChapters(itemId: String, originServerUrl: String): Flow<List<ChapterEntity>> {
    val _sql: String = "SELECT * FROM chapters WHERE itemId = ? AND originServerUrl = ? ORDER BY startMs"
    return createFlow(__db, false, arrayOf("chapters")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, itemId)
        _argIndex = 2
        _stmt.bindText(_argIndex, originServerUrl)
        val _columnIndexOfItemId: Int = getColumnIndexOrThrow(_stmt, "itemId")
        val _columnIndexOfOriginServerUrl: Int = getColumnIndexOrThrow(_stmt, "originServerUrl")
        val _columnIndexOfChapterId: Int = getColumnIndexOrThrow(_stmt, "chapterId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfStartMs: Int = getColumnIndexOrThrow(_stmt, "startMs")
        val _columnIndexOfEndMs: Int = getColumnIndexOrThrow(_stmt, "endMs")
        val _result: MutableList<ChapterEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ChapterEntity
          val _tmpItemId: String
          _tmpItemId = _stmt.getText(_columnIndexOfItemId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpChapterId: Int
          _tmpChapterId = _stmt.getLong(_columnIndexOfChapterId).toInt()
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpStartMs: Long
          _tmpStartMs = _stmt.getLong(_columnIndexOfStartMs)
          val _tmpEndMs: Long
          _tmpEndMs = _stmt.getLong(_columnIndexOfEndMs)
          _item = ChapterEntity(_tmpItemId,_tmpOriginServerUrl,_tmpChapterId,_tmpTitle,_tmpStartMs,_tmpEndMs)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getChapters(itemId: String, originServerUrl: String): List<ChapterEntity> {
    val _sql: String = "SELECT * FROM chapters WHERE itemId = ? AND originServerUrl = ? ORDER BY startMs"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, itemId)
        _argIndex = 2
        _stmt.bindText(_argIndex, originServerUrl)
        val _columnIndexOfItemId: Int = getColumnIndexOrThrow(_stmt, "itemId")
        val _columnIndexOfOriginServerUrl: Int = getColumnIndexOrThrow(_stmt, "originServerUrl")
        val _columnIndexOfChapterId: Int = getColumnIndexOrThrow(_stmt, "chapterId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfStartMs: Int = getColumnIndexOrThrow(_stmt, "startMs")
        val _columnIndexOfEndMs: Int = getColumnIndexOrThrow(_stmt, "endMs")
        val _result: MutableList<ChapterEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ChapterEntity
          val _tmpItemId: String
          _tmpItemId = _stmt.getText(_columnIndexOfItemId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpChapterId: Int
          _tmpChapterId = _stmt.getLong(_columnIndexOfChapterId).toInt()
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpStartMs: Long
          _tmpStartMs = _stmt.getLong(_columnIndexOfStartMs)
          val _tmpEndMs: Long
          _tmpEndMs = _stmt.getLong(_columnIndexOfEndMs)
          _item = ChapterEntity(_tmpItemId,_tmpOriginServerUrl,_tmpChapterId,_tmpTitle,_tmpStartMs,_tmpEndMs)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setLocalCoverPath(
    itemId: String,
    originServerUrl: String,
    path: String?,
  ) {
    val _sql: String = "UPDATE library_items SET localCoverPath = ? WHERE id = ? AND originServerUrl = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        if (path == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, path)
        }
        _argIndex = 2
        _stmt.bindText(_argIndex, itemId)
        _argIndex = 3
        _stmt.bindText(_argIndex, originServerUrl)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteChapters(itemId: String, originServerUrl: String) {
    val _sql: String = "DELETE FROM chapters WHERE itemId = ? AND originServerUrl = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, itemId)
        _argIndex = 2
        _stmt.bindText(_argIndex, originServerUrl)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}

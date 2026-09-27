package com.nortlinos.wearos.`data`.local

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class DownloadDao_Impl(
  __db: RoomDatabase,
) : DownloadDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfDownloadedItemEntity: EntityInsertAdapter<DownloadedItemEntity>

  private val __insertAdapterOfDownloadTrackEntity: EntityInsertAdapter<DownloadTrackEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfDownloadedItemEntity = object : EntityInsertAdapter<DownloadedItemEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `downloads` (`itemId`,`originServerUrl`,`status`,`localDirectory`,`fileSizeBytes`,`downloadedBytes`,`error`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DownloadedItemEntity) {
        statement.bindText(1, entity.itemId)
        statement.bindText(2, entity.originServerUrl)
        statement.bindText(3, __DownloadStatus_enumToString(entity.status))
        statement.bindText(4, entity.localDirectory)
        statement.bindLong(5, entity.fileSizeBytes)
        statement.bindLong(6, entity.downloadedBytes)
        val _tmpError: String? = entity.error
        if (_tmpError == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpError)
        }
      }
    }
    this.__insertAdapterOfDownloadTrackEntity = object : EntityInsertAdapter<DownloadTrackEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `download_tracks` (`itemId`,`originServerUrl`,`trackIndex`,`localPath`,`contentUrl`,`mimeType`,`startOffsetMs`,`durationMs`,`sizeBytes`) VALUES (?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DownloadTrackEntity) {
        statement.bindText(1, entity.itemId)
        statement.bindText(2, entity.originServerUrl)
        statement.bindLong(3, entity.trackIndex.toLong())
        statement.bindText(4, entity.localPath)
        statement.bindText(5, entity.contentUrl)
        statement.bindText(6, entity.mimeType)
        statement.bindLong(7, entity.startOffsetMs)
        statement.bindLong(8, entity.durationMs)
        statement.bindLong(9, entity.sizeBytes)
      }
    }
  }

  public override suspend fun upsertDownload(download: DownloadedItemEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfDownloadedItemEntity.insert(_connection, download)
  }

  public override suspend fun upsertTracks(tracks: List<DownloadTrackEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfDownloadTrackEntity.insert(_connection, tracks)
  }

  public override fun observeAll(): Flow<List<DownloadedItemEntity>> {
    val _sql: String = "SELECT * FROM downloads"
    return createFlow(__db, false, arrayOf("downloads")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfItemId: Int = getColumnIndexOrThrow(_stmt, "itemId")
        val _columnIndexOfOriginServerUrl: Int = getColumnIndexOrThrow(_stmt, "originServerUrl")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfLocalDirectory: Int = getColumnIndexOrThrow(_stmt, "localDirectory")
        val _columnIndexOfFileSizeBytes: Int = getColumnIndexOrThrow(_stmt, "fileSizeBytes")
        val _columnIndexOfDownloadedBytes: Int = getColumnIndexOrThrow(_stmt, "downloadedBytes")
        val _columnIndexOfError: Int = getColumnIndexOrThrow(_stmt, "error")
        val _result: MutableList<DownloadedItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DownloadedItemEntity
          val _tmpItemId: String
          _tmpItemId = _stmt.getText(_columnIndexOfItemId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpStatus: DownloadStatus
          _tmpStatus = __DownloadStatus_stringToEnum(_stmt.getText(_columnIndexOfStatus))
          val _tmpLocalDirectory: String
          _tmpLocalDirectory = _stmt.getText(_columnIndexOfLocalDirectory)
          val _tmpFileSizeBytes: Long
          _tmpFileSizeBytes = _stmt.getLong(_columnIndexOfFileSizeBytes)
          val _tmpDownloadedBytes: Long
          _tmpDownloadedBytes = _stmt.getLong(_columnIndexOfDownloadedBytes)
          val _tmpError: String?
          if (_stmt.isNull(_columnIndexOfError)) {
            _tmpError = null
          } else {
            _tmpError = _stmt.getText(_columnIndexOfError)
          }
          _item = DownloadedItemEntity(_tmpItemId,_tmpOriginServerUrl,_tmpStatus,_tmpLocalDirectory,_tmpFileSizeBytes,_tmpDownloadedBytes,_tmpError)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeDownloadedItems(): Flow<List<LibraryItemEntity>> {
    val _sql: String = """
        |SELECT * FROM library_items WHERE EXISTS (
        |             SELECT 1 FROM downloads d
        |             WHERE d.itemId = library_items.id
        |               AND d.originServerUrl = library_items.originServerUrl
        |               AND d.status = 'DOWNLOADED'
        |           )
        |           ORDER BY title COLLATE NOCASE
        """.trimMargin()
    return createFlow(__db, false, arrayOf("library_items", "downloads")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
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

  public override suspend fun getTracks(itemId: String, originServerUrl: String): List<DownloadTrackEntity> {
    val _sql: String = "SELECT * FROM download_tracks WHERE itemId = ? AND originServerUrl = ? ORDER BY trackIndex"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, itemId)
        _argIndex = 2
        _stmt.bindText(_argIndex, originServerUrl)
        val _columnIndexOfItemId: Int = getColumnIndexOrThrow(_stmt, "itemId")
        val _columnIndexOfOriginServerUrl: Int = getColumnIndexOrThrow(_stmt, "originServerUrl")
        val _columnIndexOfTrackIndex: Int = getColumnIndexOrThrow(_stmt, "trackIndex")
        val _columnIndexOfLocalPath: Int = getColumnIndexOrThrow(_stmt, "localPath")
        val _columnIndexOfContentUrl: Int = getColumnIndexOrThrow(_stmt, "contentUrl")
        val _columnIndexOfMimeType: Int = getColumnIndexOrThrow(_stmt, "mimeType")
        val _columnIndexOfStartOffsetMs: Int = getColumnIndexOrThrow(_stmt, "startOffsetMs")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfSizeBytes: Int = getColumnIndexOrThrow(_stmt, "sizeBytes")
        val _result: MutableList<DownloadTrackEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DownloadTrackEntity
          val _tmpItemId: String
          _tmpItemId = _stmt.getText(_columnIndexOfItemId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpTrackIndex: Int
          _tmpTrackIndex = _stmt.getLong(_columnIndexOfTrackIndex).toInt()
          val _tmpLocalPath: String
          _tmpLocalPath = _stmt.getText(_columnIndexOfLocalPath)
          val _tmpContentUrl: String
          _tmpContentUrl = _stmt.getText(_columnIndexOfContentUrl)
          val _tmpMimeType: String
          _tmpMimeType = _stmt.getText(_columnIndexOfMimeType)
          val _tmpStartOffsetMs: Long
          _tmpStartOffsetMs = _stmt.getLong(_columnIndexOfStartOffsetMs)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpSizeBytes: Long
          _tmpSizeBytes = _stmt.getLong(_columnIndexOfSizeBytes)
          _item = DownloadTrackEntity(_tmpItemId,_tmpOriginServerUrl,_tmpTrackIndex,_tmpLocalPath,_tmpContentUrl,_tmpMimeType,_tmpStartOffsetMs,_tmpDurationMs,_tmpSizeBytes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeDownload(itemId: String, originServerUrl: String): Flow<DownloadedItemEntity?> {
    val _sql: String = "SELECT * FROM downloads WHERE itemId = ? AND originServerUrl = ?"
    return createFlow(__db, false, arrayOf("downloads")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, itemId)
        _argIndex = 2
        _stmt.bindText(_argIndex, originServerUrl)
        val _columnIndexOfItemId: Int = getColumnIndexOrThrow(_stmt, "itemId")
        val _columnIndexOfOriginServerUrl: Int = getColumnIndexOrThrow(_stmt, "originServerUrl")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfLocalDirectory: Int = getColumnIndexOrThrow(_stmt, "localDirectory")
        val _columnIndexOfFileSizeBytes: Int = getColumnIndexOrThrow(_stmt, "fileSizeBytes")
        val _columnIndexOfDownloadedBytes: Int = getColumnIndexOrThrow(_stmt, "downloadedBytes")
        val _columnIndexOfError: Int = getColumnIndexOrThrow(_stmt, "error")
        val _result: DownloadedItemEntity?
        if (_stmt.step()) {
          val _tmpItemId: String
          _tmpItemId = _stmt.getText(_columnIndexOfItemId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpStatus: DownloadStatus
          _tmpStatus = __DownloadStatus_stringToEnum(_stmt.getText(_columnIndexOfStatus))
          val _tmpLocalDirectory: String
          _tmpLocalDirectory = _stmt.getText(_columnIndexOfLocalDirectory)
          val _tmpFileSizeBytes: Long
          _tmpFileSizeBytes = _stmt.getLong(_columnIndexOfFileSizeBytes)
          val _tmpDownloadedBytes: Long
          _tmpDownloadedBytes = _stmt.getLong(_columnIndexOfDownloadedBytes)
          val _tmpError: String?
          if (_stmt.isNull(_columnIndexOfError)) {
            _tmpError = null
          } else {
            _tmpError = _stmt.getText(_columnIndexOfError)
          }
          _result = DownloadedItemEntity(_tmpItemId,_tmpOriginServerUrl,_tmpStatus,_tmpLocalDirectory,_tmpFileSizeBytes,_tmpDownloadedBytes,_tmpError)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getDownload(itemId: String, originServerUrl: String): DownloadedItemEntity? {
    val _sql: String = "SELECT * FROM downloads WHERE itemId = ? AND originServerUrl = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, itemId)
        _argIndex = 2
        _stmt.bindText(_argIndex, originServerUrl)
        val _columnIndexOfItemId: Int = getColumnIndexOrThrow(_stmt, "itemId")
        val _columnIndexOfOriginServerUrl: Int = getColumnIndexOrThrow(_stmt, "originServerUrl")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfLocalDirectory: Int = getColumnIndexOrThrow(_stmt, "localDirectory")
        val _columnIndexOfFileSizeBytes: Int = getColumnIndexOrThrow(_stmt, "fileSizeBytes")
        val _columnIndexOfDownloadedBytes: Int = getColumnIndexOrThrow(_stmt, "downloadedBytes")
        val _columnIndexOfError: Int = getColumnIndexOrThrow(_stmt, "error")
        val _result: DownloadedItemEntity?
        if (_stmt.step()) {
          val _tmpItemId: String
          _tmpItemId = _stmt.getText(_columnIndexOfItemId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpStatus: DownloadStatus
          _tmpStatus = __DownloadStatus_stringToEnum(_stmt.getText(_columnIndexOfStatus))
          val _tmpLocalDirectory: String
          _tmpLocalDirectory = _stmt.getText(_columnIndexOfLocalDirectory)
          val _tmpFileSizeBytes: Long
          _tmpFileSizeBytes = _stmt.getLong(_columnIndexOfFileSizeBytes)
          val _tmpDownloadedBytes: Long
          _tmpDownloadedBytes = _stmt.getLong(_columnIndexOfDownloadedBytes)
          val _tmpError: String?
          if (_stmt.isNull(_columnIndexOfError)) {
            _tmpError = null
          } else {
            _tmpError = _stmt.getText(_columnIndexOfError)
          }
          _result = DownloadedItemEntity(_tmpItemId,_tmpOriginServerUrl,_tmpStatus,_tmpLocalDirectory,_tmpFileSizeBytes,_tmpDownloadedBytes,_tmpError)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteTracks(itemId: String, originServerUrl: String) {
    val _sql: String = "DELETE FROM download_tracks WHERE itemId = ? AND originServerUrl = ?"
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

  public override suspend fun deleteDownload(itemId: String, originServerUrl: String) {
    val _sql: String = "DELETE FROM downloads WHERE itemId = ? AND originServerUrl = ?"
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

  private fun __DownloadStatus_enumToString(_value: DownloadStatus): String = when (_value) {
    DownloadStatus.QUEUED -> "QUEUED"
    DownloadStatus.DOWNLOADING -> "DOWNLOADING"
    DownloadStatus.PAUSED -> "PAUSED"
    DownloadStatus.DOWNLOADED -> "DOWNLOADED"
    DownloadStatus.FAILED -> "FAILED"
  }

  private fun __DownloadStatus_stringToEnum(_value: String): DownloadStatus = when (_value) {
    "QUEUED" -> DownloadStatus.QUEUED
    "DOWNLOADING" -> DownloadStatus.DOWNLOADING
    "PAUSED" -> DownloadStatus.PAUSED
    "DOWNLOADED" -> DownloadStatus.DOWNLOADED
    "FAILED" -> DownloadStatus.FAILED
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: " + _value)
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}

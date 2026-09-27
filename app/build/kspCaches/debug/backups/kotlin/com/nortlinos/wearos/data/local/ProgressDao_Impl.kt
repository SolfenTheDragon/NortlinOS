package com.nortlinos.wearos.`data`.local

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.getTotalChangedRows
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
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
public class ProgressDao_Impl(
  __db: RoomDatabase,
) : ProgressDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfProgressEntity: EntityInsertAdapter<ProgressEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfProgressEntity = object : EntityInsertAdapter<ProgressEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `progress` (`itemId`,`originServerUrl`,`positionMs`,`durationMs`,`updatedAt`,`dirty`,`conflictPositionMs`,`conflictUpdatedAt`) VALUES (?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ProgressEntity) {
        statement.bindText(1, entity.itemId)
        statement.bindText(2, entity.originServerUrl)
        statement.bindLong(3, entity.positionMs)
        statement.bindLong(4, entity.durationMs)
        statement.bindLong(5, entity.updatedAt)
        val _tmp: Int = if (entity.dirty) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        val _tmpConflictPositionMs: Long? = entity.conflictPositionMs
        if (_tmpConflictPositionMs == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpConflictPositionMs)
        }
        val _tmpConflictUpdatedAt: Long? = entity.conflictUpdatedAt
        if (_tmpConflictUpdatedAt == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpConflictUpdatedAt)
        }
      }
    }
  }

  public override suspend fun upsert(progress: ProgressEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfProgressEntity.insert(_connection, progress)
  }

  public override suspend fun upsertAll(progress: List<ProgressEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfProgressEntity.insert(_connection, progress)
  }

  public override fun observe(itemId: String, originServerUrl: String): Flow<ProgressEntity?> {
    val _sql: String = "SELECT * FROM progress WHERE itemId = ? AND originServerUrl = ?"
    return createFlow(__db, false, arrayOf("progress")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, itemId)
        _argIndex = 2
        _stmt.bindText(_argIndex, originServerUrl)
        val _columnIndexOfItemId: Int = getColumnIndexOrThrow(_stmt, "itemId")
        val _columnIndexOfOriginServerUrl: Int = getColumnIndexOrThrow(_stmt, "originServerUrl")
        val _columnIndexOfPositionMs: Int = getColumnIndexOrThrow(_stmt, "positionMs")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDirty: Int = getColumnIndexOrThrow(_stmt, "dirty")
        val _columnIndexOfConflictPositionMs: Int = getColumnIndexOrThrow(_stmt, "conflictPositionMs")
        val _columnIndexOfConflictUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "conflictUpdatedAt")
        val _result: ProgressEntity?
        if (_stmt.step()) {
          val _tmpItemId: String
          _tmpItemId = _stmt.getText(_columnIndexOfItemId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpPositionMs: Long
          _tmpPositionMs = _stmt.getLong(_columnIndexOfPositionMs)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDirty: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfDirty).toInt()
          _tmpDirty = _tmp != 0
          val _tmpConflictPositionMs: Long?
          if (_stmt.isNull(_columnIndexOfConflictPositionMs)) {
            _tmpConflictPositionMs = null
          } else {
            _tmpConflictPositionMs = _stmt.getLong(_columnIndexOfConflictPositionMs)
          }
          val _tmpConflictUpdatedAt: Long?
          if (_stmt.isNull(_columnIndexOfConflictUpdatedAt)) {
            _tmpConflictUpdatedAt = null
          } else {
            _tmpConflictUpdatedAt = _stmt.getLong(_columnIndexOfConflictUpdatedAt)
          }
          _result = ProgressEntity(_tmpItemId,_tmpOriginServerUrl,_tmpPositionMs,_tmpDurationMs,_tmpUpdatedAt,_tmpDirty,_tmpConflictPositionMs,_tmpConflictUpdatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun `get`(itemId: String, originServerUrl: String): ProgressEntity? {
    val _sql: String = "SELECT * FROM progress WHERE itemId = ? AND originServerUrl = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, itemId)
        _argIndex = 2
        _stmt.bindText(_argIndex, originServerUrl)
        val _columnIndexOfItemId: Int = getColumnIndexOrThrow(_stmt, "itemId")
        val _columnIndexOfOriginServerUrl: Int = getColumnIndexOrThrow(_stmt, "originServerUrl")
        val _columnIndexOfPositionMs: Int = getColumnIndexOrThrow(_stmt, "positionMs")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDirty: Int = getColumnIndexOrThrow(_stmt, "dirty")
        val _columnIndexOfConflictPositionMs: Int = getColumnIndexOrThrow(_stmt, "conflictPositionMs")
        val _columnIndexOfConflictUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "conflictUpdatedAt")
        val _result: ProgressEntity?
        if (_stmt.step()) {
          val _tmpItemId: String
          _tmpItemId = _stmt.getText(_columnIndexOfItemId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpPositionMs: Long
          _tmpPositionMs = _stmt.getLong(_columnIndexOfPositionMs)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDirty: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfDirty).toInt()
          _tmpDirty = _tmp != 0
          val _tmpConflictPositionMs: Long?
          if (_stmt.isNull(_columnIndexOfConflictPositionMs)) {
            _tmpConflictPositionMs = null
          } else {
            _tmpConflictPositionMs = _stmt.getLong(_columnIndexOfConflictPositionMs)
          }
          val _tmpConflictUpdatedAt: Long?
          if (_stmt.isNull(_columnIndexOfConflictUpdatedAt)) {
            _tmpConflictUpdatedAt = null
          } else {
            _tmpConflictUpdatedAt = _stmt.getLong(_columnIndexOfConflictUpdatedAt)
          }
          _result = ProgressEntity(_tmpItemId,_tmpOriginServerUrl,_tmpPositionMs,_tmpDurationMs,_tmpUpdatedAt,_tmpDirty,_tmpConflictPositionMs,_tmpConflictUpdatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun allForServer(originServerUrl: String): List<ProgressEntity> {
    val _sql: String = "SELECT * FROM progress WHERE originServerUrl = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, originServerUrl)
        val _columnIndexOfItemId: Int = getColumnIndexOrThrow(_stmt, "itemId")
        val _columnIndexOfOriginServerUrl: Int = getColumnIndexOrThrow(_stmt, "originServerUrl")
        val _columnIndexOfPositionMs: Int = getColumnIndexOrThrow(_stmt, "positionMs")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDirty: Int = getColumnIndexOrThrow(_stmt, "dirty")
        val _columnIndexOfConflictPositionMs: Int = getColumnIndexOrThrow(_stmt, "conflictPositionMs")
        val _columnIndexOfConflictUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "conflictUpdatedAt")
        val _result: MutableList<ProgressEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProgressEntity
          val _tmpItemId: String
          _tmpItemId = _stmt.getText(_columnIndexOfItemId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpPositionMs: Long
          _tmpPositionMs = _stmt.getLong(_columnIndexOfPositionMs)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDirty: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfDirty).toInt()
          _tmpDirty = _tmp != 0
          val _tmpConflictPositionMs: Long?
          if (_stmt.isNull(_columnIndexOfConflictPositionMs)) {
            _tmpConflictPositionMs = null
          } else {
            _tmpConflictPositionMs = _stmt.getLong(_columnIndexOfConflictPositionMs)
          }
          val _tmpConflictUpdatedAt: Long?
          if (_stmt.isNull(_columnIndexOfConflictUpdatedAt)) {
            _tmpConflictUpdatedAt = null
          } else {
            _tmpConflictUpdatedAt = _stmt.getLong(_columnIndexOfConflictUpdatedAt)
          }
          _item = ProgressEntity(_tmpItemId,_tmpOriginServerUrl,_tmpPositionMs,_tmpDurationMs,_tmpUpdatedAt,_tmpDirty,_tmpConflictPositionMs,_tmpConflictUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeRecentProgressForServer(originServerUrl: String, limit: Int): Flow<List<ProgressEntity>> {
    val _sql: String = """
        |SELECT * FROM progress
        |           WHERE originServerUrl = ?
        |             AND positionMs > 0
        |             AND (durationMs <= 0 OR positionMs < durationMs)
        |           ORDER BY updatedAt DESC
        |           LIMIT ?
        """.trimMargin()
    return createFlow(__db, false, arrayOf("progress")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, originServerUrl)
        _argIndex = 2
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfItemId: Int = getColumnIndexOrThrow(_stmt, "itemId")
        val _columnIndexOfOriginServerUrl: Int = getColumnIndexOrThrow(_stmt, "originServerUrl")
        val _columnIndexOfPositionMs: Int = getColumnIndexOrThrow(_stmt, "positionMs")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDirty: Int = getColumnIndexOrThrow(_stmt, "dirty")
        val _columnIndexOfConflictPositionMs: Int = getColumnIndexOrThrow(_stmt, "conflictPositionMs")
        val _columnIndexOfConflictUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "conflictUpdatedAt")
        val _result: MutableList<ProgressEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProgressEntity
          val _tmpItemId: String
          _tmpItemId = _stmt.getText(_columnIndexOfItemId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpPositionMs: Long
          _tmpPositionMs = _stmt.getLong(_columnIndexOfPositionMs)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDirty: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfDirty).toInt()
          _tmpDirty = _tmp != 0
          val _tmpConflictPositionMs: Long?
          if (_stmt.isNull(_columnIndexOfConflictPositionMs)) {
            _tmpConflictPositionMs = null
          } else {
            _tmpConflictPositionMs = _stmt.getLong(_columnIndexOfConflictPositionMs)
          }
          val _tmpConflictUpdatedAt: Long?
          if (_stmt.isNull(_columnIndexOfConflictUpdatedAt)) {
            _tmpConflictUpdatedAt = null
          } else {
            _tmpConflictUpdatedAt = _stmt.getLong(_columnIndexOfConflictUpdatedAt)
          }
          _item = ProgressEntity(_tmpItemId,_tmpOriginServerUrl,_tmpPositionMs,_tmpDurationMs,_tmpUpdatedAt,_tmpDirty,_tmpConflictPositionMs,_tmpConflictUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeConflicts(): Flow<List<ProgressEntity>> {
    val _sql: String = "SELECT * FROM progress WHERE conflictPositionMs IS NOT NULL"
    return createFlow(__db, false, arrayOf("progress")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfItemId: Int = getColumnIndexOrThrow(_stmt, "itemId")
        val _columnIndexOfOriginServerUrl: Int = getColumnIndexOrThrow(_stmt, "originServerUrl")
        val _columnIndexOfPositionMs: Int = getColumnIndexOrThrow(_stmt, "positionMs")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDirty: Int = getColumnIndexOrThrow(_stmt, "dirty")
        val _columnIndexOfConflictPositionMs: Int = getColumnIndexOrThrow(_stmt, "conflictPositionMs")
        val _columnIndexOfConflictUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "conflictUpdatedAt")
        val _result: MutableList<ProgressEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProgressEntity
          val _tmpItemId: String
          _tmpItemId = _stmt.getText(_columnIndexOfItemId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpPositionMs: Long
          _tmpPositionMs = _stmt.getLong(_columnIndexOfPositionMs)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDirty: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfDirty).toInt()
          _tmpDirty = _tmp != 0
          val _tmpConflictPositionMs: Long?
          if (_stmt.isNull(_columnIndexOfConflictPositionMs)) {
            _tmpConflictPositionMs = null
          } else {
            _tmpConflictPositionMs = _stmt.getLong(_columnIndexOfConflictPositionMs)
          }
          val _tmpConflictUpdatedAt: Long?
          if (_stmt.isNull(_columnIndexOfConflictUpdatedAt)) {
            _tmpConflictUpdatedAt = null
          } else {
            _tmpConflictUpdatedAt = _stmt.getLong(_columnIndexOfConflictUpdatedAt)
          }
          _item = ProgressEntity(_tmpItemId,_tmpOriginServerUrl,_tmpPositionMs,_tmpDurationMs,_tmpUpdatedAt,_tmpDirty,_tmpConflictPositionMs,_tmpConflictUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeRecentForServer(originServerUrl: String, limit: Int): Flow<List<RecentPlaybackItem>> {
    val _sql: String = """
        |SELECT p.itemId, p.originServerUrl, i.title, i.author, i.coverPath,
        |                  i.localCoverPath, p.positionMs, p.durationMs, p.updatedAt,
        |                  EXISTS (
        |                      SELECT 1 FROM downloads d
        |                      WHERE d.itemId = p.itemId
        |                        AND d.originServerUrl = p.originServerUrl
        |                        AND d.status = 'DOWNLOADED'
        |                  ) AS downloaded
        |           FROM progress p
        |           INNER JOIN library_items i
        |             ON i.id = p.itemId AND i.originServerUrl = p.originServerUrl
        |           WHERE p.originServerUrl = ?
        |             AND p.positionMs > 0
        |             AND (p.durationMs <= 0 OR p.positionMs < p.durationMs)
        |           ORDER BY p.updatedAt DESC
        |           LIMIT ?
        """.trimMargin()
    return createFlow(__db, false, arrayOf("downloads", "progress", "library_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, originServerUrl)
        _argIndex = 2
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfItemId: Int = 0
        val _columnIndexOfOriginServerUrl: Int = 1
        val _columnIndexOfTitle: Int = 2
        val _columnIndexOfAuthor: Int = 3
        val _columnIndexOfCoverPath: Int = 4
        val _columnIndexOfLocalCoverPath: Int = 5
        val _columnIndexOfPositionMs: Int = 6
        val _columnIndexOfDurationMs: Int = 7
        val _columnIndexOfUpdatedAt: Int = 8
        val _columnIndexOfDownloaded: Int = 9
        val _result: MutableList<RecentPlaybackItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: RecentPlaybackItem
          val _tmpItemId: String
          _tmpItemId = _stmt.getText(_columnIndexOfItemId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpAuthor: String?
          if (_stmt.isNull(_columnIndexOfAuthor)) {
            _tmpAuthor = null
          } else {
            _tmpAuthor = _stmt.getText(_columnIndexOfAuthor)
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
          val _tmpPositionMs: Long
          _tmpPositionMs = _stmt.getLong(_columnIndexOfPositionMs)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDownloaded: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfDownloaded).toInt()
          _tmpDownloaded = _tmp != 0
          _item = RecentPlaybackItem(_tmpItemId,_tmpOriginServerUrl,_tmpTitle,_tmpAuthor,_tmpCoverPath,_tmpLocalCoverPath,_tmpPositionMs,_tmpDurationMs,_tmpUpdatedAt,_tmpDownloaded)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeRecentDownloaded(limit: Int): Flow<List<RecentPlaybackItem>> {
    val _sql: String = """
        |SELECT p.itemId, p.originServerUrl, i.title, i.author, i.coverPath,
        |                  i.localCoverPath, p.positionMs, p.durationMs, p.updatedAt,
        |                  1 AS downloaded
        |           FROM progress p
        |           INNER JOIN library_items i
        |             ON i.id = p.itemId AND i.originServerUrl = p.originServerUrl
        |           INNER JOIN downloads d
        |             ON d.itemId = p.itemId
        |            AND d.originServerUrl = p.originServerUrl
        |            AND d.status = 'DOWNLOADED'
        |           WHERE p.positionMs > 0
        |             AND (p.durationMs <= 0 OR p.positionMs < p.durationMs)
        |           ORDER BY p.updatedAt DESC
        |           LIMIT ?
        """.trimMargin()
    return createFlow(__db, false, arrayOf("progress", "library_items", "downloads")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfItemId: Int = 0
        val _columnIndexOfOriginServerUrl: Int = 1
        val _columnIndexOfTitle: Int = 2
        val _columnIndexOfAuthor: Int = 3
        val _columnIndexOfCoverPath: Int = 4
        val _columnIndexOfLocalCoverPath: Int = 5
        val _columnIndexOfPositionMs: Int = 6
        val _columnIndexOfDurationMs: Int = 7
        val _columnIndexOfUpdatedAt: Int = 8
        val _columnIndexOfDownloaded: Int = 9
        val _result: MutableList<RecentPlaybackItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: RecentPlaybackItem
          val _tmpItemId: String
          _tmpItemId = _stmt.getText(_columnIndexOfItemId)
          val _tmpOriginServerUrl: String
          _tmpOriginServerUrl = _stmt.getText(_columnIndexOfOriginServerUrl)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpAuthor: String?
          if (_stmt.isNull(_columnIndexOfAuthor)) {
            _tmpAuthor = null
          } else {
            _tmpAuthor = _stmt.getText(_columnIndexOfAuthor)
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
          val _tmpPositionMs: Long
          _tmpPositionMs = _stmt.getLong(_columnIndexOfPositionMs)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDownloaded: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfDownloaded).toInt()
          _tmpDownloaded = _tmp != 0
          _item = RecentPlaybackItem(_tmpItemId,_tmpOriginServerUrl,_tmpTitle,_tmpAuthor,_tmpCoverPath,_tmpLocalCoverPath,_tmpPositionMs,_tmpDurationMs,_tmpUpdatedAt,_tmpDownloaded)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markCleanIfUnchanged(
    itemId: String,
    originServerUrl: String,
    expectedUpdatedAt: Long,
  ): Int {
    val _sql: String = """
        |UPDATE progress SET dirty = 0
        |           WHERE itemId = ? AND originServerUrl = ?
        |           AND updatedAt = ?
        """.trimMargin()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, itemId)
        _argIndex = 2
        _stmt.bindText(_argIndex, originServerUrl)
        _argIndex = 3
        _stmt.bindLong(_argIndex, expectedUpdatedAt)
        _stmt.step()
        getTotalChangedRows(_connection)
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}

package com.nortlinos.wearos.data.repository

import com.nortlinos.wearos.data.local.ProgressEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressConflictResolverTest {
    @Test
    fun `dirty newer local progress is pushed`() {
        assertEquals(ProgressSyncAction.PUSH, resolve(localUpdatedAt = 200, remoteUpdatedAt = 100, dirty = true))
    }

    @Test
    fun `newer remote progress is pulled when local is clean`() {
        assertEquals(ProgressSyncAction.PULL, resolve(localUpdatedAt = 100, remoteUpdatedAt = 200, dirty = false))
    }

    @Test
    fun `newer remote and dirty local preserves both as conflict`() {
        assertEquals(ProgressSyncAction.CONFLICT, resolve(localUpdatedAt = 100, remoteUpdatedAt = 200, dirty = true))
    }

    @Test
    fun `equal timestamps never discard a dirty local update`() {
        assertEquals(ProgressSyncAction.PUSH, resolve(localUpdatedAt = 200, remoteUpdatedAt = 200, dirty = true))
    }

    @Test
    fun `pending conflict is not pushed even once local becomes newer`() {
        assertEquals(
            ProgressSyncAction.CONFLICT,
            resolve(localUpdatedAt = 300, remoteUpdatedAt = 200, dirty = true, pendingConflict = true)
        )
    }

    @Test
    fun `resolved conflict pushes normally`() {
        assertEquals(
            ProgressSyncAction.PUSH,
            resolve(localUpdatedAt = 300, remoteUpdatedAt = 200, dirty = true, pendingConflict = false)
        )
    }

    private fun resolve(
        localUpdatedAt: Long,
        remoteUpdatedAt: Long,
        dirty: Boolean,
        pendingConflict: Boolean = false
    ) =
        ProgressConflictResolver.resolve(
            local = ProgressEntity(
                itemId = "book",
                originServerUrl = "https://server.example",
                positionMs = 1_000,
                durationMs = 10_000,
                updatedAt = localUpdatedAt,
                dirty = dirty,
                conflictPositionMs = if (pendingConflict) 5_000 else null,
                conflictUpdatedAt = if (pendingConflict) 200 else null
            ),
            remoteUpdatedAt = remoteUpdatedAt,
            remoteExists = true
        )
}

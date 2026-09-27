package com.nortlinos.wearos.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nortlinos.wearos.data.repository.ProgressSyncEngine
import com.nortlinos.wearos.data.repository.SessionRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import retrofit2.HttpException
import java.io.IOException

class ProgressSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val engine = EntryPointAccessors.fromApplication(
            applicationContext,
            SyncEntryPoint::class.java
        )
        if (engine.sessionRepository().session.value == null) return Result.success()
        return try {
            engine.syncEngine().sync()
            Result.success()
        } catch (error: HttpException) {
            if (error.code() == 401 || error.code() == 403) {
                Result.failure()
            } else {
                Result.retry()
            }
        } catch (error: IOException) {
            Result.retry()
        } catch (error: Exception) {
            Result.failure()
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SyncEntryPoint {
        fun syncEngine(): ProgressSyncEngine
        fun sessionRepository(): SessionRepository
    }

    companion object {
        const val UNIQUE_WORK_NAME = "progress-sync-now"
        const val PERIODIC_WORK_NAME = "progress-sync-periodic"
    }
}

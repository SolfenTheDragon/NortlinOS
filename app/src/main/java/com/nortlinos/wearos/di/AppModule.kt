package com.nortlinos.wearos.di

import android.content.Context
import com.nortlinos.wearos.data.api.ApiClient
import com.nortlinos.wearos.data.local.AppDatabase
import com.nortlinos.wearos.data.local.DownloadDao
import com.nortlinos.wearos.data.local.LibraryDao
import com.nortlinos.wearos.data.local.ProgressDao
import com.nortlinos.wearos.service.ExpiryAlarm
import com.nortlinos.wearos.service.SystemExpiryAlarm
import com.nortlinos.wearos.service.TimeSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): AppDatabase = AppDatabase.create(context)

    /**
     * One shared client for every HTTP caller (API, downloads, cover art). OkHttp's own guidance
     * is that clients should be shared: each new instance gets an empty connection pool, so a
     * per-request or per-worker client forces a fresh TCP + TLS handshake and keeps the radio
     * powered longer than necessary. Sharing reuses warm connections instead.
     */
    @Provides
    @Singleton
    fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides fun libraryDao(database: AppDatabase): LibraryDao = database.libraryDao()
    @Provides fun downloadDao(database: AppDatabase): DownloadDao = database.downloadDao()
    @Provides fun progressDao(database: AppDatabase): ProgressDao = database.progressDao()

    @Provides
    @Singleton
    fun expiryAlarm(alarm: SystemExpiryAlarm): ExpiryAlarm = alarm

    @Provides
    @Singleton
    fun timeSource(): TimeSource = TimeSource.System
}

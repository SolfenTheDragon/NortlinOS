package com.nortlinos.wearos.di

import android.annotation.SuppressLint
import android.content.Context
import com.nortlinos.wearos.BuildConfig
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
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
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
        .apply { if (BuildConfig.ALLOW_INSECURE_SERVERS) trustAllCertificates() }
        .build()

    /**
     * Dev-only: accepts self-signed/untrusted certificates and any hostname. Compiled out of
     * effect when `nortlinos.allowInsecureServers=false` (see gradle.properties).
     */
    @SuppressLint("CustomX509TrustManager", "TrustAllX509TrustManager", "BadHostnameVerifier")
    private fun OkHttpClient.Builder.trustAllCertificates(): OkHttpClient.Builder {
        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        }
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
        }
        return sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
    }

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

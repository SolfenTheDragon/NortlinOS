package com.nortlinos.wearos.presentation.viewmodel;

import android.content.Context;
import com.nortlinos.wearos.data.api.ApiClient;
import com.nortlinos.wearos.data.local.SettingsStore;
import com.nortlinos.wearos.data.repository.DownloadRepository;
import com.nortlinos.wearos.data.repository.LibraryRepository;
import com.nortlinos.wearos.data.repository.ProgressRepository;
import com.nortlinos.wearos.data.repository.SessionRepository;
import com.nortlinos.wearos.service.AudioOutputs;
import com.nortlinos.wearos.service.ConnectivityMonitor;
import com.nortlinos.wearos.service.SleepTimerManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class PlayerViewModel_Factory implements Factory<PlayerViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<ApiClient> apiClientProvider;

  private final Provider<SessionRepository> sessionRepositoryProvider;

  private final Provider<LibraryRepository> libraryRepositoryProvider;

  private final Provider<DownloadRepository> downloadRepositoryProvider;

  private final Provider<ProgressRepository> progressRepositoryProvider;

  private final Provider<SleepTimerManager> sleepTimerManagerProvider;

  private final Provider<ConnectivityMonitor> connectivityMonitorProvider;

  private final Provider<SettingsStore> settingsStoreProvider;

  private final Provider<AudioOutputs> audioOutputsProvider;

  private PlayerViewModel_Factory(Provider<Context> contextProvider,
      Provider<ApiClient> apiClientProvider, Provider<SessionRepository> sessionRepositoryProvider,
      Provider<LibraryRepository> libraryRepositoryProvider,
      Provider<DownloadRepository> downloadRepositoryProvider,
      Provider<ProgressRepository> progressRepositoryProvider,
      Provider<SleepTimerManager> sleepTimerManagerProvider,
      Provider<ConnectivityMonitor> connectivityMonitorProvider,
      Provider<SettingsStore> settingsStoreProvider, Provider<AudioOutputs> audioOutputsProvider) {
    this.contextProvider = contextProvider;
    this.apiClientProvider = apiClientProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.libraryRepositoryProvider = libraryRepositoryProvider;
    this.downloadRepositoryProvider = downloadRepositoryProvider;
    this.progressRepositoryProvider = progressRepositoryProvider;
    this.sleepTimerManagerProvider = sleepTimerManagerProvider;
    this.connectivityMonitorProvider = connectivityMonitorProvider;
    this.settingsStoreProvider = settingsStoreProvider;
    this.audioOutputsProvider = audioOutputsProvider;
  }

  @Override
  public PlayerViewModel get() {
    return newInstance(contextProvider.get(), apiClientProvider.get(), sessionRepositoryProvider.get(), libraryRepositoryProvider.get(), downloadRepositoryProvider.get(), progressRepositoryProvider.get(), sleepTimerManagerProvider.get(), connectivityMonitorProvider.get(), settingsStoreProvider.get(), audioOutputsProvider.get());
  }

  public static PlayerViewModel_Factory create(Provider<Context> contextProvider,
      Provider<ApiClient> apiClientProvider, Provider<SessionRepository> sessionRepositoryProvider,
      Provider<LibraryRepository> libraryRepositoryProvider,
      Provider<DownloadRepository> downloadRepositoryProvider,
      Provider<ProgressRepository> progressRepositoryProvider,
      Provider<SleepTimerManager> sleepTimerManagerProvider,
      Provider<ConnectivityMonitor> connectivityMonitorProvider,
      Provider<SettingsStore> settingsStoreProvider, Provider<AudioOutputs> audioOutputsProvider) {
    return new PlayerViewModel_Factory(contextProvider, apiClientProvider, sessionRepositoryProvider, libraryRepositoryProvider, downloadRepositoryProvider, progressRepositoryProvider, sleepTimerManagerProvider, connectivityMonitorProvider, settingsStoreProvider, audioOutputsProvider);
  }

  public static PlayerViewModel newInstance(Context context, ApiClient apiClient,
      SessionRepository sessionRepository, LibraryRepository libraryRepository,
      DownloadRepository downloadRepository, ProgressRepository progressRepository,
      SleepTimerManager sleepTimerManager, ConnectivityMonitor connectivityMonitor,
      SettingsStore settingsStore, AudioOutputs audioOutputs) {
    return new PlayerViewModel(context, apiClient, sessionRepository, libraryRepository, downloadRepository, progressRepository, sleepTimerManager, connectivityMonitor, settingsStore, audioOutputs);
  }
}

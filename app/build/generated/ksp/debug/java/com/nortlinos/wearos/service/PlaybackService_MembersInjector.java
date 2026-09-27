package com.nortlinos.wearos.service;

import com.nortlinos.wearos.data.repository.ProgressRepository;
import com.nortlinos.wearos.data.repository.SessionRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

@QualifierMetadata
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
public final class PlaybackService_MembersInjector implements MembersInjector<PlaybackService> {
  private final Provider<ProgressRepository> progressRepositoryProvider;

  private final Provider<SessionRepository> sessionRepositoryProvider;

  private final Provider<ConnectivityMonitor> connectivityMonitorProvider;

  private final Provider<SleepTimerManager> sleepTimerManagerProvider;

  private final Provider<OkHttpClient> okHttpClientProvider;

  private PlaybackService_MembersInjector(Provider<ProgressRepository> progressRepositoryProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<ConnectivityMonitor> connectivityMonitorProvider,
      Provider<SleepTimerManager> sleepTimerManagerProvider,
      Provider<OkHttpClient> okHttpClientProvider) {
    this.progressRepositoryProvider = progressRepositoryProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.connectivityMonitorProvider = connectivityMonitorProvider;
    this.sleepTimerManagerProvider = sleepTimerManagerProvider;
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public void injectMembers(PlaybackService instance) {
    injectProgressRepository(instance, progressRepositoryProvider.get());
    injectSessionRepository(instance, sessionRepositoryProvider.get());
    injectConnectivityMonitor(instance, connectivityMonitorProvider.get());
    injectSleepTimerManager(instance, sleepTimerManagerProvider.get());
    injectOkHttpClient(instance, okHttpClientProvider.get());
  }

  public static MembersInjector<PlaybackService> create(
      Provider<ProgressRepository> progressRepositoryProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<ConnectivityMonitor> connectivityMonitorProvider,
      Provider<SleepTimerManager> sleepTimerManagerProvider,
      Provider<OkHttpClient> okHttpClientProvider) {
    return new PlaybackService_MembersInjector(progressRepositoryProvider, sessionRepositoryProvider, connectivityMonitorProvider, sleepTimerManagerProvider, okHttpClientProvider);
  }

  @InjectedFieldSignature("com.nortlinos.wearos.service.PlaybackService.progressRepository")
  public static void injectProgressRepository(PlaybackService instance,
      ProgressRepository progressRepository) {
    instance.progressRepository = progressRepository;
  }

  @InjectedFieldSignature("com.nortlinos.wearos.service.PlaybackService.sessionRepository")
  public static void injectSessionRepository(PlaybackService instance,
      SessionRepository sessionRepository) {
    instance.sessionRepository = sessionRepository;
  }

  @InjectedFieldSignature("com.nortlinos.wearos.service.PlaybackService.connectivityMonitor")
  public static void injectConnectivityMonitor(PlaybackService instance,
      ConnectivityMonitor connectivityMonitor) {
    instance.connectivityMonitor = connectivityMonitor;
  }

  @InjectedFieldSignature("com.nortlinos.wearos.service.PlaybackService.sleepTimerManager")
  public static void injectSleepTimerManager(PlaybackService instance,
      SleepTimerManager sleepTimerManager) {
    instance.sleepTimerManager = sleepTimerManager;
  }

  @InjectedFieldSignature("com.nortlinos.wearos.service.PlaybackService.okHttpClient")
  public static void injectOkHttpClient(PlaybackService instance, OkHttpClient okHttpClient) {
    instance.okHttpClient = okHttpClient;
  }
}

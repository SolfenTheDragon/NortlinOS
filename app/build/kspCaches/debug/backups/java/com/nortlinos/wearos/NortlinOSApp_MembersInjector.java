package com.nortlinos.wearos;

import com.nortlinos.wearos.data.repository.SessionRepository;
import com.nortlinos.wearos.service.ConnectivityMonitor;
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
public final class NortlinOSApp_MembersInjector implements MembersInjector<NortlinOSApp> {
  private final Provider<ConnectivityMonitor> connectivityMonitorProvider;

  private final Provider<SessionRepository> sessionRepositoryProvider;

  private final Provider<OkHttpClient> okHttpClientProvider;

  private NortlinOSApp_MembersInjector(Provider<ConnectivityMonitor> connectivityMonitorProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<OkHttpClient> okHttpClientProvider) {
    this.connectivityMonitorProvider = connectivityMonitorProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public void injectMembers(NortlinOSApp instance) {
    injectConnectivityMonitor(instance, connectivityMonitorProvider.get());
    injectSessionRepository(instance, sessionRepositoryProvider.get());
    injectOkHttpClient(instance, okHttpClientProvider.get());
  }

  public static MembersInjector<NortlinOSApp> create(
      Provider<ConnectivityMonitor> connectivityMonitorProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<OkHttpClient> okHttpClientProvider) {
    return new NortlinOSApp_MembersInjector(connectivityMonitorProvider, sessionRepositoryProvider, okHttpClientProvider);
  }

  @InjectedFieldSignature("com.nortlinos.wearos.NortlinOSApp.connectivityMonitor")
  public static void injectConnectivityMonitor(NortlinOSApp instance,
      ConnectivityMonitor connectivityMonitor) {
    instance.connectivityMonitor = connectivityMonitor;
  }

  @InjectedFieldSignature("com.nortlinos.wearos.NortlinOSApp.sessionRepository")
  public static void injectSessionRepository(NortlinOSApp instance,
      SessionRepository sessionRepository) {
    instance.sessionRepository = sessionRepository;
  }

  @InjectedFieldSignature("com.nortlinos.wearos.NortlinOSApp.okHttpClient")
  public static void injectOkHttpClient(NortlinOSApp instance, OkHttpClient okHttpClient) {
    instance.okHttpClient = okHttpClient;
  }
}

package com.nortlinos.wearos.data.repository;

import com.nortlinos.wearos.data.api.ApiClient;
import com.nortlinos.wearos.data.local.SessionStore;
import com.nortlinos.wearos.service.ConnectivityMonitor;
import com.nortlinos.wearos.service.OidcAuthorizers;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class SessionRepository_Factory implements Factory<SessionRepository> {
  private final Provider<ApiClient> apiClientProvider;

  private final Provider<SessionStore> sessionStoreProvider;

  private final Provider<OidcAuthorizers> authorizersProvider;

  private final Provider<ConnectivityMonitor> connectivityMonitorProvider;

  private SessionRepository_Factory(Provider<ApiClient> apiClientProvider,
      Provider<SessionStore> sessionStoreProvider, Provider<OidcAuthorizers> authorizersProvider,
      Provider<ConnectivityMonitor> connectivityMonitorProvider) {
    this.apiClientProvider = apiClientProvider;
    this.sessionStoreProvider = sessionStoreProvider;
    this.authorizersProvider = authorizersProvider;
    this.connectivityMonitorProvider = connectivityMonitorProvider;
  }

  @Override
  public SessionRepository get() {
    return newInstance(apiClientProvider.get(), sessionStoreProvider.get(), authorizersProvider.get(), connectivityMonitorProvider.get());
  }

  public static SessionRepository_Factory create(Provider<ApiClient> apiClientProvider,
      Provider<SessionStore> sessionStoreProvider, Provider<OidcAuthorizers> authorizersProvider,
      Provider<ConnectivityMonitor> connectivityMonitorProvider) {
    return new SessionRepository_Factory(apiClientProvider, sessionStoreProvider, authorizersProvider, connectivityMonitorProvider);
  }

  public static SessionRepository newInstance(ApiClient apiClient, SessionStore sessionStore,
      OidcAuthorizers authorizers, ConnectivityMonitor connectivityMonitor) {
    return new SessionRepository(apiClient, sessionStore, authorizers, connectivityMonitor);
  }
}

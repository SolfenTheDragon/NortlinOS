package com.nortlinos.wearos.data.repository;

import com.nortlinos.wearos.data.api.ApiClient;
import com.nortlinos.wearos.data.local.AppDatabase;
import com.nortlinos.wearos.data.local.LibraryDao;
import com.nortlinos.wearos.service.ConnectivityMonitor;
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
public final class LibraryRepository_Factory implements Factory<LibraryRepository> {
  private final Provider<ApiClient> apiClientProvider;

  private final Provider<AppDatabase> databaseProvider;

  private final Provider<LibraryDao> libraryDaoProvider;

  private final Provider<SessionRepository> sessionRepositoryProvider;

  private final Provider<ConnectivityMonitor> connectivityMonitorProvider;

  private LibraryRepository_Factory(Provider<ApiClient> apiClientProvider,
      Provider<AppDatabase> databaseProvider, Provider<LibraryDao> libraryDaoProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<ConnectivityMonitor> connectivityMonitorProvider) {
    this.apiClientProvider = apiClientProvider;
    this.databaseProvider = databaseProvider;
    this.libraryDaoProvider = libraryDaoProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.connectivityMonitorProvider = connectivityMonitorProvider;
  }

  @Override
  public LibraryRepository get() {
    return newInstance(apiClientProvider.get(), databaseProvider.get(), libraryDaoProvider.get(), sessionRepositoryProvider.get(), connectivityMonitorProvider.get());
  }

  public static LibraryRepository_Factory create(Provider<ApiClient> apiClientProvider,
      Provider<AppDatabase> databaseProvider, Provider<LibraryDao> libraryDaoProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<ConnectivityMonitor> connectivityMonitorProvider) {
    return new LibraryRepository_Factory(apiClientProvider, databaseProvider, libraryDaoProvider, sessionRepositoryProvider, connectivityMonitorProvider);
  }

  public static LibraryRepository newInstance(ApiClient apiClient, AppDatabase database,
      LibraryDao libraryDao, SessionRepository sessionRepository,
      ConnectivityMonitor connectivityMonitor) {
    return new LibraryRepository(apiClient, database, libraryDao, sessionRepository, connectivityMonitor);
  }
}

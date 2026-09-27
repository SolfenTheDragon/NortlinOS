package com.nortlinos.wearos.presentation.viewmodel;

import com.nortlinos.wearos.data.repository.DownloadRepository;
import com.nortlinos.wearos.data.repository.LibraryRepository;
import com.nortlinos.wearos.data.repository.ProgressRepository;
import com.nortlinos.wearos.data.repository.SessionRepository;
import com.nortlinos.wearos.service.ConnectivityMonitor;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class LibraryViewModel_Factory implements Factory<LibraryViewModel> {
  private final Provider<LibraryRepository> libraryRepositoryProvider;

  private final Provider<DownloadRepository> downloadRepositoryProvider;

  private final Provider<ProgressRepository> progressRepositoryProvider;

  private final Provider<ConnectivityMonitor> connectivityMonitorProvider;

  private final Provider<SessionRepository> sessionRepositoryProvider;

  private LibraryViewModel_Factory(Provider<LibraryRepository> libraryRepositoryProvider,
      Provider<DownloadRepository> downloadRepositoryProvider,
      Provider<ProgressRepository> progressRepositoryProvider,
      Provider<ConnectivityMonitor> connectivityMonitorProvider,
      Provider<SessionRepository> sessionRepositoryProvider) {
    this.libraryRepositoryProvider = libraryRepositoryProvider;
    this.downloadRepositoryProvider = downloadRepositoryProvider;
    this.progressRepositoryProvider = progressRepositoryProvider;
    this.connectivityMonitorProvider = connectivityMonitorProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
  }

  @Override
  public LibraryViewModel get() {
    return newInstance(libraryRepositoryProvider.get(), downloadRepositoryProvider.get(), progressRepositoryProvider.get(), connectivityMonitorProvider.get(), sessionRepositoryProvider.get());
  }

  public static LibraryViewModel_Factory create(
      Provider<LibraryRepository> libraryRepositoryProvider,
      Provider<DownloadRepository> downloadRepositoryProvider,
      Provider<ProgressRepository> progressRepositoryProvider,
      Provider<ConnectivityMonitor> connectivityMonitorProvider,
      Provider<SessionRepository> sessionRepositoryProvider) {
    return new LibraryViewModel_Factory(libraryRepositoryProvider, downloadRepositoryProvider, progressRepositoryProvider, connectivityMonitorProvider, sessionRepositoryProvider);
  }

  public static LibraryViewModel newInstance(LibraryRepository libraryRepository,
      DownloadRepository downloadRepository, ProgressRepository progressRepository,
      ConnectivityMonitor connectivityMonitor, SessionRepository sessionRepository) {
    return new LibraryViewModel(libraryRepository, downloadRepository, progressRepository, connectivityMonitor, sessionRepository);
  }
}

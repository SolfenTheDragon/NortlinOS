package com.nortlinos.wearos.data.repository;

import android.content.Context;
import com.nortlinos.wearos.data.local.DownloadDao;
import com.nortlinos.wearos.data.local.LibraryDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class DownloadRepository_Factory implements Factory<DownloadRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<DownloadDao> downloadDaoProvider;

  private final Provider<LibraryDao> libraryDaoProvider;

  private final Provider<SessionRepository> sessionRepositoryProvider;

  private DownloadRepository_Factory(Provider<Context> contextProvider,
      Provider<DownloadDao> downloadDaoProvider, Provider<LibraryDao> libraryDaoProvider,
      Provider<SessionRepository> sessionRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.downloadDaoProvider = downloadDaoProvider;
    this.libraryDaoProvider = libraryDaoProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
  }

  @Override
  public DownloadRepository get() {
    return newInstance(contextProvider.get(), downloadDaoProvider.get(), libraryDaoProvider.get(), sessionRepositoryProvider.get());
  }

  public static DownloadRepository_Factory create(Provider<Context> contextProvider,
      Provider<DownloadDao> downloadDaoProvider, Provider<LibraryDao> libraryDaoProvider,
      Provider<SessionRepository> sessionRepositoryProvider) {
    return new DownloadRepository_Factory(contextProvider, downloadDaoProvider, libraryDaoProvider, sessionRepositoryProvider);
  }

  public static DownloadRepository newInstance(Context context, DownloadDao downloadDao,
      LibraryDao libraryDao, SessionRepository sessionRepository) {
    return new DownloadRepository(context, downloadDao, libraryDao, sessionRepository);
  }
}

package com.nortlinos.wearos.presentation.viewmodel;

import com.nortlinos.wearos.data.local.SettingsStore;
import com.nortlinos.wearos.data.repository.DownloadRepository;
import com.nortlinos.wearos.data.repository.SessionRepository;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<SettingsStore> settingsStoreProvider;

  private final Provider<SessionRepository> sessionRepositoryProvider;

  private final Provider<DownloadRepository> downloadRepositoryProvider;

  private SettingsViewModel_Factory(Provider<SettingsStore> settingsStoreProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<DownloadRepository> downloadRepositoryProvider) {
    this.settingsStoreProvider = settingsStoreProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.downloadRepositoryProvider = downloadRepositoryProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(settingsStoreProvider.get(), sessionRepositoryProvider.get(), downloadRepositoryProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<SettingsStore> settingsStoreProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<DownloadRepository> downloadRepositoryProvider) {
    return new SettingsViewModel_Factory(settingsStoreProvider, sessionRepositoryProvider, downloadRepositoryProvider);
  }

  public static SettingsViewModel newInstance(SettingsStore settingsStore,
      SessionRepository sessionRepository, DownloadRepository downloadRepository) {
    return new SettingsViewModel(settingsStore, sessionRepository, downloadRepository);
  }
}

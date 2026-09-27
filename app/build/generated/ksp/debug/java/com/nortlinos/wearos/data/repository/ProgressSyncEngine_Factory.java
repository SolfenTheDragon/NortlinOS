package com.nortlinos.wearos.data.repository;

import com.nortlinos.wearos.data.api.ApiClient;
import com.nortlinos.wearos.data.local.ProgressDao;
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
public final class ProgressSyncEngine_Factory implements Factory<ProgressSyncEngine> {
  private final Provider<ApiClient> apiClientProvider;

  private final Provider<ProgressDao> progressDaoProvider;

  private final Provider<SessionRepository> sessionRepositoryProvider;

  private ProgressSyncEngine_Factory(Provider<ApiClient> apiClientProvider,
      Provider<ProgressDao> progressDaoProvider,
      Provider<SessionRepository> sessionRepositoryProvider) {
    this.apiClientProvider = apiClientProvider;
    this.progressDaoProvider = progressDaoProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
  }

  @Override
  public ProgressSyncEngine get() {
    return newInstance(apiClientProvider.get(), progressDaoProvider.get(), sessionRepositoryProvider.get());
  }

  public static ProgressSyncEngine_Factory create(Provider<ApiClient> apiClientProvider,
      Provider<ProgressDao> progressDaoProvider,
      Provider<SessionRepository> sessionRepositoryProvider) {
    return new ProgressSyncEngine_Factory(apiClientProvider, progressDaoProvider, sessionRepositoryProvider);
  }

  public static ProgressSyncEngine newInstance(ApiClient apiClient, ProgressDao progressDao,
      SessionRepository sessionRepository) {
    return new ProgressSyncEngine(apiClient, progressDao, sessionRepository);
  }
}

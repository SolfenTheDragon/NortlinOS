package com.nortlinos.wearos.di;

import com.nortlinos.wearos.data.local.AppDatabase;
import com.nortlinos.wearos.data.local.DownloadDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_DownloadDaoFactory implements Factory<DownloadDao> {
  private final Provider<AppDatabase> databaseProvider;

  private AppModule_DownloadDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public DownloadDao get() {
    return downloadDao(databaseProvider.get());
  }

  public static AppModule_DownloadDaoFactory create(Provider<AppDatabase> databaseProvider) {
    return new AppModule_DownloadDaoFactory(databaseProvider);
  }

  public static DownloadDao downloadDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.downloadDao(database));
  }
}

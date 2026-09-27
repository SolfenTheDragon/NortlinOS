package com.nortlinos.wearos.di;

import com.nortlinos.wearos.data.local.AppDatabase;
import com.nortlinos.wearos.data.local.ProgressDao;
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
public final class AppModule_ProgressDaoFactory implements Factory<ProgressDao> {
  private final Provider<AppDatabase> databaseProvider;

  private AppModule_ProgressDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ProgressDao get() {
    return progressDao(databaseProvider.get());
  }

  public static AppModule_ProgressDaoFactory create(Provider<AppDatabase> databaseProvider) {
    return new AppModule_ProgressDaoFactory(databaseProvider);
  }

  public static ProgressDao progressDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.progressDao(database));
  }
}

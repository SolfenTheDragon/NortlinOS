package com.nortlinos.wearos.di;

import com.nortlinos.wearos.data.local.AppDatabase;
import com.nortlinos.wearos.data.local.LibraryDao;
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
public final class AppModule_LibraryDaoFactory implements Factory<LibraryDao> {
  private final Provider<AppDatabase> databaseProvider;

  private AppModule_LibraryDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public LibraryDao get() {
    return libraryDao(databaseProvider.get());
  }

  public static AppModule_LibraryDaoFactory create(Provider<AppDatabase> databaseProvider) {
    return new AppModule_LibraryDaoFactory(databaseProvider);
  }

  public static LibraryDao libraryDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.libraryDao(database));
  }
}

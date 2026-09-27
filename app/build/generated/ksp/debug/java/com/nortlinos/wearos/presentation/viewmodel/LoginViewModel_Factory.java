package com.nortlinos.wearos.presentation.viewmodel;

import com.nortlinos.wearos.data.repository.LibraryRepository;
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
public final class LoginViewModel_Factory implements Factory<LoginViewModel> {
  private final Provider<SessionRepository> sessionRepositoryProvider;

  private final Provider<LibraryRepository> libraryRepositoryProvider;

  private LoginViewModel_Factory(Provider<SessionRepository> sessionRepositoryProvider,
      Provider<LibraryRepository> libraryRepositoryProvider) {
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.libraryRepositoryProvider = libraryRepositoryProvider;
  }

  @Override
  public LoginViewModel get() {
    return newInstance(sessionRepositoryProvider.get(), libraryRepositoryProvider.get());
  }

  public static LoginViewModel_Factory create(Provider<SessionRepository> sessionRepositoryProvider,
      Provider<LibraryRepository> libraryRepositoryProvider) {
    return new LoginViewModel_Factory(sessionRepositoryProvider, libraryRepositoryProvider);
  }

  public static LoginViewModel newInstance(SessionRepository sessionRepository,
      LibraryRepository libraryRepository) {
    return new LoginViewModel(sessionRepository, libraryRepository);
  }
}

package com.nortlinos.wearos.presentation;

import com.nortlinos.wearos.service.OidcRedirectBus;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<OidcRedirectBus> oidcRedirectsProvider;

  private MainActivity_MembersInjector(Provider<OidcRedirectBus> oidcRedirectsProvider) {
    this.oidcRedirectsProvider = oidcRedirectsProvider;
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectOidcRedirects(instance, oidcRedirectsProvider.get());
  }

  public static MembersInjector<MainActivity> create(
      Provider<OidcRedirectBus> oidcRedirectsProvider) {
    return new MainActivity_MembersInjector(oidcRedirectsProvider);
  }

  @InjectedFieldSignature("com.nortlinos.wearos.presentation.MainActivity.oidcRedirects")
  public static void injectOidcRedirects(MainActivity instance, OidcRedirectBus oidcRedirects) {
    instance.oidcRedirects = oidcRedirects;
  }
}

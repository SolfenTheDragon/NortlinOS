package com.nortlinos.wearos.service;

import android.content.Context;
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
public final class OidcAuthorizers_Factory implements Factory<OidcAuthorizers> {
  private final Provider<Context> contextProvider;

  private final Provider<OidcRedirectBus> redirectsProvider;

  private OidcAuthorizers_Factory(Provider<Context> contextProvider,
      Provider<OidcRedirectBus> redirectsProvider) {
    this.contextProvider = contextProvider;
    this.redirectsProvider = redirectsProvider;
  }

  @Override
  public OidcAuthorizers get() {
    return newInstance(contextProvider.get(), redirectsProvider.get());
  }

  public static OidcAuthorizers_Factory create(Provider<Context> contextProvider,
      Provider<OidcRedirectBus> redirectsProvider) {
    return new OidcAuthorizers_Factory(contextProvider, redirectsProvider);
  }

  public static OidcAuthorizers newInstance(Context context, OidcRedirectBus redirects) {
    return new OidcAuthorizers(context, redirects);
  }
}

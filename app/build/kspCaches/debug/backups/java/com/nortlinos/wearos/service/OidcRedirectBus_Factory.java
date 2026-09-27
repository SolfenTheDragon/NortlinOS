package com.nortlinos.wearos.service;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class OidcRedirectBus_Factory implements Factory<OidcRedirectBus> {
  @Override
  public OidcRedirectBus get() {
    return newInstance();
  }

  public static OidcRedirectBus_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static OidcRedirectBus newInstance() {
    return new OidcRedirectBus();
  }

  private static final class InstanceHolder {
    static final OidcRedirectBus_Factory INSTANCE = new OidcRedirectBus_Factory();
  }
}

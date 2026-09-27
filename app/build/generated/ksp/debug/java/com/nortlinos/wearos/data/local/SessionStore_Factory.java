package com.nortlinos.wearos.data.local;

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
public final class SessionStore_Factory implements Factory<SessionStore> {
  private final Provider<Context> contextProvider;

  private SessionStore_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SessionStore get() {
    return newInstance(contextProvider.get());
  }

  public static SessionStore_Factory create(Provider<Context> contextProvider) {
    return new SessionStore_Factory(contextProvider);
  }

  public static SessionStore newInstance(Context context) {
    return new SessionStore(context);
  }
}

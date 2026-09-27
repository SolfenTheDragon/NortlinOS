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
public final class SettingsStore_Factory implements Factory<SettingsStore> {
  private final Provider<Context> contextProvider;

  private SettingsStore_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SettingsStore get() {
    return newInstance(contextProvider.get());
  }

  public static SettingsStore_Factory create(Provider<Context> contextProvider) {
    return new SettingsStore_Factory(contextProvider);
  }

  public static SettingsStore newInstance(Context context) {
    return new SettingsStore(context);
  }
}

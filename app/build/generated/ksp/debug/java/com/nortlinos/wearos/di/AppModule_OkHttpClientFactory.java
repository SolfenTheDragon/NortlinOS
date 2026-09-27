package com.nortlinos.wearos.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

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
public final class AppModule_OkHttpClientFactory implements Factory<OkHttpClient> {
  @Override
  public OkHttpClient get() {
    return okHttpClient();
  }

  public static AppModule_OkHttpClientFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static OkHttpClient okHttpClient() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.okHttpClient());
  }

  private static final class InstanceHolder {
    static final AppModule_OkHttpClientFactory INSTANCE = new AppModule_OkHttpClientFactory();
  }
}

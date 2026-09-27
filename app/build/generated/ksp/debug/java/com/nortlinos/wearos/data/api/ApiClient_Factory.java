package com.nortlinos.wearos.data.api;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
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
public final class ApiClient_Factory implements Factory<ApiClient> {
  private final Provider<OkHttpClient> httpClientProvider;

  private ApiClient_Factory(Provider<OkHttpClient> httpClientProvider) {
    this.httpClientProvider = httpClientProvider;
  }

  @Override
  public ApiClient get() {
    return newInstance(httpClientProvider.get());
  }

  public static ApiClient_Factory create(Provider<OkHttpClient> httpClientProvider) {
    return new ApiClient_Factory(httpClientProvider);
  }

  public static ApiClient newInstance(OkHttpClient httpClient) {
    return new ApiClient(httpClient);
  }
}

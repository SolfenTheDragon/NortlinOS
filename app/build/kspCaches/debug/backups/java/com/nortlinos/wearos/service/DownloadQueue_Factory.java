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
public final class DownloadQueue_Factory implements Factory<DownloadQueue> {
  @Override
  public DownloadQueue get() {
    return newInstance();
  }

  public static DownloadQueue_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DownloadQueue newInstance() {
    return new DownloadQueue();
  }

  private static final class InstanceHolder {
    static final DownloadQueue_Factory INSTANCE = new DownloadQueue_Factory();
  }
}

package com.nortlinos.wearos.di;

import com.nortlinos.wearos.service.TimeSource;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_TimeSourceFactory implements Factory<TimeSource> {
  @Override
  public TimeSource get() {
    return timeSource();
  }

  public static AppModule_TimeSourceFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TimeSource timeSource() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.timeSource());
  }

  private static final class InstanceHolder {
    static final AppModule_TimeSourceFactory INSTANCE = new AppModule_TimeSourceFactory();
  }
}

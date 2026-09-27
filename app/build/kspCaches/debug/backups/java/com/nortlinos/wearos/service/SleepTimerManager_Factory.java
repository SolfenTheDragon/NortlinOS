package com.nortlinos.wearos.service;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
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
public final class SleepTimerManager_Factory implements Factory<SleepTimerManager> {
  private final Provider<ExpiryAlarm> expiryAlarmProvider;

  private final Provider<TimeSource> timeSourceProvider;

  private SleepTimerManager_Factory(Provider<ExpiryAlarm> expiryAlarmProvider,
      Provider<TimeSource> timeSourceProvider) {
    this.expiryAlarmProvider = expiryAlarmProvider;
    this.timeSourceProvider = timeSourceProvider;
  }

  @Override
  public SleepTimerManager get() {
    return newInstance(expiryAlarmProvider.get(), timeSourceProvider.get());
  }

  public static SleepTimerManager_Factory create(Provider<ExpiryAlarm> expiryAlarmProvider,
      Provider<TimeSource> timeSourceProvider) {
    return new SleepTimerManager_Factory(expiryAlarmProvider, timeSourceProvider);
  }

  public static SleepTimerManager newInstance(ExpiryAlarm expiryAlarm, TimeSource timeSource) {
    return new SleepTimerManager(expiryAlarm, timeSource);
  }
}

package com.nortlinos.wearos.di;

import com.nortlinos.wearos.service.ExpiryAlarm;
import com.nortlinos.wearos.service.SystemExpiryAlarm;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ExpiryAlarmFactory implements Factory<ExpiryAlarm> {
  private final Provider<SystemExpiryAlarm> alarmProvider;

  private AppModule_ExpiryAlarmFactory(Provider<SystemExpiryAlarm> alarmProvider) {
    this.alarmProvider = alarmProvider;
  }

  @Override
  public ExpiryAlarm get() {
    return expiryAlarm(alarmProvider.get());
  }

  public static AppModule_ExpiryAlarmFactory create(Provider<SystemExpiryAlarm> alarmProvider) {
    return new AppModule_ExpiryAlarmFactory(alarmProvider);
  }

  public static ExpiryAlarm expiryAlarm(SystemExpiryAlarm alarm) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.expiryAlarm(alarm));
  }
}

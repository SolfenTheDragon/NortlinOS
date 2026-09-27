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
public final class AudioOutputs_Factory implements Factory<AudioOutputs> {
  private final Provider<Context> contextProvider;

  private AudioOutputs_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AudioOutputs get() {
    return newInstance(contextProvider.get());
  }

  public static AudioOutputs_Factory create(Provider<Context> contextProvider) {
    return new AudioOutputs_Factory(contextProvider);
  }

  public static AudioOutputs newInstance(Context context) {
    return new AudioOutputs(context);
  }
}

package com.nortlinos.wearos;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.nortlinos.wearos.data.api.ApiClient;
import com.nortlinos.wearos.data.local.AppDatabase;
import com.nortlinos.wearos.data.local.DownloadDao;
import com.nortlinos.wearos.data.local.LibraryDao;
import com.nortlinos.wearos.data.local.ProgressDao;
import com.nortlinos.wearos.data.local.SessionStore;
import com.nortlinos.wearos.data.local.SettingsStore;
import com.nortlinos.wearos.data.repository.DownloadRepository;
import com.nortlinos.wearos.data.repository.LibraryRepository;
import com.nortlinos.wearos.data.repository.ProgressRepository;
import com.nortlinos.wearos.data.repository.ProgressSyncEngine;
import com.nortlinos.wearos.data.repository.SessionRepository;
import com.nortlinos.wearos.di.AppModule_DatabaseFactory;
import com.nortlinos.wearos.di.AppModule_DownloadDaoFactory;
import com.nortlinos.wearos.di.AppModule_ExpiryAlarmFactory;
import com.nortlinos.wearos.di.AppModule_LibraryDaoFactory;
import com.nortlinos.wearos.di.AppModule_OkHttpClientFactory;
import com.nortlinos.wearos.di.AppModule_ProgressDaoFactory;
import com.nortlinos.wearos.di.AppModule_TimeSourceFactory;
import com.nortlinos.wearos.presentation.MainActivity;
import com.nortlinos.wearos.presentation.MainActivity_MembersInjector;
import com.nortlinos.wearos.presentation.viewmodel.LibraryViewModel;
import com.nortlinos.wearos.presentation.viewmodel.LibraryViewModel_HiltModules;
import com.nortlinos.wearos.presentation.viewmodel.LibraryViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.nortlinos.wearos.presentation.viewmodel.LibraryViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.nortlinos.wearos.presentation.viewmodel.LoginViewModel;
import com.nortlinos.wearos.presentation.viewmodel.LoginViewModel_HiltModules;
import com.nortlinos.wearos.presentation.viewmodel.LoginViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.nortlinos.wearos.presentation.viewmodel.LoginViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.nortlinos.wearos.presentation.viewmodel.PlayerViewModel;
import com.nortlinos.wearos.presentation.viewmodel.PlayerViewModel_HiltModules;
import com.nortlinos.wearos.presentation.viewmodel.PlayerViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.nortlinos.wearos.presentation.viewmodel.PlayerViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.nortlinos.wearos.presentation.viewmodel.SettingsViewModel;
import com.nortlinos.wearos.presentation.viewmodel.SettingsViewModel_HiltModules;
import com.nortlinos.wearos.presentation.viewmodel.SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.nortlinos.wearos.presentation.viewmodel.SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.nortlinos.wearos.service.AudioOutputs;
import com.nortlinos.wearos.service.ConnectivityMonitor;
import com.nortlinos.wearos.service.DownloadQueue;
import com.nortlinos.wearos.service.ExpiryAlarm;
import com.nortlinos.wearos.service.OidcAuthorizers;
import com.nortlinos.wearos.service.OidcRedirectBus;
import com.nortlinos.wearos.service.PlaybackService;
import com.nortlinos.wearos.service.PlaybackService_MembersInjector;
import com.nortlinos.wearos.service.SleepTimerManager;
import com.nortlinos.wearos.service.SystemExpiryAlarm;
import com.nortlinos.wearos.service.TimeSource;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

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
public final class DaggerNortlinOSApp_HiltComponents_SingletonC {
  private DaggerNortlinOSApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public NortlinOSApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements NortlinOSApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public NortlinOSApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements NortlinOSApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public NortlinOSApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements NortlinOSApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public NortlinOSApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements NortlinOSApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public NortlinOSApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements NortlinOSApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public NortlinOSApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements NortlinOSApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public NortlinOSApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements NortlinOSApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public NortlinOSApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends NortlinOSApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends NortlinOSApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    FragmentCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends NortlinOSApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends NortlinOSApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    ActivityCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
      injectMainActivity2(mainActivity);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(ImmutableMap.<String, Boolean>of(LibraryViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, LibraryViewModel_HiltModules.KeyModule.provide(), LoginViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, LoginViewModel_HiltModules.KeyModule.provide(), PlayerViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, PlayerViewModel_HiltModules.KeyModule.provide(), SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SettingsViewModel_HiltModules.KeyModule.provide()));
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    private MainActivity injectMainActivity2(MainActivity instance) {
      MainActivity_MembersInjector.injectOidcRedirects(instance, singletonCImpl.oidcRedirectBusProvider.get());
      return instance;
    }
  }

  private static final class ViewModelCImpl extends NortlinOSApp_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    Provider<LibraryViewModel> libraryViewModelProvider;

    Provider<LoginViewModel> loginViewModelProvider;

    Provider<PlayerViewModel> playerViewModelProvider;

    Provider<SettingsViewModel> settingsViewModelProvider;

    ViewModelCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        SavedStateHandle savedStateHandleParam, ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.libraryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.loginViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.playerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(ImmutableMap.<String, javax.inject.Provider<ViewModel>>of(LibraryViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (libraryViewModelProvider)), LoginViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (loginViewModelProvider)), PlayerViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (playerViewModelProvider)), SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (settingsViewModelProvider))));
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return ImmutableMap.<Class<?>, Object>of();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // com.nortlinos.wearos.presentation.viewmodel.LibraryViewModel
          return (T) new LibraryViewModel(singletonCImpl.libraryRepositoryProvider.get(), singletonCImpl.downloadRepositoryProvider.get(), singletonCImpl.progressRepositoryProvider.get(), singletonCImpl.connectivityMonitorProvider.get(), singletonCImpl.sessionRepositoryProvider.get());

          case 1: // com.nortlinos.wearos.presentation.viewmodel.LoginViewModel
          return (T) new LoginViewModel(singletonCImpl.sessionRepositoryProvider.get(), singletonCImpl.libraryRepositoryProvider.get());

          case 2: // com.nortlinos.wearos.presentation.viewmodel.PlayerViewModel
          return (T) new PlayerViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.apiClientProvider.get(), singletonCImpl.sessionRepositoryProvider.get(), singletonCImpl.libraryRepositoryProvider.get(), singletonCImpl.downloadRepositoryProvider.get(), singletonCImpl.progressRepositoryProvider.get(), singletonCImpl.sleepTimerManagerProvider.get(), singletonCImpl.connectivityMonitorProvider.get(), singletonCImpl.settingsStoreProvider.get(), singletonCImpl.audioOutputsProvider.get());

          case 3: // com.nortlinos.wearos.presentation.viewmodel.SettingsViewModel
          return (T) new SettingsViewModel(singletonCImpl.settingsStoreProvider.get(), singletonCImpl.sessionRepositoryProvider.get(), singletonCImpl.downloadRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends NortlinOSApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends NortlinOSApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    @Override
    public void injectPlaybackService(PlaybackService playbackService) {
      injectPlaybackService2(playbackService);
    }

    private PlaybackService injectPlaybackService2(PlaybackService instance) {
      PlaybackService_MembersInjector.injectProgressRepository(instance, singletonCImpl.progressRepositoryProvider.get());
      PlaybackService_MembersInjector.injectSessionRepository(instance, singletonCImpl.sessionRepositoryProvider.get());
      PlaybackService_MembersInjector.injectConnectivityMonitor(instance, singletonCImpl.connectivityMonitorProvider.get());
      PlaybackService_MembersInjector.injectSleepTimerManager(instance, singletonCImpl.sleepTimerManagerProvider.get());
      PlaybackService_MembersInjector.injectOkHttpClient(instance, singletonCImpl.okHttpClientProvider.get());
      return instance;
    }
  }

  private static final class SingletonCImpl extends NortlinOSApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    Provider<ConnectivityMonitor> connectivityMonitorProvider;

    Provider<OkHttpClient> okHttpClientProvider;

    Provider<ApiClient> apiClientProvider;

    Provider<SessionStore> sessionStoreProvider;

    Provider<OidcRedirectBus> oidcRedirectBusProvider;

    Provider<OidcAuthorizers> oidcAuthorizersProvider;

    Provider<SessionRepository> sessionRepositoryProvider;

    Provider<AppDatabase> databaseProvider;

    Provider<DownloadRepository> downloadRepositoryProvider;

    Provider<DownloadQueue> downloadQueueProvider;

    Provider<LibraryRepository> libraryRepositoryProvider;

    Provider<ProgressSyncEngine> progressSyncEngineProvider;

    Provider<ProgressRepository> progressRepositoryProvider;

    Provider<SystemExpiryAlarm> systemExpiryAlarmProvider;

    Provider<ExpiryAlarm> expiryAlarmProvider;

    Provider<TimeSource> timeSourceProvider;

    Provider<SleepTimerManager> sleepTimerManagerProvider;

    Provider<SettingsStore> settingsStoreProvider;

    Provider<AudioOutputs> audioOutputsProvider;

    SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    ProgressDao progressDao() {
      return AppModule_ProgressDaoFactory.progressDao(databaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.connectivityMonitorProvider = DoubleCheck.provider(new SwitchingProvider<ConnectivityMonitor>(singletonCImpl, 0));
      this.okHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 3));
      this.apiClientProvider = DoubleCheck.provider(new SwitchingProvider<ApiClient>(singletonCImpl, 2));
      this.sessionStoreProvider = DoubleCheck.provider(new SwitchingProvider<SessionStore>(singletonCImpl, 4));
      this.oidcRedirectBusProvider = DoubleCheck.provider(new SwitchingProvider<OidcRedirectBus>(singletonCImpl, 6));
      this.oidcAuthorizersProvider = DoubleCheck.provider(new SwitchingProvider<OidcAuthorizers>(singletonCImpl, 5));
      this.sessionRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<SessionRepository>(singletonCImpl, 1));
      this.databaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 7));
      this.downloadRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<DownloadRepository>(singletonCImpl, 8));
      this.downloadQueueProvider = DoubleCheck.provider(new SwitchingProvider<DownloadQueue>(singletonCImpl, 9));
      this.libraryRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<LibraryRepository>(singletonCImpl, 10));
      this.progressSyncEngineProvider = DoubleCheck.provider(new SwitchingProvider<ProgressSyncEngine>(singletonCImpl, 11));
      this.progressRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ProgressRepository>(singletonCImpl, 12));
      this.systemExpiryAlarmProvider = DoubleCheck.provider(new SwitchingProvider<SystemExpiryAlarm>(singletonCImpl, 15));
      this.expiryAlarmProvider = DoubleCheck.provider(new SwitchingProvider<ExpiryAlarm>(singletonCImpl, 14));
      this.timeSourceProvider = DoubleCheck.provider(new SwitchingProvider<TimeSource>(singletonCImpl, 16));
      this.sleepTimerManagerProvider = DoubleCheck.provider(new SwitchingProvider<SleepTimerManager>(singletonCImpl, 13));
      this.settingsStoreProvider = DoubleCheck.provider(new SwitchingProvider<SettingsStore>(singletonCImpl, 17));
      this.audioOutputsProvider = DoubleCheck.provider(new SwitchingProvider<AudioOutputs>(singletonCImpl, 18));
    }

    @Override
    public void injectNortlinOSApp(NortlinOSApp nortlinOSApp) {
      injectNortlinOSApp2(nortlinOSApp);
    }

    @Override
    public ApiClient apiClient() {
      return apiClientProvider.get();
    }

    @Override
    public OkHttpClient okHttpClient() {
      return okHttpClientProvider.get();
    }

    @Override
    public DownloadDao downloadDao() {
      return AppModule_DownloadDaoFactory.downloadDao(databaseProvider.get());
    }

    @Override
    public LibraryDao libraryDao() {
      return AppModule_LibraryDaoFactory.libraryDao(databaseProvider.get());
    }

    @Override
    public DownloadRepository downloadRepository() {
      return downloadRepositoryProvider.get();
    }

    @Override
    public DownloadQueue downloadQueue() {
      return downloadQueueProvider.get();
    }

    @Override
    public LibraryRepository libraryRepository() {
      return libraryRepositoryProvider.get();
    }

    @Override
    public SessionRepository sessionRepository() {
      return sessionRepositoryProvider.get();
    }

    @Override
    public ProgressSyncEngine syncEngine() {
      return progressSyncEngineProvider.get();
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return ImmutableSet.<Boolean>of();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private NortlinOSApp injectNortlinOSApp2(NortlinOSApp instance) {
      NortlinOSApp_MembersInjector.injectConnectivityMonitor(instance, connectivityMonitorProvider.get());
      NortlinOSApp_MembersInjector.injectSessionRepository(instance, sessionRepositoryProvider.get());
      NortlinOSApp_MembersInjector.injectOkHttpClient(instance, okHttpClientProvider.get());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // com.nortlinos.wearos.service.ConnectivityMonitor
          return (T) new ConnectivityMonitor(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 1: // com.nortlinos.wearos.data.repository.SessionRepository
          return (T) new SessionRepository(singletonCImpl.apiClientProvider.get(), singletonCImpl.sessionStoreProvider.get(), singletonCImpl.oidcAuthorizersProvider.get(), singletonCImpl.connectivityMonitorProvider.get());

          case 2: // com.nortlinos.wearos.data.api.ApiClient
          return (T) new ApiClient(singletonCImpl.okHttpClientProvider.get());

          case 3: // okhttp3.OkHttpClient
          return (T) AppModule_OkHttpClientFactory.okHttpClient();

          case 4: // com.nortlinos.wearos.data.local.SessionStore
          return (T) new SessionStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 5: // com.nortlinos.wearos.service.OidcAuthorizers
          return (T) new OidcAuthorizers(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.oidcRedirectBusProvider.get());

          case 6: // com.nortlinos.wearos.service.OidcRedirectBus
          return (T) new OidcRedirectBus();

          case 7: // com.nortlinos.wearos.data.local.AppDatabase
          return (T) AppModule_DatabaseFactory.database(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 8: // com.nortlinos.wearos.data.repository.DownloadRepository
          return (T) new DownloadRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.downloadDao(), singletonCImpl.libraryDao(), singletonCImpl.sessionRepositoryProvider.get());

          case 9: // com.nortlinos.wearos.service.DownloadQueue
          return (T) new DownloadQueue();

          case 10: // com.nortlinos.wearos.data.repository.LibraryRepository
          return (T) new LibraryRepository(singletonCImpl.apiClientProvider.get(), singletonCImpl.databaseProvider.get(), singletonCImpl.libraryDao(), singletonCImpl.sessionRepositoryProvider.get(), singletonCImpl.connectivityMonitorProvider.get());

          case 11: // com.nortlinos.wearos.data.repository.ProgressSyncEngine
          return (T) new ProgressSyncEngine(singletonCImpl.apiClientProvider.get(), singletonCImpl.progressDao(), singletonCImpl.sessionRepositoryProvider.get());

          case 12: // com.nortlinos.wearos.data.repository.ProgressRepository
          return (T) new ProgressRepository(singletonCImpl.progressDao());

          case 13: // com.nortlinos.wearos.service.SleepTimerManager
          return (T) new SleepTimerManager(singletonCImpl.expiryAlarmProvider.get(), singletonCImpl.timeSourceProvider.get());

          case 14: // com.nortlinos.wearos.service.ExpiryAlarm
          return (T) AppModule_ExpiryAlarmFactory.expiryAlarm(singletonCImpl.systemExpiryAlarmProvider.get());

          case 15: // com.nortlinos.wearos.service.SystemExpiryAlarm
          return (T) new SystemExpiryAlarm(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 16: // com.nortlinos.wearos.service.TimeSource
          return (T) AppModule_TimeSourceFactory.timeSource();

          case 17: // com.nortlinos.wearos.data.local.SettingsStore
          return (T) new SettingsStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 18: // com.nortlinos.wearos.service.AudioOutputs
          return (T) new AudioOutputs(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }
}

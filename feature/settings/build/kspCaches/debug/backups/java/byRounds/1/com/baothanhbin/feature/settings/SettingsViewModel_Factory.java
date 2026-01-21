package com.baothanhbin.feature.settings;

import com.baothanhbin.core.data.repository.AuthRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
    "deprecation"
})
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public SettingsViewModel_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider) {
    return new SettingsViewModel_Factory(authRepositoryProvider);
  }

  public static SettingsViewModel newInstance(AuthRepository authRepository) {
    return new SettingsViewModel(authRepository);
  }
}

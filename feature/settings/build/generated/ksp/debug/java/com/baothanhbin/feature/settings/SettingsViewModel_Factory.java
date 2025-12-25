package com.baothanhbin.feature.settings;

import com.baothanhbin.core.data.repository.AuthRepository;
import com.baothanhbin.core.data.repository.SecuritySettingsRepository;
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
  private final Provider<SecuritySettingsRepository> securitySettingsRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public SettingsViewModel_Factory(
      Provider<SecuritySettingsRepository> securitySettingsRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.securitySettingsRepositoryProvider = securitySettingsRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(securitySettingsRepositoryProvider.get(), authRepositoryProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<SecuritySettingsRepository> securitySettingsRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new SettingsViewModel_Factory(securitySettingsRepositoryProvider, authRepositoryProvider);
  }

  public static SettingsViewModel newInstance(SecuritySettingsRepository securitySettingsRepository,
      AuthRepository authRepository) {
    return new SettingsViewModel(securitySettingsRepository, authRepository);
  }
}

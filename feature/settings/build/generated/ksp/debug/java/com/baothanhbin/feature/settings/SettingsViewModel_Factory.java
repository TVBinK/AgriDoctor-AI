package com.baothanhbin.feature.settings;

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

  public SettingsViewModel_Factory(
      Provider<SecuritySettingsRepository> securitySettingsRepositoryProvider) {
    this.securitySettingsRepositoryProvider = securitySettingsRepositoryProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(securitySettingsRepositoryProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<SecuritySettingsRepository> securitySettingsRepositoryProvider) {
    return new SettingsViewModel_Factory(securitySettingsRepositoryProvider);
  }

  public static SettingsViewModel newInstance(
      SecuritySettingsRepository securitySettingsRepository) {
    return new SettingsViewModel(securitySettingsRepository);
  }
}

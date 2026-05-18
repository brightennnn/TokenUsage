package io.github.brightennnn.mmtokenmonitor.ui.screens.settings;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.brightennnn.mmtokenmonitor.data.repository.FontSizeRepository;
import io.github.brightennnn.mmtokenmonitor.data.repository.ThemeRepository;
import io.github.brightennnn.mmtokenmonitor.domain.repository.TokenRepository;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
    "KotlinInternalInJava"
})
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<TokenRepository> repositoryProvider;

  private final Provider<FontSizeRepository> fontSizeRepositoryProvider;

  private final Provider<ThemeRepository> themeRepositoryProvider;

  private final Provider<Context> contextProvider;

  public SettingsViewModel_Factory(Provider<TokenRepository> repositoryProvider,
      Provider<FontSizeRepository> fontSizeRepositoryProvider,
      Provider<ThemeRepository> themeRepositoryProvider, Provider<Context> contextProvider) {
    this.repositoryProvider = repositoryProvider;
    this.fontSizeRepositoryProvider = fontSizeRepositoryProvider;
    this.themeRepositoryProvider = themeRepositoryProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(repositoryProvider.get(), fontSizeRepositoryProvider.get(), themeRepositoryProvider.get(), contextProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<TokenRepository> repositoryProvider,
      Provider<FontSizeRepository> fontSizeRepositoryProvider,
      Provider<ThemeRepository> themeRepositoryProvider, Provider<Context> contextProvider) {
    return new SettingsViewModel_Factory(repositoryProvider, fontSizeRepositoryProvider, themeRepositoryProvider, contextProvider);
  }

  public static SettingsViewModel newInstance(TokenRepository repository,
      FontSizeRepository fontSizeRepository, ThemeRepository themeRepository, Context context) {
    return new SettingsViewModel(repository, fontSizeRepository, themeRepository, context);
  }
}

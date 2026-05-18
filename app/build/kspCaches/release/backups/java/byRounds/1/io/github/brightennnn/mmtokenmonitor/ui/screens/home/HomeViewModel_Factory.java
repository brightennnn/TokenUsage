package io.github.brightennnn.mmtokenmonitor.ui.screens.home;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.brightennnn.mmtokenmonitor.data.repository.FontSizeRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<TokenRepository> repositoryProvider;

  private final Provider<FontSizeRepository> fontSizeRepositoryProvider;

  private final Provider<Context> contextProvider;

  public HomeViewModel_Factory(Provider<TokenRepository> repositoryProvider,
      Provider<FontSizeRepository> fontSizeRepositoryProvider, Provider<Context> contextProvider) {
    this.repositoryProvider = repositoryProvider;
    this.fontSizeRepositoryProvider = fontSizeRepositoryProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(repositoryProvider.get(), fontSizeRepositoryProvider.get(), contextProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<TokenRepository> repositoryProvider,
      Provider<FontSizeRepository> fontSizeRepositoryProvider, Provider<Context> contextProvider) {
    return new HomeViewModel_Factory(repositoryProvider, fontSizeRepositoryProvider, contextProvider);
  }

  public static HomeViewModel newInstance(TokenRepository repository,
      FontSizeRepository fontSizeRepository, Context context) {
    return new HomeViewModel(repository, fontSizeRepository, context);
  }
}

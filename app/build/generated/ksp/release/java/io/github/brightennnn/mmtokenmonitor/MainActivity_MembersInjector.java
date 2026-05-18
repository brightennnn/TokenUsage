package io.github.brightennnn.mmtokenmonitor;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import io.github.brightennnn.mmtokenmonitor.data.repository.ThemeRepository;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
    "KotlinInternalInJava"
})
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<ThemeRepository> themeRepositoryProvider;

  public MainActivity_MembersInjector(Provider<ThemeRepository> themeRepositoryProvider) {
    this.themeRepositoryProvider = themeRepositoryProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<ThemeRepository> themeRepositoryProvider) {
    return new MainActivity_MembersInjector(themeRepositoryProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectThemeRepository(instance, themeRepositoryProvider.get());
  }

  @InjectedFieldSignature("io.github.brightennnn.mmtokenmonitor.MainActivity.themeRepository")
  public static void injectThemeRepository(MainActivity instance, ThemeRepository themeRepository) {
    instance.themeRepository = themeRepository;
  }
}

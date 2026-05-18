package io.github.brightennnn.mmtokenmonitor.data.repository;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.brightennnn.mmtokenmonitor.data.remote.MiniMaxApi;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
    "KotlinInternalInJava"
})
public final class TokenRepositoryImpl_Factory implements Factory<TokenRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<MiniMaxApi> apiProvider;

  public TokenRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<MiniMaxApi> apiProvider) {
    this.contextProvider = contextProvider;
    this.apiProvider = apiProvider;
  }

  @Override
  public TokenRepositoryImpl get() {
    return newInstance(contextProvider.get(), apiProvider.get());
  }

  public static TokenRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<MiniMaxApi> apiProvider) {
    return new TokenRepositoryImpl_Factory(contextProvider, apiProvider);
  }

  public static TokenRepositoryImpl newInstance(Context context, MiniMaxApi api) {
    return new TokenRepositoryImpl(context, api);
  }
}

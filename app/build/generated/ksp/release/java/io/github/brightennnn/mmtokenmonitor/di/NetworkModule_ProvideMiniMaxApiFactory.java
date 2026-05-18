package io.github.brightennnn.mmtokenmonitor.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.brightennnn.mmtokenmonitor.data.remote.MiniMaxApi;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

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
    "KotlinInternalInJava"
})
public final class NetworkModule_ProvideMiniMaxApiFactory implements Factory<MiniMaxApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideMiniMaxApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public MiniMaxApi get() {
    return provideMiniMaxApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideMiniMaxApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideMiniMaxApiFactory(retrofitProvider);
  }

  public static MiniMaxApi provideMiniMaxApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideMiniMaxApi(retrofit));
  }
}

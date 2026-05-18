package io.github.brightennnn.mmtokenmonitor.data.repository;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
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
public final class FontSizeRepository_Factory implements Factory<FontSizeRepository> {
  private final Provider<Context> contextProvider;

  public FontSizeRepository_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public FontSizeRepository get() {
    return newInstance(contextProvider.get());
  }

  public static FontSizeRepository_Factory create(Provider<Context> contextProvider) {
    return new FontSizeRepository_Factory(contextProvider);
  }

  public static FontSizeRepository newInstance(Context context) {
    return new FontSizeRepository(context);
  }
}

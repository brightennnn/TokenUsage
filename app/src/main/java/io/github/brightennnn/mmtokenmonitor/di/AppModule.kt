package io.github.brightennnn.mmtokenmonitor.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.brightennnn.mmtokenmonitor.data.remote.GlmApi
import io.github.brightennnn.mmtokenmonitor.data.remote.MiniMaxApi
import io.github.brightennnn.mmtokenmonitor.data.repository.GlmRepositoryImpl
import io.github.brightennnn.mmtokenmonitor.data.repository.TokenRepositoryImpl
import io.github.brightennnn.mmtokenmonitor.domain.repository.GlmRepository
import io.github.brightennnn.mmtokenmonitor.domain.repository.TokenRepository
import javax.inject.Singleton
import javax.inject.Qualifier
import dagger.Binds
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** 标识 MiniMax API 的 Retrofit 实例 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MiniMaxRetrofit

/** 标识 GLM API 的 Retrofit 实例（独立 baseUrl，鉴权方式不同） */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GlmRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @MiniMaxRetrofit
    fun provideMiniMaxRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.minimaxi.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @GlmRetrofit
    fun provideGlmRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://open.bigmodel.cn/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideMiniMaxApi(@MiniMaxRetrofit retrofit: Retrofit): MiniMaxApi {
        return retrofit.create(MiniMaxApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGlmApi(@GlmRetrofit retrofit: Retrofit): GlmApi {
        return retrofit.create(GlmApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTokenRepository(impl: TokenRepositoryImpl): TokenRepository

    @Binds
    @Singleton
    abstract fun bindGlmRepository(impl: GlmRepositoryImpl): GlmRepository
}

package com.example.sduiassignment.di

import android.content.Context
import com.example.sduiassignment.data.model.Widget
import com.example.sduiassignment.data.remote.ApiService
import com.example.sduiassignment.data.remote.WidgetDeserializer
import com.example.sduiassignment.ui.common.PerfTrace
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val BASE_URL = "https://api.npoint.io/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(Widget::class.java, WidgetDeserializer())
        .create()

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val perfInterceptor = Interceptor { chain ->
            val response = chain.proceed(chain.request())
            PerfTrace.mark("sdui", "network_response_received")
            response
        }
        val cache = Cache(File(context.cacheDir, "http_cache"), 5L * 1024 * 1024)
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(perfInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
}

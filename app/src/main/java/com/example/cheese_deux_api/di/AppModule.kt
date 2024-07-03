package com.example.cheese_deux_api.di

import android.app.Application
import com.example.cheese_deux_api.cheese_deux.CheeseDeuxApi
import com.example.cheese_deux_api.data.DataStorage
import com.example.cheesechase.gyroscope.GyroSensor
import com.example.cheesechase.gyroscope.MeasurableSensor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideGyroSensor(app: Application): MeasurableSensor {
        return GyroSensor(app)
    }

    @Provides
    @Singleton
    fun provideDataStore(app: Application): DataStorage {
        return DataStorage(app)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(
            60,
            TimeUnit.SECONDS
        )
        .readTimeout(
            60,
            TimeUnit.SECONDS
        )
        .build()


    @Provides
    @Singleton
    fun provideCheeseDeuxApi(
        okHttpClient: OkHttpClient
    ): CheeseDeuxApi = Retrofit.Builder()
        .baseUrl("https://chasedeux.vercel.app")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CheeseDeuxApi::class.java)
}
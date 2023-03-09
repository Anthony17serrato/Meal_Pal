package edu.fullerton.ecs.cpsc411.mealpal.modules

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.fullerton.ecs.cpsc411.mealpal.data.network.EdamamService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object EdamamServiceModule {
    private const val BASE_URL = "https://api.edamam.com/api/recipes/"
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(BASE_URL)
        .build()
    private val retrofitService: EdamamService by lazy {
        retrofit.create(EdamamService::class.java)
    }

    @Singleton
    @Provides
    fun provideEdamamService(): EdamamService {
        return retrofitService
    }
}
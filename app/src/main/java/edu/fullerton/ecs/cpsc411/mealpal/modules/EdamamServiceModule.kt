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

@InstallIn(SingletonComponent::class)
@Module
object EdamamServiceModule {
    private const val BASE_URL = "https://api.edamam.com/api/recipes/"

    @Provides
    fun provideEdamamService(): EdamamService {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(EdamamService::class.java)
    }
}
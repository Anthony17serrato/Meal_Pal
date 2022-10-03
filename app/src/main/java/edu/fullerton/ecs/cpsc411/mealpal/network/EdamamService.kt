package edu.fullerton.ecs.cpsc411.mealpal.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import edu.fullerton.ecs.cpsc411.mealpal.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface EdamamService {
	@Headers("Accept: application/json")
	@GET("v2?type=public")
	suspend fun getRecipes(
		@Query("q") keyword: String,
		@Query("app_id") id: String = BuildConfig.EDAMAM_ID,
		@Query("app_key") key: String = BuildConfig.EDAMAM_KEY,
		@Query("calories") calories: String,
		@Query("_cont") pageId: String? = null
	): RecipesResponse

//	@Headers("Accept: application/json")
//	@GET("/search?q={keyword}&app_id=4c087af2&app_key=f7072010a971bf0c2012cfae655c7f6e&from=0&to=100&calories={minCalories}-{maxCalories}&health={healthLabels}")
//   	suspend fun getRecipes(
//		@Path("keyword") keyword: String,
//		@Path("minCalories") minCalories: String,
//		@Path("maxCalories") maxCalories: String,
//		@Path("healthLabels") healthLabels: String
//	): RecipesResponse

	companion object {
		private const val BASE_URL = "https://api.edamam.com/api/recipes/"

		fun create(): EdamamService {
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
}
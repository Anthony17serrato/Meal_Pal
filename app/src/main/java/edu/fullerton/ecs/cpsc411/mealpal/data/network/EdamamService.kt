package edu.fullerton.ecs.cpsc411.mealpal.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import edu.fullerton.ecs.cpsc411.mealpal.BuildConfig
import edu.fullerton.ecs.cpsc411.mealpal.shared.HealthLabels
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
		@Query("health") healthLabels: List<String>?,
		@Query("diet") dietLabels: List<String>?,
		@Query("_cont") pageId: String? = null
	): RecipesResponse

	@Headers("Accept: application/json")
	@GET("v2?type=public")
	suspend fun getDefaultRecipes(
		@Query("_cont") pageId: String? = null,
		@Query("app_id") id: String = BuildConfig.EDAMAM_ID,
		@Query("app_key") key: String = BuildConfig.EDAMAM_KEY,
		@Query("mealType") mealType: String = "Dinner",
		@Query("dishType") dishType: String = "Main course",
		@Query("diet") diet: String = "balanced"
	): RecipesResponse

//	@Headers("Accept: application/json")
//	@GET("/search?q={keyword}&app_id=4c087af2&app_key=f7072010a971bf0c2012cfae655c7f6e&from=0&to=100&calories={minCalories}-{maxCalories}&health={healthLabels}")
//   	suspend fun getRecipes(
//		@Path("keyword") keyword: String,
//		@Path("minCalories") minCalories: String,
//		@Path("maxCalories") maxCalories: String,
//		@Path("healthLabels") healthLabels: String
//	): RecipesResponse
}
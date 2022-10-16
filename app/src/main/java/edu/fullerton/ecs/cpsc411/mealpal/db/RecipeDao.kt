package edu.fullerton.ecs.cpsc411.mealpal.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.DiscoverQuery
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertRecipe(recipeEntity: RecipeEntity)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertRecipes(recipeEntities: List<RecipeEntity>)

	@Query("SELECT url, saveTime, title, image, calories, diet_labels, health_labels, cautions" +
		   " FROM recipe_table WHERE is_saved = 1  ORDER BY saveTime DESC")
	fun getSavedRecipes() : Flow<List<RecipeListModel>>

//	@Query("SELECT url, saveTime, title, image, calories, diet_labels, health_labels, cautions" +
//		   " FROM recipe_table WHERE query_url == :queryUrl")
//	fun getQueriedRecipes(queryUrl: String) : Flow<List<RecipeListModel>>
	@Query("SELECT * FROM recipe_table WHERE (discover_keyword = :keyword AND" +
			" discover_calMin = :minCalories AND discover_calThresh = :maxCalories AND " +
			"discover_hLabel IS :healthLabels)")
	fun recipesByQuery(
		keyword: String,
		minCalories: String,
		maxCalories: String,
		healthLabels: String?
	) : PagingSource<Int, RecipeEntity>

	@Query("SELECT * FROM recipe_table WHERE url == :recipeUrl")
	fun getRecipe(recipeUrl: String) : Flow<RecipeEntity>

	@Query("DELETE FROM recipe_table")
	suspend fun clearRecipes() : Int
}
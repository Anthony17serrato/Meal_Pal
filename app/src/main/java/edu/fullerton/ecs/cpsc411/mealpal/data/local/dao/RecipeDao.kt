package edu.fullerton.ecs.cpsc411.mealpal.data.local.dao

import androidx.room.*
import edu.fullerton.ecs.cpsc411.mealpal.data.local.entities.RecipeEntity
import edu.fullerton.ecs.cpsc411.mealpal.data.local.entities.RecipeListModel
import edu.fullerton.ecs.cpsc411.mealpal.data.local.entities.RecipeWithIngredients
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
	@Transaction
	@Query("SELECT * FROM recipe_table WHERE url == :recipeUrl")
	fun getRecipeWithIngredients(recipeUrl: String) : Flow<RecipeWithIngredients?>
}
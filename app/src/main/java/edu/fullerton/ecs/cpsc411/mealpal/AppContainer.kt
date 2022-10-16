package edu.fullerton.ecs.cpsc411.mealpal

import android.content.Context
import edu.fullerton.ecs.cpsc411.mealpal.db.MealPalDatabase
import edu.fullerton.ecs.cpsc411.mealpal.network.EdamamService
import edu.fullerton.ecs.cpsc411.mealpal.repos.RecipeRepository
import kotlinx.coroutines.CoroutineScope

class AppContainer(appContext: Context, applicationScope: CoroutineScope) {
	private val database by lazy { MealPalDatabase.getDatabase(appContext) }
	private val recipeRemoteDataSource by lazy { EdamamService.create() }
	val repository by lazy { RecipeRepository(database.recipeDao(), database.remoteKeysDao(), database, recipeRemoteDataSource, applicationScope) }
}
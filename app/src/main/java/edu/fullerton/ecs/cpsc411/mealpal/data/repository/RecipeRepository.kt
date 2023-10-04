package edu.fullerton.ecs.cpsc411.mealpal.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import edu.fullerton.ecs.cpsc411.mealpal.data.local.MealPalDatabase
import edu.fullerton.ecs.cpsc411.mealpal.data.local.dao.IngredientDao
import edu.fullerton.ecs.cpsc411.mealpal.data.local.dao.RecipeDao
import edu.fullerton.ecs.cpsc411.mealpal.data.local.entities.RecipeListModel
import edu.fullerton.ecs.cpsc411.mealpal.data.local.entities.RecipeWithIngredients
import edu.fullerton.ecs.cpsc411.mealpal.modules.ApplicationScope
import edu.fullerton.ecs.cpsc411.mealpal.data.network.EdamamService
import edu.fullerton.ecs.cpsc411.mealpal.data.network.asEntityList
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.DiscoverQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepository @Inject constructor(
	private val mealPalDatabase: MealPalDatabase,
	private val recipeDao: RecipeDao,
	private val ingredientDao: IngredientDao,
	private val recipeRemoteDataSource: EdamamService,
	@ApplicationScope private val externalScope: CoroutineScope
) {
	// Conflated channel to prevent unnecessary IO if user spams the save toggle
	private val _saveToggleChannel = Channel<RecipeWithIngredients> (Channel.CONFLATED)
	val saveToggleChannel: SendChannel<RecipeWithIngredients> = _saveToggleChannel
	// Used to display recipes that have not been saved by the user
	private var nonPersistedRecipe: MutableStateFlow<RecipeWithIngredients?> = MutableStateFlow(null)

	/**
	 * Retrieves all recipes that the user has saved
	 */
	fun getSavedRecipes() : Flow<List<RecipeListModel>> = recipeDao.getSavedRecipes()

	/**
	 * 	A stream of RecipeEntity that matches the provided URL, searches for both non-persisted and
	 * 	persisted RecipeEntities that match the URL.
	 */
	fun getRecipe(url: String) : Flow<RecipeWithIngredients> =
		merge(
			recipeDao.getRecipeWithIngredients(url).filterNotNull(),
			nonPersistedRecipe.filterNotNull().filter { it.recipe.url == url }
		).distinctUntilChanged()

	/**
	 *	Starts a toggle worker that collects updates and persists them in a cancellation safe manner.
	 *	Worker collects toggle updates only while the callers scope is active.
	 */
	fun CoroutineScope.startToggleWorker() = launch {
		for (toggledRecipe in _saveToggleChannel) {
			externalScope.launch {
				nonPersistedRecipe.update { toggledRecipe }
				mealPalDatabase.withTransaction {
					recipeDao.insertRecipe(toggledRecipe.recipe)
					ingredientDao.insertIngredients(toggledRecipe.ingredients)
				}

			}.join()
		}
	}

	/**
	 * 	Returns a flow of PagingData given a query to create a search on
	 * 	@param query the query of recipes to search for
	 */
	fun fetchRecipes(query: DiscoverQuery): Flow<PagingData<RecipeWithIngredients>> {
		Timber.i("Network request")
		return Pager(
			config = PagingConfig(
				pageSize = NETWORK_PAGE_SIZE,
				enablePlaceholders = false
			),
			pagingSourceFactory = { EdamamPagingSource(recipeRemoteDataSource, query) }
		).flow
	}

	/**
	 * 	Cache a recipe entity that the user want's to view details of, this will cache the recipe but
	 * 	not persist it. Subsequent calls to this function will replace the cache with the latest
	 * 	provided recipe.
	 */
	fun viewNonPersistedRecipe(recipe: RecipeWithIngredients) = nonPersistedRecipe.update { recipe }

	/**
	 * Fetches 20 random recipes to be temporarily displayed based on current time of day.
	 * morning = breakfast; afternoon = lunch; evening = dinner. Future revisions will use recipe popularity
	 * data to determine what is shown in the trending section.
	 */
	suspend fun fetchTrendingRecipes(): List<RecipeWithIngredients> {
		return try {
			recipeRemoteDataSource.getTrendingRecipes()
				.hits.asEntityList(pageId = null)
		} catch (exception: IOException) {
			Timber.e(exception)
			emptyList()
		} catch (exception: HttpException) {
			Timber.e(exception)
			emptyList()
		}
	}

	companion object {
		const val NETWORK_PAGE_SIZE = 20
	}
}
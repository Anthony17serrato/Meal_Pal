package edu.fullerton.ecs.cpsc411.mealpal.repos

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import edu.fullerton.ecs.cpsc411.mealpal.db.RecipeEntity
import edu.fullerton.ecs.cpsc411.mealpal.db.RecipeDao
import edu.fullerton.ecs.cpsc411.mealpal.db.RecipeListModel
import edu.fullerton.ecs.cpsc411.mealpal.network.EdamamService
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.DiscoverQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class RecipeRepository(
	private val recipeDao: RecipeDao,
	private val recipeRemoteDataSource: EdamamService,
	private val externalScope: CoroutineScope
) {
	// Conflated channel to prevent unnecessary IO if user spams the save toggle
	private val _saveToggleChannel = Channel<RecipeEntity> (Channel.CONFLATED)
	val saveToggleChannel: SendChannel<RecipeEntity> = _saveToggleChannel
	// Used to display recipes that have not been saved by the user
	private var nonPersistedRecipe: MutableStateFlow<RecipeEntity?> = MutableStateFlow(null)

	/**
	 * Retrieves all recipes that the user has saved
	 */
	fun getSavedRecipes() : Flow<List<RecipeListModel>> = recipeDao.getSavedRecipes()

	/**
	 * 	A stream of RecipeEntity that matches the provided URL, searches for both non-persisted and
	 * 	persisted RecipeEntities that match the URL.
	 */
	fun getRecipe(url: String) : Flow<RecipeEntity> =
		merge(
			recipeDao.getRecipe(url).filterNotNull(),
			nonPersistedRecipe.filterNotNull().filter { it.url == url }
		)

	/**
	 *	Starts a toggle worker that collects updates and persists them in a cancellation safe manner.
	 *	Worker collects toggle updates only while the callers scope is active.
	 */
	fun CoroutineScope.startToggleWorker() = launch {
		for (toggledRecipe in _saveToggleChannel) {
			externalScope.launch {
				nonPersistedRecipe.update { null }
				recipeDao.insertRecipe(toggledRecipe)
			}.join()
		}
	}

	/**
	 * 	Returns a flow of PagingData given a query to create a search on
	 * 	@param query the query of recipes to search for
	 */
	fun fetchRecipes(query: DiscoverQuery): Flow<PagingData<RecipeEntity>> {
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
	 * 	Pass a recipe entity that the user want's to view details of, this will cache the recipe but
	 * 	not persist it. Subsequent calls to this function will replace the cache with the latest
	 * 	provided recipe.
	 */
	fun viewNonPersistedRecipe(recipe: RecipeEntity) = nonPersistedRecipe.update { recipe }

	companion object {
		const val NETWORK_PAGE_SIZE = 20
	}
}
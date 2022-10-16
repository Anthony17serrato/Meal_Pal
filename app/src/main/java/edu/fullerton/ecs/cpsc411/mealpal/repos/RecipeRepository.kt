package edu.fullerton.ecs.cpsc411.mealpal.repos

import androidx.paging.*
import edu.fullerton.ecs.cpsc411.mealpal.db.*
import edu.fullerton.ecs.cpsc411.mealpal.network.EdamamService
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.DiscoverQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber

class RecipeRepository(
	private val recipeDao: RecipeDao,
	private val remoteKeysDao: RemoteKeysDao,
	private val mealPalDatabase: MealPalDatabase,
	private val recipeRemoteDataSource: EdamamService,
	private val externalScope: CoroutineScope
) {
	// conflated channel to prevent unnecessary IO if user spams the save toggle
	private val _saveToggleChannel = Channel<RecipeEntity> (Channel.CONFLATED)
	val saveToggleChannel: SendChannel<RecipeEntity> = _saveToggleChannel

	fun getSavedRecipes() : Flow<List<RecipeListModel>> = recipeDao.getSavedRecipes()

//	fun getQueriedRecipes(queryUrl: String) : Flow<List<RecipeListModel>> =
//		recipeDao.getQueriedRecipes(queryUrl)

	fun getRecipe(url: String) : Flow<RecipeEntity> = recipeDao.getRecipe(url)

	/**
	 * 	Starts a toggle worker that collects updates and persists them in a cancellation safe manner
	 * 	collects toggle updates only while the callers scope is active
	***/
	fun CoroutineScope.startToggleWorker() = launch {
		for (toggledRecipe in _saveToggleChannel) {
			externalScope.launch {
				recipeDao.insertRecipe(toggledRecipe)
			}.join()
		}
	}

	fun fetchRecipes(query: DiscoverQuery): Flow<PagingData<RecipeEntity>> {
		Timber.i("Network request")
		val pagingSourceFactory = {
			recipeDao.recipesByQuery(
				keyword = query.keyword,
				minCalories = query.calMin,
				maxCalories = query.calThresh,
				healthLabels = query.hLabel
			)
		}
		@OptIn(ExperimentalPagingApi::class)
		return Pager(
			config = PagingConfig(
				pageSize = NETWORK_PAGE_SIZE,
				enablePlaceholders = false
			),
			remoteMediator = EdamamRemoteMediator(
				query,
				recipeRemoteDataSource,
				mealPalDatabase,
				recipeDao,
				remoteKeysDao
			),
			pagingSourceFactory = pagingSourceFactory
		).flow
	}

	companion object {
		const val NETWORK_PAGE_SIZE = 20
	}
}
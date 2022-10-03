package edu.fullerton.ecs.cpsc411.mealpal.repos

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import edu.fullerton.ecs.cpsc411.mealpal.db.RecipeEntity
import edu.fullerton.ecs.cpsc411.mealpal.db.RecipeDao
import edu.fullerton.ecs.cpsc411.mealpal.db.RecipeListModel
import edu.fullerton.ecs.cpsc411.mealpal.network.EdamamService
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.DiscoverQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class RecipeRepository(
	private val recipeDao: RecipeDao,
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
		return Pager(
			config = PagingConfig(
				pageSize = NETWORK_PAGE_SIZE,
				enablePlaceholders = false
			),
			pagingSourceFactory = { EdamamPagingSource(recipeRemoteDataSource, query) }
		).flow
//		try {
//			val response = api.getRecipes(query.keyword, calories = "${query.calMin}-${query.calThresh}")
//			Timber.i("Network response ${response.hits.size}")
//			response.hits.asEntityList(url).let {
//				recipeDao.insertRecipes(it)
//			}
//		} catch (t: Throwable) {
//			Timber.e("Caught request Exception $t")
//		}
	}

	companion object {
		const val NETWORK_PAGE_SIZE = 20
	}
}
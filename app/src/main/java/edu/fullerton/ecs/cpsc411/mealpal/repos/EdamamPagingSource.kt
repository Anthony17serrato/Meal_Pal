package edu.fullerton.ecs.cpsc411.mealpal.repos

import androidx.paging.PagingSource
import androidx.paging.PagingState
import edu.fullerton.ecs.cpsc411.mealpal.db.RecipeWithIngredients
import edu.fullerton.ecs.cpsc411.mealpal.network.EdamamService
import edu.fullerton.ecs.cpsc411.mealpal.network.asEntityList
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.DiscoverQuery
import okio.IOException
import retrofit2.HttpException
import timber.log.Timber
import java.net.URLDecoder

class EdamamPagingSource(
	private val service: EdamamService,
	private val query: DiscoverQuery
) : PagingSource<String, RecipeWithIngredients>() {
	override suspend fun load(params: LoadParams<String>): LoadResult<String, RecipeWithIngredients> {
		val currentKey = params.key
		return try {
			val response = service.getRecipes(
				keyword = query.keyword,
				calories = "${query.calMin}-${query.calThresh}",
				pageId = currentKey
			)
			Timber.i("Network response ${response.hits.size}")
			val nextKey = response.links.nextPage?.url?.let { parseNextPageFromUrl(it) }
			Timber.i("Next Key is: $nextKey")
			// TODO: this check is only too ensure there is enough recipes for trending, remove when trending is separated
			val recipeEntities = if (response.hits.size > 6) response.hits.asEntityList(currentKey) else listOf()

			LoadResult.Page(
				data = recipeEntities,
				prevKey = null, // Only paging forward.
				nextKey = nextKey
			)
		}  catch (exception: IOException) {
			Timber.e(exception)
			return LoadResult.Error(exception)
		} catch (exception: HttpException) {
			Timber.e(exception)
			return LoadResult.Error(exception)
		}
	}

	override fun getRefreshKey(state: PagingState<String, RecipeWithIngredients>): String? {
		return state.anchorPosition?.let { anchorPosition ->
			state.closestItemToPosition(anchorPosition)?.recipe?.pageId
		}
	}

	private fun parseNextPageFromUrl(url: String): String {
		return URLDecoder.decode(
			url.substringAfter("&_cont=","").substringBefore("&", ""),
			"utf-8"
		)
	}
}
package edu.fullerton.ecs.cpsc411.mealpal.repos

import androidx.paging.*
import androidx.room.withTransaction
import edu.fullerton.ecs.cpsc411.mealpal.db.*
import edu.fullerton.ecs.cpsc411.mealpal.network.EdamamService
import edu.fullerton.ecs.cpsc411.mealpal.network.asEntityList
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.DiscoverQuery
import okio.IOException
import retrofit2.HttpException
import timber.log.Timber
import java.net.URLDecoder

// Edamam api does not expect a starting page key so just use placeholder here
private const val EDAMAM_STARTING_PAGE_KEY = "placeholder"

@OptIn(ExperimentalPagingApi::class)
class EdamamRemoteMediator(
    private val query: DiscoverQuery,
    private val service: EdamamService,
    private val mealPalDatabase: MealPalDatabase,
    private val recipeDao: RecipeDao,
    private val remoteKeysDao: RemoteKeysDao
) : RemoteMediator<Int, RecipeEntity>(){
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, RecipeEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.currKey ?: EDAMAM_STARTING_PAGE_KEY
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                // If remoteKeys is null, that means the refresh result is not in the database yet.
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                // If remoteKeys is null, that means the refresh result is not in the database yet.
                // We can return Success with endOfPaginationReached = false because Paging
                // will call this method again if RemoteKeys becomes non-null.
                // If remoteKeys is NOT NULL but its nextKey is null, that means we've reached
                // the end of pagination for append.
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }
        try {
            Timber.i("Querying page: $page")
            val response = service.getRecipes(
                keyword = query.keyword,
                calories = "${query.calMin}-${query.calThresh}",
                // first page does not require a key
                pageId = if (page == EDAMAM_STARTING_PAGE_KEY) null else page
            )
            Timber.i("Network response next page:${response.links.nextPage?.url}")
            // TODO: this check is only too ensure there is enough recipes for trending, remove when trending is separated
            val recipeEntities = if (response.hits.size > 6) response.hits.asEntityList(query) else listOf()
            val endOfPaginationReached = recipeEntities.isEmpty()
            mealPalDatabase.withTransaction {
                // clear all tables in the database
                if (loadType == LoadType.REFRESH) {
                    remoteKeysDao.clearRemoteKeys()
                    recipeDao.clearRecipes()
                }
                val prevKey = if (page == EDAMAM_STARTING_PAGE_KEY) null else remoteKeysDao.getPreviousKeyFromCurrent(page)
                val nextKey = if (endOfPaginationReached) null else response.links.nextPage?.url?.let { parseNextPageFromUrl(it) }

                val keys = recipeEntities.map {
                    RemoteKeys(recipeId = it.url, currKey = page, prevKey = prevKey, nextKey = nextKey)
                }
                Timber.i("prev $prevKey, next $nextKey, curr $page")
                if (page == nextKey) {
                    Timber.e("current key and next key are equal")
                } else {
                    Timber.i("current key and next key are unique")
                }
                remoteKeysDao.insertAll(keys)
                recipeDao.insertRecipes(recipeEntities)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            Timber.e(exception)
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            Timber.e(exception)
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, RecipeEntity>): RemoteKeys? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { recipeEntity ->
                // Get the remote keys of the last item retrieved
                remoteKeysDao.remoteKeysRecipeId(recipeEntity.url)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, RecipeEntity>): RemoteKeys? {
        // Get the first page that was retrieved, that contained items.
        // From that first page, get the first item
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { recipeEntity ->
                // Get the remote keys of the first items retrieved
                remoteKeysDao.remoteKeysRecipeId(recipeEntity.url)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, RecipeEntity>): RemoteKeys? {
        // The paging library is trying to load data after the anchor position
        // Get the item closest to the anchor position
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.url?.let { mealId ->
                remoteKeysDao.remoteKeysRecipeId(mealId)
            }
        }
    }

    private fun parseNextPageFromUrl(url: String): String {
        return URLDecoder.decode(
            url.substringAfter("&_cont=","").substringBefore("&", ""),
            "utf-8"
        )
    }
}
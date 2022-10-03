package edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import androidx.savedstate.SavedStateRegistryOwner
import edu.fullerton.ecs.cpsc411.mealpal.db.RecipeListModel
import edu.fullerton.ecs.cpsc411.mealpal.db.asRecipeListModel
import edu.fullerton.ecs.cpsc411.mealpal.repos.RecipeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DiscoverViewModel(
	private val recipeRepo: RecipeRepository,
	private val savedStateHandle: SavedStateHandle
) : ViewModel() {
	val discoverUiState: StateFlow<DiscoverUiState>
	val pagingDataFlow: Flow<PagingData<RecipeListModel>>
	val accept: (UiAction) -> Unit

	init {
		val initialQuery: DiscoverQuery = savedStateHandle[LAST_SEARCH_QUERY] ?: DEFAULT_QUERY
		val lastQueryScrolled: DiscoverQuery = savedStateHandle[LAST_QUERY_SCROLLED] ?: DEFAULT_QUERY
		val actionStateFlow = MutableSharedFlow<UiAction>()
		val searches = actionStateFlow
			.filterIsInstance<UiAction.Search>()
			.distinctUntilChanged()
			.onStart { emit(UiAction.Search(query = initialQuery)) }
		val queriesScrolled = actionStateFlow
			.filterIsInstance<UiAction.Scroll>()
			.distinctUntilChanged()
			.shareIn(
				scope = viewModelScope,
				started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
				replay = 1
			)
			.onStart { emit(UiAction.Scroll(currentQuery = lastQueryScrolled)) }

		pagingDataFlow = searches
			.flatMapLatest { searchRepo(queryString = it.query) }
			.cachedIn(viewModelScope)

		discoverUiState = combine(
			searches,
			queriesScrolled,
			::Pair
		).map { (search, scroll) ->
			DiscoverUiState(
				query = search.query,
				lastQueryScrolled = scroll.currentQuery,
				hasNotScrolledForCurrentSearch = search.query != scroll.currentQuery
			)
		}.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
			initialValue = DiscoverUiState()
		)

		accept = { action ->
			viewModelScope.launch { actionStateFlow.emit(action) }
		}
	}

	private fun searchRepo(queryString: DiscoverQuery) : Flow<PagingData<RecipeListModel>> =
		recipeRepo.fetchRecipes(queryString)
			.map { pagingData ->
				pagingData.map { it.asRecipeListModel() }
			}

	override fun onCleared() {
		savedStateHandle[LAST_SEARCH_QUERY] = discoverUiState.value.query
		savedStateHandle[LAST_QUERY_SCROLLED] = discoverUiState.value.lastQueryScrolled
		super.onCleared()
	}
}

sealed class UiAction {
	data class Search(val query: DiscoverQuery) : UiAction()
	data class Scroll(val currentQuery: DiscoverQuery) : UiAction()
}

data class DiscoverUiState(
	val query: DiscoverQuery = DiscoverQuery(),
	val lastQueryScrolled: DiscoverQuery = DiscoverQuery(),
	val hasNotScrolledForCurrentSearch: Boolean = false
)

class DiscoverViewModelFactory(
	owner: SavedStateRegistryOwner,
	private val recipeRepo: RecipeRepository
) : AbstractSavedStateViewModelFactory(owner, null) {
	override fun <T : ViewModel> create(
		key: String,
		modelClass: Class<T>,
		handle: SavedStateHandle
	): T {
		if (modelClass.isAssignableFrom(DiscoverViewModel::class.java)) {
			@Suppress("UNCHECKED_CAST")
			return DiscoverViewModel(recipeRepo, handle) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}
}

private const val LAST_SEARCH_QUERY: String = "last_search_query"
private val DEFAULT_QUERY = DiscoverQuery()
private const val LAST_QUERY_SCROLLED: String = "last_query_scrolled"
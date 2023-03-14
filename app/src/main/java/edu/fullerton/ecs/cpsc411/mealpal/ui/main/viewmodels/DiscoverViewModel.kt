package edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.fullerton.ecs.cpsc411.mealpal.data.local.entities.RecipeListModel
import edu.fullerton.ecs.cpsc411.mealpal.data.local.entities.asRecipeListModel
import edu.fullerton.ecs.cpsc411.mealpal.data.repository.PreferencesRepository
import edu.fullerton.ecs.cpsc411.mealpal.data.repository.RecipeRepository
import edu.fullerton.ecs.cpsc411.mealpal.shared.DietLabels
import edu.fullerton.ecs.cpsc411.mealpal.shared.HealthLabels
import edu.fullerton.ecs.cpsc411.mealpal.usecase.RecipeInteraction
import edu.fullerton.ecs.cpsc411.mealpal.usecase.RecipeInteractionsUseCase
import edu.fullerton.ecs.cpsc411.mealpal.utils.DEFAULT_QUERY
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DiscoverViewModel @Inject constructor(
	private val recipeRepo: RecipeRepository,
	private val recipeInteractionsUseCase: RecipeInteractionsUseCase,
	private val savedStateHandle: SavedStateHandle,
	private val preferencesRepository: PreferencesRepository
) : ViewModel() {
	private val _discoverSearchState = MutableStateFlow(DiscoverSearchState())
	val discoverSearchState = _discoverSearchState.asStateFlow()
	val discoverUiState: StateFlow<DiscoverUiState>
	val pagingDataFlow: Flow<PagingData<DiscoverItemUiState>>
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

	private fun searchRepo(queryString: DiscoverQuery) : Flow<PagingData<DiscoverItemUiState>> =
		recipeRepo.fetchRecipes(queryString)
			.cachedIn(viewModelScope)
			.combine(preferencesRepository.userMpPrefsCache) { pagingData, _ ->
				// we don't need to do anything with user prefs here we just want to refresh when they change
				pagingData
			}
			.map { pagingData ->
				pagingData.map { recipeWithIngredients ->
					val recipeListModel = recipeWithIngredients.recipe.asRecipeListModel()
					DiscoverItemUiState(
						recipeListModel = recipeListModel,
						recipeInteractions = recipeInteractionsUseCase.getInteractions(
							dietLabels = recipeListModel.dietLabels,
							healthLabels = recipeListModel.healthLabels
						),
						onSelect = { recipeRepo.viewNonPersistedRecipe(recipeWithIngredients) }
					)
				}
			}

	override fun onCleared() {
		savedStateHandle[LAST_SEARCH_QUERY] = discoverUiState.value.query
		savedStateHandle[LAST_QUERY_SCROLLED] = discoverUiState.value.lastQueryScrolled
		super.onCleared()
	}

	fun indexLocalizedHealthLabels(localizedString: String, healthLabel: HealthLabels) {
		_discoverSearchState.update {
			it.copy(healthLabelsIndex = it.healthLabelsIndex + listOf(Pair(localizedString, healthLabel)))
		}
	}

	fun indexLocalizedDietLabels(localizedString: String, dietLabel: DietLabels) {
		_discoverSearchState.update {
			it.copy(dietLabelsIndex = it.dietLabelsIndex + listOf(Pair(localizedString, dietLabel)))
		}
	}

	fun updateCurrentKeyword(keyword: CharSequence?) {
		val sanitizedKeyword = keyword?.trim() ?: return
		_discoverSearchState.update {
			it.copy(currentKeyword = sanitizedKeyword.toString())
		}
	}

	fun setMaxCalories(value: Float) {
		_discoverSearchState.update {
			it.copy(maxCalories = value.toInt().toString())
		}
	}

	fun setMinCalories(value: Float) {
		_discoverSearchState.update {
			it.copy(minCalories = value.toInt().toString())
		}
	}

	fun executeSearch() {
		_discoverSearchState.value.apply {
			if (currentKeyword.isNotEmpty()) {
				accept(
					UiAction.Search(
						query = DiscoverQuery(
							keyword = currentKeyword,
							calMin = minCalories,
							calThresh = maxCalories,
							healthLabels = selectedHealthLabels.map { it.apiValue },
							dietLabels = selectedDietLabels.map { it.apiValue }
						).apply { Timber.i("$this") }
					)
				)
			}
		}
	}

	fun setHealthLabel(label: CharSequence?) {
		val selectedHealthLabel = _discoverSearchState.value.healthLabelsIndex
			.find { it.first == label?.trim().toString() }?.second ?: return
		_discoverSearchState.update { it.copy(selectedHealthLabels = listOf(selectedHealthLabel)) }
	}

	fun setDietLabel(label: CharSequence?) {
		val selectedDietLabel = _discoverSearchState.value.dietLabelsIndex
			.find { it.first == label?.trim().toString() }?.second ?:return
		_discoverSearchState.update { it.copy(selectedDietLabels = listOf(selectedDietLabel)) }
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

data class DiscoverSearchState(
	val healthLabelsIndex: List<Pair<String, HealthLabels>> = emptyList(),
	val dietLabelsIndex: List<Pair<String, DietLabels>> = emptyList(),
	val currentKeyword: String = "",
	val minCalories: String = "200",
	val maxCalories: String = "1800",
	val selectedHealthLabels: List<HealthLabels> = emptyList(),
	val selectedDietLabels: List<DietLabels> = emptyList()
)

data class DiscoverItemUiState(
	val recipeListModel: RecipeListModel,
	val recipeInteractions: RecipeInteraction,
	val onSelect: () -> Unit
)

private const val LAST_SEARCH_QUERY: String = "last_search_query"
private const val LAST_QUERY_SCROLLED: String = "last_query_scrolled"
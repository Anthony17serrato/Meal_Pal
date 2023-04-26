package edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels

import androidx.annotation.DrawableRes
import androidx.compose.runtime.*
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.fullerton.ecs.cpsc411.mealpal.R
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
	private val _quickPicksState = MutableStateFlow(QuickPicksState())
	val quickPicksState = _quickPicksState.asStateFlow()
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
				_quickPicksState.update { it.copy(selectedPick = null) }
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

	fun setUseHealthProfile(state: Boolean) {
		viewModelScope.launch {
			_discoverSearchState.update {
				it.copy(
					shouldUseHealthProfile = state,
					selectedHealthLabels = if (state) {
						preferencesRepository.userMpPrefsCache.first().userHealthLabels ?: emptyList()
					} else {
						emptyList()
					},
					selectedDietLabels = if (state) {
						preferencesRepository.userMpPrefsCache.first().userDietLabels ?: emptyList()
					} else {
						emptyList()
					}
				)
			}
		}
	}

	fun setSelectedQuickPick(quickPicks: QuickPicks) {
		if (quickPicks == _quickPicksState.value.selectedPick) {
			_quickPicksState.update { it.copy(selectedPick = null) }
			accept(
				UiAction.Search(DiscoverQuery())
			)
		} else {
			_quickPicksState.update { it.copy(selectedPick = quickPicks) }
			accept(
				UiAction.Search(DiscoverQuery(keyword = quickPicks.searchableName))
			)
		}
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
	val selectedDietLabels: List<DietLabels> = emptyList(),
	val shouldUseHealthProfile: Boolean = false
)

data class DiscoverItemUiState(
	val recipeListModel: RecipeListModel,
	val recipeInteractions: RecipeInteraction,
	val onSelect: () -> Unit
)

// TODO Extract string resources
enum class QuickPicks(val cuisineName: String, val searchableName: String, @DrawableRes val icon: Int) {
	Sushi("Sushi", "Sushi", R.drawable.sushi_quick_pick),
	Thai("Thai", "Thai food", R.drawable.thai_food_quick_pick),
	Healthy("Healthy", "Healthy food", R.drawable.healthy_quick_pick),
	Pizza("Pizza", "Pizza", R.drawable.pizza_quick_pick),
	Desserts("Desserts", "Desserts", R.drawable.desserts_quick_pick),
	Mexican("Mexican", "Mexican food", R.drawable.mexican_quick_pick),
	Chicken("Chicken", "Chicken", R.drawable.chicken_quick_pick),
	Italian("Italian", "Italian food", R.drawable.italian_quick_pick),
	Steak("Steak", "Steak", R.drawable.steak_quick_pick),
	Ramen("Ramen", "Ramen", R.drawable.ramen_quick_pick),
	Asian("Asian", "Asian food", R.drawable.asian_quick_pick),
	Chinese("Chinese", "Chinese food", R.drawable.chinese_quick_pick),
	Pho("Pho", "Pho", R.drawable.pho_quick_pick),
	Coffee("Coffee", "Coffee", R.drawable.coffee_quick_pick),
	Soup("Soup", "Soup", R.drawable.soup_quick_pick),
	Sandwiches("Sandwiches", "Sandwiches", R.drawable.sandwich_quick_pick)
}

data class QuickPicksState(
	val selectedPick: QuickPicks? = null
)

private const val LAST_SEARCH_QUERY: String = "last_search_query"
private const val LAST_QUERY_SCROLLED: String = "last_query_scrolled"
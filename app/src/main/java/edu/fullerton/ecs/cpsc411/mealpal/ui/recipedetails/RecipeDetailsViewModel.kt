package edu.fullerton.ecs.cpsc411.mealpal.ui.recipedetails

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import edu.fullerton.ecs.cpsc411.mealpal.db.RecipeEntity
import edu.fullerton.ecs.cpsc411.mealpal.repos.RecipeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecipeDetailsViewModel(
	private val recipeRepo: RecipeRepository,
	private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {
	private val _recipeUiState = MutableStateFlow(RecipeModel())
	val recipeUiState = _recipeUiState.asStateFlow()

	init {
		recipeRepo.run {
			viewModelScope.startToggleWorker()
		}
	}

	fun onSaveToggle() {
		_recipeUiState.value.recipeEntity?.let { recipe ->
			recipeRepo.saveToggleChannel.trySend(recipe.copy(isSaved = !recipe.isSaved))
		}
	}

	fun setRecipe(recipeUrl: String) = viewModelScope.launch {
		recipeRepo.getRecipe(recipeUrl).collect { recipe ->
			_recipeUiState.update { currentRecipeModel ->
				currentRecipeModel.copy(recipeEntity = recipe)
			}
		}
	}

	// Generate palette
	fun createPalette(bitmap: Bitmap) = viewModelScope.launch(defaultDispatcher) {
		val palette = Palette.from(bitmap).generate()
		_recipeUiState.update { currentRecipeModel ->
			currentRecipeModel.copy(palette = palette)
		}
	}

}

data class RecipeModel(
	val recipeEntity: RecipeEntity? = null,
	val palette: Palette? = null
)

class RecipeDetailsViewModelFactory(private val recipeRepo: RecipeRepository) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(RecipeDetailsViewModel::class.java)) {
			@Suppress("UNCHECKED_CAST")
			return RecipeDetailsViewModel(recipeRepo) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}
}
package edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.fullerton.ecs.cpsc411.mealpal.data.local.entities.RecipeListModel
import edu.fullerton.ecs.cpsc411.mealpal.data.repository.RecipeRepository
import edu.fullerton.ecs.cpsc411.mealpal.usecase.RecipeInteraction
import edu.fullerton.ecs.cpsc411.mealpal.usecase.RecipeInteractionsUseCase
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class SavedMealsViewModel @Inject constructor(
	private val recipeRepo: RecipeRepository,
	private val recipeInteractionsUseCase: RecipeInteractionsUseCase
) : ViewModel() {
	val savedRecipes =
		recipeRepo.getSavedRecipes().map { recipeList ->
			recipeList.map { recipe ->
				RecipeWithInteractions(
					recipe = recipe,
					interactions = recipeInteractionsUseCase.getInteractions(
						dietLabels = recipe.dietLabels,
						healthLabels = recipe.healthLabels
					)
				)
			}
		}
}

data class RecipeWithInteractions(
	val recipe: RecipeListModel,
	val interactions: RecipeInteraction
)
package edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.fullerton.ecs.cpsc411.mealpal.repos.RecipeRepository
import kotlinx.coroutines.flow.map

class SavedMealsViewModel(recipeRepo: RecipeRepository) : ViewModel() {
	val savedRecipes = recipeRepo.getSavedRecipes()
}

class SavedMealsViewModelFactory(private val recipeRepo: RecipeRepository) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(SavedMealsViewModel::class.java)) {
			@Suppress("UNCHECKED_CAST")
			return SavedMealsViewModel(recipeRepo) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}
}
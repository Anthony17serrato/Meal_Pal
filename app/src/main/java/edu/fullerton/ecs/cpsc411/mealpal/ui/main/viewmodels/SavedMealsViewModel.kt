package edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.fullerton.ecs.cpsc411.mealpal.data.repository.RecipeRepository
import javax.inject.Inject

@HiltViewModel
class SavedMealsViewModel @Inject constructor(private val recipeRepo: RecipeRepository) : ViewModel() {
	val savedRecipes = recipeRepo.getSavedRecipes()
}
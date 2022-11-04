package edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.fullerton.ecs.cpsc411.mealpal.repos.RecipeRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class SavedMealsViewModel @Inject constructor(private val recipeRepo: RecipeRepository) : ViewModel() {
	val savedRecipes = recipeRepo.getSavedRecipes()
}
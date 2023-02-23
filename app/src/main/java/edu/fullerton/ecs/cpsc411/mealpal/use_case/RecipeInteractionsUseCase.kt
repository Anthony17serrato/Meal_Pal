package edu.fullerton.ecs.cpsc411.mealpal.use_case

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dagger.hilt.android.scopes.ViewModelScoped
import edu.fullerton.ecs.cpsc411.mealpal.R
import edu.fullerton.ecs.cpsc411.mealpal.data.local.entities.RecipeEntity
import edu.fullerton.ecs.cpsc411.mealpal.data.repository.PreferencesRepository
import javax.inject.Inject

@ViewModelScoped
class RecipeInteractionsUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    suspend fun getInteractions(recipe: RecipeEntity): RecipeInteraction {
        val userPrefs = preferencesRepository.getDietPreferences().map { it.apiValue } +
                preferencesRepository.getHealthPreferences().map { it.apiValue }
        val recipeLabels = (recipe.dietLabels + recipe.healthLabels).map { it.lowercase() }
        val conflicts = userPrefs.subtract(recipeLabels.toSet())
        return if (conflicts.isEmpty()) RecipeInteraction.Approved
        else RecipeInteraction.Warning(
            conflicts.joinToString { conflict ->
                conflict.replaceFirstChar { it.uppercase() }
            }
        )
    }
}

sealed class RecipeInteraction(
    @StringRes val interactionLabel: Int,
    @StringRes val interactionDescription: Int,
    @DrawableRes val interactionIcon: Int,
    @ColorRes val cardColor: Int
) {
    data class Warning(val conflicts: String) : RecipeInteraction(
        R.string.warning,
        R.string.warning_description,
        R.drawable.round_warning_24,
        R.color.card_warning
    )

    object Approved : RecipeInteraction(
        R.string.approved_recipe,
        R.string.approved_description,
        R.drawable.round_verified_24,
        R.color.card_ok
    )
}
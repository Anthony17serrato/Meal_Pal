package edu.fullerton.ecs.cpsc411.mealpal.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation
import edu.fullerton.ecs.cpsc411.mealpal.data.local.entities.IngredientEntity

data class RecipeWithIngredients(
    @Embedded val recipe: RecipeEntity,
    @Relation(
        parentColumn = "url",
        entityColumn = "entityId"
    )
    val ingredients: List<IngredientEntity>
)

package edu.fullerton.ecs.cpsc411.mealpal.db

import androidx.room.Embedded
import androidx.room.Relation

data class RecipeWithIngredients(
    @Embedded val recipe: RecipeEntity,
    @Relation(
        parentColumn = "url",
        entityColumn = "entityId"
    )
    val ingredients: List<IngredientEntity>
)

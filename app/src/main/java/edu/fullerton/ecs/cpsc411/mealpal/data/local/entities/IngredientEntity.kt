package edu.fullerton.ecs.cpsc411.mealpal.data.local.entities

import androidx.room.Entity

@Entity(tableName = "ingredient_table", primaryKeys = ["entityId", "text"])
data class IngredientEntity(
    val entityId: String,
    val text: String,
    val imageUrl: String?
)
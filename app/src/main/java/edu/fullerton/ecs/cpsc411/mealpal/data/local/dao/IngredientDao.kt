package edu.fullerton.ecs.cpsc411.mealpal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import edu.fullerton.ecs.cpsc411.mealpal.data.local.entities.IngredientEntity

@Dao
interface IngredientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredientEntity: List<IngredientEntity>)
}
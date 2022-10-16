package edu.fullerton.ecs.cpsc411.mealpal.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeys(
    @PrimaryKey
    val recipeId: String,
    val currKey: String?,
    val prevKey: String?,
    val nextKey: String?
)

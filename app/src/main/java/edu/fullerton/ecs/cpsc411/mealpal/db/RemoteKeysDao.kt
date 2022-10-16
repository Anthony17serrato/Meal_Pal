package edu.fullerton.ecs.cpsc411.mealpal.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<RemoteKeys>)

    @Query("SELECT * FROM remote_keys WHERE recipeId = :recipeId")
    suspend fun remoteKeysRecipeId(recipeId: String): RemoteKeys?

    @Query("SELECT currKey FROM remote_keys WHERE nextKey = :currentKey")
    suspend fun getPreviousKeyFromCurrent(currentKey: String) : String?

    @Query("DELETE FROM remote_keys")
    suspend fun clearRemoteKeys()
}
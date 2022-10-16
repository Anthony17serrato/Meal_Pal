package edu.fullerton.ecs.cpsc411.mealpal.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// Annotates class to be a Room Database with a table (entity) of the Word class
@Database(
	entities = [RecipeEntity::class, RemoteKeys::class],
	version = 1,
	exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MealPalDatabase : RoomDatabase() {

	abstract fun recipeDao(): RecipeDao
	abstract fun remoteKeysDao(): RemoteKeysDao

	companion object {
		// Singleton prevents multiple instances of database opening at the
		// same time.
		@Volatile
		private var INSTANCE: MealPalDatabase? = null

		fun getDatabase(context: Context): MealPalDatabase {
			// if the INSTANCE is not null, then return it,
			// if it is, then create the database
			return INSTANCE ?: synchronized(this) {
				val instance = Room.databaseBuilder(
					context.applicationContext,
					MealPalDatabase::class.java,
					"mp_database"
				).build()
				INSTANCE = instance
				// return instance
				instance
			}
		}
	}
}

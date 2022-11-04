package edu.fullerton.ecs.cpsc411.mealpal.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import edu.fullerton.ecs.cpsc411.mealpal.db.MealPalDatabase
import edu.fullerton.ecs.cpsc411.mealpal.db.RecipeDao
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
    @Provides
    fun provideRecipeDao(appDatabase: MealPalDatabase): RecipeDao {
        return appDatabase.recipeDao()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): MealPalDatabase {
        return MealPalDatabase.getDatabase(appContext)
    }
}
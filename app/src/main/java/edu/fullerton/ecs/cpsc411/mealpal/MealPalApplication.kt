package edu.fullerton.ecs.cpsc411.mealpal

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import edu.fullerton.ecs.cpsc411.mealpal.db.MealPalDatabase
import edu.fullerton.ecs.cpsc411.mealpal.repos.RecipeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber

@HiltAndroidApp
class MealPalApplication : Application() {

	override fun onCreate() {
		super.onCreate()
		Timber.plant(Timber.DebugTree())
	}
}
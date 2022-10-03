package edu.fullerton.ecs.cpsc411.mealpal

import android.app.Application
import edu.fullerton.ecs.cpsc411.mealpal.db.MealPalDatabase
import edu.fullerton.ecs.cpsc411.mealpal.repos.RecipeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber

class MealPalApplication : Application() {
	// No need to cancel this scope as it'll be torn down with the process
	val applicationScope = CoroutineScope(SupervisorJob())
	// Instance of AppContainer that will be used by all the Activities of the app
	val appContainer = AppContainer(this, applicationScope)

	override fun onCreate() {
		super.onCreate()
		Timber.plant(Timber.DebugTree())
	}
}
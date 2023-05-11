package edu.fullerton.ecs.cpsc411.mealpal

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MealPalApplication : Application() {

	override fun onCreate() {
		super.onCreate()
		// Initialize Timber debug logging
		Timber.plant(Timber.DebugTree())

		// Initialize firebase app + app check for enforcing all API calls come from a signed apk
		FirebaseApp.initializeApp(/*context=*/this)
		val firebaseAppCheck = FirebaseAppCheck.getInstance()
		firebaseAppCheck.installAppCheckProviderFactory(
			if (BuildConfig.DEBUG) {
				DebugAppCheckProviderFactory.getInstance()
			} else {
				PlayIntegrityAppCheckProviderFactory.getInstance()
			}
		)
	}
}
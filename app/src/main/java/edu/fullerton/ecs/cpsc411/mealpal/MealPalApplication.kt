package edu.fullerton.ecs.cpsc411.mealpal

import android.app.Application
import android.content.Intent
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import kotlin.system.exitProcess


@HiltAndroidApp
class MealPalApplication : Application() {

	override fun onCreate() {
		super.onCreate()
		// Initialize Timber debug logging
		Timber.plant(Timber.DebugTree())

		// Setup handler for uncaught exceptions.
		// Setup handler for uncaught exceptions.
		Thread.setDefaultUncaughtExceptionHandler { thread, e ->
			handleUncaughtException(
				thread,
				e
			)
		}

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

	private fun handleUncaughtException(thread: Thread?, e: Throwable) {
		Timber.e(e)
		exitProcess(1) // kill off the crashed app
	}
}
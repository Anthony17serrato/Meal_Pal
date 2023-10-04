package edu.fullerton.ecs.cpsc411.mealpal.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope

@InstallIn(SingletonComponent::class)
@Module
object CoroutineScopesModule {

    @Singleton
    @ApplicationScope
    @Provides
    fun providesCoroutineScope(
        @MainDispatcher mainDispatcher: CoroutineDispatcher
    ): CoroutineScope {
        val handler = CoroutineExceptionHandler { _, exception ->
            Timber.e(exception, "CoroutineExceptionHandler got ")
        }
        return CoroutineScope(SupervisorJob() + mainDispatcher + handler)
    }
}
package edu.fullerton.ecs.cpsc411.mealpal.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import edu.fullerton.ecs.cpsc411.mealpal.modules.ApplicationScope
import edu.fullerton.ecs.cpsc411.mealpal.shared.DietLabels
import edu.fullerton.ecs.cpsc411.mealpal.shared.HealthLabels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationScope private val appScope: CoroutineScope
) {
    // provides a cache for frequently accessed user profile data
    private val userMpPrefsCache = MutableStateFlow(UserMpPrefs())

    init {
        // populate cache
        appScope.launch {
            val userDietLabels = getDietPreferences()
            val userHealthLabels = getHealthPreferences()
            userMpPrefsCache.update {
                it.copy(
                    userHealthLabels = userHealthLabels,
                    userDietLabels = userDietLabels
                )
            }
        }
    }

    suspend fun isOnboarded() : Boolean =
        dataStore.data.map { preferences ->
            preferences[IS_ONBOARDED] ?: false
        }.first()

    fun onboardingComplete() = appScope.launch {
        dataStore.edit { preferences ->
            preferences[IS_ONBOARDED] = true
        }
    }

    fun saveDietPreferences(dietPrefs: List<DietLabels>) = appScope.launch {
        dataStore.edit { preferences ->
            preferences[DIET_PREFS] = dietPrefs.toStringSet()
        }
        userMpPrefsCache.update { it.copy(userDietLabels = dietPrefs) }
    }

    suspend fun getDietPreferences() : List<DietLabels> {
        // return cache, if cache is not yet populated return persisted value
        return userMpPrefsCache.value.userDietLabels ?: run {
            dataStore.data.map { preferences ->
                preferences[DIET_PREFS]?.toDietLabels() ?: emptyList()
            }.first()
        }
    }

    fun saveHealthPreferences(selectedHealthLabels: List<HealthLabels>) = appScope.launch {
        dataStore.edit { preferences ->
            preferences[HEALTH_PREFS] = selectedHealthLabels.toStringSet()
        }
        userMpPrefsCache.update { it.copy(userHealthLabels = selectedHealthLabels) }
    }

    suspend fun getHealthPreferences() : List<HealthLabels> {
        // return cache, if cache is not yet populated return persisted value
        return userMpPrefsCache.value.userHealthLabels ?: run {
            dataStore.data.map { preferences ->
                preferences[HEALTH_PREFS]?.toHealthLabels() ?: emptyList()
            }.first()
        }
    }

    private fun <T : Enum<T>> List<Enum<T>>.toStringSet() = this.map { it.name }.toSet()

    private fun Set<String>.toDietLabels() : List<DietLabels> = this.map { DietLabels.valueOf(it) }
    private fun Set<String>.toHealthLabels() : List<HealthLabels> = this.map { HealthLabels.valueOf(it) }


    companion object {
        val IS_ONBOARDED = booleanPreferencesKey("isOnboarded")
        val DIET_PREFS = stringSetPreferencesKey("dietPrefs")
        val HEALTH_PREFS = stringSetPreferencesKey("healthPrefs")
    }
}

data class UserMpPrefs(
    val userHealthLabels: List<HealthLabels>? = null,
    val userDietLabels: List<DietLabels>? = null
)
package edu.fullerton.ecs.cpsc411.mealpal.ui.onboarding

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.fullerton.ecs.cpsc411.mealpal.data.repository.PreferencesRepository
import edu.fullerton.ecs.cpsc411.mealpal.shared.DietLabels
import edu.fullerton.ecs.cpsc411.mealpal.shared.HealthLabels
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.round

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    private val _onboardingUiState = MutableStateFlow(OnboardingUiState())
    val onboardingUiState = _onboardingUiState.asStateFlow()

    fun setProgress(screenNumber: Int) {
        _onboardingUiState.update {
            it.copy(
                progress = round(screenNumber * (100.0/ONBOARDING_SCREENS)).toInt()
            )
        }
    }

    fun updateSelectedDietLabels(checkedIds: List<Int>) {
        val selectedDietLabels = checkedIds.map {
            DietLabels.values()[it]
        }
        _onboardingUiState.update {
            it.copy(selectedDietLabels = selectedDietLabels)
        }
    }

    fun updateSelectedHealthLabels(checkedIds: List<Int>) {
        val selectedHealthLabels = checkedIds.map {
            HealthLabels.values()[it]
        }
        _onboardingUiState.update {
            it.copy(selectedHealthLabels = selectedHealthLabels)
        }
    }

    fun saveDietPreferences() {
        preferencesRepository.saveDietPreferences(_onboardingUiState.value.selectedDietLabels)
    }

    fun saveHealthPreferencesAndCompleteOnboarding() {
        preferencesRepository.saveHealthPreferences(_onboardingUiState.value.selectedHealthLabels)
        preferencesRepository.onboardingComplete()
    }

    companion object {
        private const val ONBOARDING_SCREENS = 3
    }

}

data class OnboardingUiState(
    val progress: Int = 0,
    val selectedDietLabels: List<DietLabels> = emptyList(),
    val selectedHealthLabels: List<HealthLabels> = emptyList()
)
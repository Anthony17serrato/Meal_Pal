package edu.fullerton.ecs.cpsc411.mealpal.ui.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.round

class OnboardingViewModel : ViewModel() {
    private val _onboardingUiState = MutableStateFlow(OnboardingUiState())
    val onboardingUiState = _onboardingUiState.asStateFlow()

    fun setProgress(screenNumber: Int) {
        _onboardingUiState.update {
            it.copy(
                progress = round(screenNumber * (100.0/ONBOARDING_SCREENS)).toInt()
            )
        }
    }

    companion object {
        private const val ONBOARDING_SCREENS = 3
    }

}

data class OnboardingUiState(
    val progress: Int = 0
)
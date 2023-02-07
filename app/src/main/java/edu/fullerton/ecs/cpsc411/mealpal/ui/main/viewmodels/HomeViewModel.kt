package edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel : ViewModel() {
    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState = _homeUiState.asStateFlow()

    fun onboardingShown() {
        _homeUiState.update {
            it.copy(isOnboardingShown = true)
        }
    }
}

data class HomeUiState(
    val isOnboardingShown: Boolean = false
)
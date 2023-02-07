package edu.fullerton.ecs.cpsc411.mealpal.ui.onboarding

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import edu.fullerton.ecs.cpsc411.mealpal.databinding.ActivityOnboardingBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding
    private val onboardingViewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.linearProgressIndicator.setProgressCompat(0, true)
        lifecycleScope.launch {
            delay(10)
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                onboardingViewModel.onboardingUiState
                    .map { it.progress }
                    .distinctUntilChanged()
                    .collect {
                        binding.linearProgressIndicator.setProgressCompat(it, true)
                    }
            }
        }
    }
}
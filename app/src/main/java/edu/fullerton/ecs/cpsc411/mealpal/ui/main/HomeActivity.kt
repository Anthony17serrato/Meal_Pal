package edu.fullerton.ecs.cpsc411.mealpal.ui.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import edu.fullerton.ecs.cpsc411.mealpal.R
import edu.fullerton.ecs.cpsc411.mealpal.data.repository.PreferencesRepository
import edu.fullerton.ecs.cpsc411.mealpal.databinding.ActivityHomeBinding
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.fragments.SearchRecipeDialogFragment
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.HomeViewModel
import edu.fullerton.ecs.cpsc411.mealpal.ui.onboarding.OnboardingActivity
import edu.fullerton.ecs.cpsc411.mealpal.utils.getColorFromAttr
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()
    @Inject lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        fun showBottomSearchSheet() {
            val modalBottomSheet = SearchRecipeDialogFragment()
            modalBottomSheet.show(supportFragmentManager, SearchRecipeDialogFragment.TAG)
        }
        binding.bottomNavigation.setupWithNavController(navController)
        binding.appBarLayout.setBackgroundColor(getColorFromAttr(Color.BLUE))
        binding.homeToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_search -> {
                    showBottomSearchSheet()

                    true
                }
                else -> { true }
            }
        }
        binding.searchBar.setOnClickListener {
            showBottomSearchSheet()
        }
        launchOnboardingIfRequired()
    }

    private fun launchOnboardingIfRequired() {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)
                lifecycleScope.launch {
                    if (!preferencesRepository.isOnboarded() &&
                        !homeViewModel.homeUiState.value.isOnboardingShown) {
                        homeViewModel.onboardingShown()
                        startActivity(
                            Intent(this@HomeActivity, OnboardingActivity::class.java)
                        )
                    }
                }
            }
        })
    }
}

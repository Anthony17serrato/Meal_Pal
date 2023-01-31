package edu.fullerton.ecs.cpsc411.mealpal.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import edu.fullerton.ecs.cpsc411.mealpal.R
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.fragments.SearchRecipeDialogFragment
import edu.fullerton.ecs.cpsc411.mealpal.databinding.ActivityMainBinding
import edu.fullerton.ecs.cpsc411.mealpal.ui.onboarding.OnboardingActivity
import edu.fullerton.ecs.cpsc411.mealpal.utils.getColorFromAttr
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    @Inject lateinit var dataStore: DataStore<Preferences>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)
        binding.appBarLayout.setBackgroundColor(getColorFromAttr(R.attr.colorSurface))
        binding.homeToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_search -> {
                    val newFragment: BottomSheetDialogFragment= SearchRecipeDialogFragment()

                    val ft =
                        supportFragmentManager.beginTransaction()
                    val prev =
                        supportFragmentManager.findFragmentById(R.id.dialog)
                    if (prev != null) {
                        ft.remove(prev)
                    }
                    // save transaction to the back stack
                    // save transaction to the back stack
                    ft.addToBackStack("dialog")
                    newFragment.show(ft, "dialog")
                    supportFragmentManager.executePendingTransactions()
                    true
                }
                else -> { true }
            }
        }
        binding.searchBar.setOnClickListener {
            val newFragment: BottomSheetDialogFragment= SearchRecipeDialogFragment()

            val ft =
                supportFragmentManager.beginTransaction()
            val prev =
                supportFragmentManager.findFragmentById(R.id.dialog)
            if (prev != null) {
                ft.remove(prev)
            }
            // save transaction to the back stack
            // save transaction to the back stack
            ft.addToBackStack("dialog")
            newFragment.show(ft, "dialog")
            supportFragmentManager.executePendingTransactions()
        }
        launchOnboardingIfRequired()
    }

    private fun launchOnboardingIfRequired() {
        val IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)
                lifecycleScope.launch {
                    val isOnboarded = dataStore.data.map { preferences ->
                        preferences[IS_ONBOARDED] ?: false
                    }.first()
                    if (!isOnboarded) {
                        startActivity(Intent(this@MainActivity, OnboardingActivity::class.java))
                    }
                }
            }
        })
    }
}

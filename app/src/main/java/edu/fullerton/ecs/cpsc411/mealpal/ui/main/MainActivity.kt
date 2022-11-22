package edu.fullerton.ecs.cpsc411.mealpal.ui.main

import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import edu.fullerton.ecs.cpsc411.mealpal.R
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.fragments.SearchRecipeDialogFragment
import edu.fullerton.ecs.cpsc411.mealpal.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

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

        binding.searchParams.inputType = InputType.TYPE_NULL
        binding.searchArea.setOnClickListener {
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
    }
}

package edu.fullerton.ecs.cpsc411.mealpal.ui.main

import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import edu.fullerton.ecs.cpsc411.mealpal.R
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.fragments.SearchRecipeDialogFragment
import edu.fullerton.ecs.cpsc411.mealpal.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    var sectionsPagerAdapter: SectionsPagerAdapter? = null
    var viewPager: ViewPager2? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.mytb.apply {
            setLogo(R.drawable.ic_logo_mp)
            title="  Meal Pal"
            setSupportActionBar(this)
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


        sectionsPagerAdapter = SectionsPagerAdapter( this)

        viewPager= findViewById(R.id.view_pager)
        viewPager!!.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        TabLayoutMediator(tabs, viewPager!!) { tab, position ->
            if(position==0){
                tab.text = "Discover"
            }else{
                tab.text = "My Meals"
            }
        }.attach()



    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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
            else -> super.onOptionsItemSelected(item)
        }
    }
}

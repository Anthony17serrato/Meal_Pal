package edu.fullerton.ecs.cpsc411.mealpal.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.fragments.DiscoverFragment
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.fragments.SavedMealsFragment

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun createFragment(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a DiscoverFragment (defined as a static inner class below).
        return if(position==0){
            DiscoverFragment()
        }else{
            SavedMealsFragment()
        }

    }

    override fun getItemCount(): Int {
        // Show 2 total pages.
        return 2
    }
}
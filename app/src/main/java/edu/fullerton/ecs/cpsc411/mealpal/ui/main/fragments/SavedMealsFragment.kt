package edu.fullerton.ecs.cpsc411.mealpal.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import edu.fullerton.ecs.cpsc411.mealpal.R
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.SavedMealsAdapter
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.SavedMealsViewModel
import edu.fullerton.ecs.cpsc411.mealpal.ui.recipedetails.RecipeDetailsActivity
import edu.fullerton.ecs.cpsc411.mealpal.utils.MEAL_URL
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SavedMealsFragment : Fragment() {
    private val recipeDetailsViewModel: SavedMealsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_my_meals, container, false)

        val recipeAdapter = SavedMealsAdapter {
            onClickItem(it)
        }
        val recipesRecycler: RecyclerView = root.findViewById(R.id.recycle_me_meals)
        recipesRecycler.apply {
            layoutManager = StaggeredGridLayoutManager(2,LinearLayoutManager.VERTICAL)
            adapter = recipeAdapter
        }

        lifecycleScope.launch {
            recipeDetailsViewModel.savedRecipes.collect {
                recipeAdapter.submitList(it)
            }
        }

        return root
    }

    private fun onClickItem(url: String) {
         Intent(context, RecipeDetailsActivity::class.java).apply {
             putExtra(MEAL_URL, url)
             startActivity(this)
         }
    }
}
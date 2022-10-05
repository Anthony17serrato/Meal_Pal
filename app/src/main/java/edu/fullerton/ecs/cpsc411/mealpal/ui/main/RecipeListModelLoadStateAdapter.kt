package edu.fullerton.ecs.cpsc411.mealpal.ui.main

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter

class RecipeListModelLoadStateAdapter(private val retry: () -> Unit) : LoadStateAdapter<RecipeListModelViewHolder>() {
    override fun onBindViewHolder(holder: RecipeListModelViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): RecipeListModelViewHolder {
        return RecipeListModelViewHolder.create(parent, retry)
    }
}
package edu.fullerton.ecs.cpsc411.mealpal.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import edu.fullerton.ecs.cpsc411.mealpal.R
import edu.fullerton.ecs.cpsc411.mealpal.databinding.RecipeListModelFooterViewItemBinding

class RecipeListModelViewHolder(
    private val binding: RecipeListModelFooterViewItemBinding,
    retry: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.retryButton.setOnClickListener { retry.invoke() }
    }

    fun bind(loadState: LoadState) {
        if (loadState is LoadState.Error) {
            binding.errorMsg.text = loadState.error.localizedMessage
        }
        binding.progressBar.isVisible = loadState is LoadState.Loading
        binding.retryButton.isVisible = loadState is LoadState.Error
        binding.errorMsg.isVisible = loadState is LoadState.Error
    }

    companion object {
        fun create(parent: ViewGroup, retry: () -> Unit): RecipeListModelViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recipe_list_model_footer_view_item, parent, false)
            val binding = RecipeListModelFooterViewItemBinding.bind(view)
            return RecipeListModelViewHolder(binding, retry)
        }
    }
}
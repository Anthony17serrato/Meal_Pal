package edu.fullerton.ecs.cpsc411.mealpal.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.fullerton.ecs.cpsc411.mealpal.R
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.DiscoverItemUiState

class TrendingAdapter(private val onClick: (String) -> Unit)
	: ListAdapter<DiscoverItemUiState, TrendingAdapter.RecipeViewHolder>(DiscoverItemUiStateDiffCallback) {

	class RecipeViewHolder(itemView: View, val onClick: (String) -> Unit) : RecyclerView.ViewHolder(itemView) {
		private val mealTitle: TextView = itemView.findViewById(R.id.trendingTitle)
		private val mealImage: ImageView = itemView.findViewById(R.id.trendingImage)
		private val viewRecipeButton: Button= itemView.findViewById(R.id.viewRecipeButton)
		private var url: String? = null

		/* Bind recipe data. */
		fun bind(discoverItem: DiscoverItemUiState) {
			discoverItem.recipeListModel.let { recipe ->
				url = recipe.url
				mealTitle.text = recipe.title
				Glide.with(mealImage.context).load(recipe.image).into(mealImage)
			}
			itemView.setOnClickListener {
				itemSelected(discoverItem)
			}
			viewRecipeButton.setOnClickListener {
				itemSelected(discoverItem)
			}
		}

		private fun itemSelected(discoverItem: DiscoverItemUiState) {
			discoverItem.onSelect()
			url?.let {
				onClick(it)
			}
		}
	}

	/* Creates and inflates view and return RecipeViewHolder. */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
		val view = LayoutInflater.from(parent.context)
			.inflate(R.layout.item_trend_meal, parent, false)
		return RecipeViewHolder(view, onClick)
	}

	/* Gets current RecipeListModel and uses it to bind view. */
	override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
		val recipe = getItem(position)
		holder.bind(recipe)
	}
}
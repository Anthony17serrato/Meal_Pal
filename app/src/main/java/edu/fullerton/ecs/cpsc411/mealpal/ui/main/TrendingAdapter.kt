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
import edu.fullerton.ecs.cpsc411.mealpal.db.RecipeListModel

class TrendingAdapter(private val onClick: (String) -> Unit) : ListAdapter<RecipeListModel, TrendingAdapter.RecipeViewHolder>(RecipeListModelDiffCallback) {

	class RecipeViewHolder(itemView: View, val onClick: (String) -> Unit) : RecyclerView.ViewHolder(itemView) {
		private val mealTitle: TextView = itemView.findViewById(R.id.trendingTitle)
		private val mealImage: ImageView = itemView.findViewById(R.id.trendingImage)
		private val viewRecipeButton: Button= itemView.findViewById(R.id.viewRecipeButton)
		private var url: String? = null

		init {
			itemView.setOnClickListener {
				url?.let {
					onClick(it)
				}
			}
			viewRecipeButton.setOnClickListener {
				url?.let {
					onClick(it)
				}
			}
		}

		/* Bind recipe data. */
		fun bind(recipe: RecipeListModel) {
			url = recipe.url
			mealTitle.text = recipe.title
			Glide.with(mealImage.context).load(recipe.image).into(mealImage)
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
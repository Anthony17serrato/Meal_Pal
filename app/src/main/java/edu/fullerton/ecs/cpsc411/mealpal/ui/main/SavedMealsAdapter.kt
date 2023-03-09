package edu.fullerton.ecs.cpsc411.mealpal.ui.main

import androidx.recyclerview.widget.ListAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.fullerton.ecs.cpsc411.mealpal.R
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.RecipeWithInteractions

class SavedMealsAdapter(private val onClick: (String) -> Unit) : ListAdapter<RecipeWithInteractions, SavedMealsAdapter.RecipeViewHolder>(RecipeListModelDiffCallback) {

	class RecipeViewHolder(itemView: View, val onClick: (String) -> Unit) : RecyclerView.ViewHolder(itemView) {
		private val mealTitle: TextView = itemView.findViewById(R.id.meal_title)
		private val calories: TextView = itemView.findViewById(R.id.caloriespreview)
		private val interactionText: TextView = itemView.findViewById(R.id.interactionText)
		private val interactionImage: ImageView = itemView.findViewById(R.id.interactionIcon)
		private val interactionCard: CardView = itemView.findViewById(R.id.interactionCard)
		private val mealImage: ImageView = itemView.findViewById(R.id.meal_img)
		private var url: String? = null

		init {
			itemView.setOnClickListener {
				url?.let {
					onClick(it)
				}
			}
		}

		/* Bind recipe data. */
		fun bind(recipeWithInteractions: RecipeWithInteractions) {
			val localContext = itemView.context
			recipeWithInteractions.recipe.let { recipe ->
				url = recipe.url
				mealTitle.text = recipe.title
				calories.text = itemView.context.getString(R.string.calories_indicator, recipe.calories.toInt().toString())
				Glide.with(mealImage.context).load(recipe.image).into(mealImage)
			}
			recipeWithInteractions.interactions.let {recipeInteractions ->
				interactionText.apply {
					text = localContext.getString(recipeInteractions.interactionLabel)
				}
				interactionImage.apply {
					setImageDrawable(
						AppCompatResources.getDrawable(
							localContext,
							recipeInteractions.interactionIcon
						)
					)
				}
				interactionCard.setCardBackgroundColor(
					ContextCompat.getColor(localContext, recipeInteractions.cardColor)
				)
			}

		}
	}

	/* Creates and inflates view and return RecipeViewHolder. */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
		val view = LayoutInflater.from(parent.context)
			.inflate(R.layout.item_meal, parent, false)
		return RecipeViewHolder(view, onClick)
	}

	/* Gets current RecipeListModel and uses it to bind view. */
	override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
		val recipe = getItem(position)
		holder.bind(recipe)
	}
}

object RecipeListModelDiffCallback : DiffUtil.ItemCallback<RecipeWithInteractions>() {
	override fun areItemsTheSame(oldItem: RecipeWithInteractions, newItem: RecipeWithInteractions): Boolean {
		return oldItem == newItem
	}

	override fun areContentsTheSame(oldItem: RecipeWithInteractions, newItem: RecipeWithInteractions): Boolean {
		return oldItem.recipe.saveTime == newItem.recipe.saveTime
	}
}
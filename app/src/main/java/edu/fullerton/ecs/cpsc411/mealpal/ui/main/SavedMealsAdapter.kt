package edu.fullerton.ecs.cpsc411.mealpal.ui.main

import androidx.recyclerview.widget.ListAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.fullerton.ecs.cpsc411.mealpal.R
import edu.fullerton.ecs.cpsc411.mealpal.db.RecipeListModel

class SavedMealsAdapter(private val onClick: (String) -> Unit) : ListAdapter<RecipeListModel, SavedMealsAdapter.RecipeViewHolder>(RecipeListModelDiffCallback) {

	class RecipeViewHolder(itemView: View, val onClick: (String) -> Unit) : RecyclerView.ViewHolder(itemView) {
		private val mealTitle: TextView = itemView.findViewById(R.id.meal_title)
		private val calories: TextView = itemView.findViewById(R.id.caloriespreview)
		private val mealInfo: TextView = itemView.findViewById(R.id.textView2)
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
		fun bind(recipe: RecipeListModel) {
			url = recipe.url
			mealTitle.text = recipe.title
			calories.text = itemView.context.getString(R.string.calories_indicator, recipe.calories.toInt().toString())
			Glide.with(mealImage.context).load(recipe.image).into(mealImage)

			val joined = ArrayList<String>()
			joined.addAll(recipe.dietLabels)
			joined.addAll(recipe.healthLabels)
			joined.addAll(recipe.cautions)
			var info = "Info: "
			for (item in joined){
				info += "$item, "
			}
			info = info.substring(0,info.length-2)
			mealInfo.text = info
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

object RecipeListModelDiffCallback : DiffUtil.ItemCallback<RecipeListModel>() {
	override fun areItemsTheSame(oldItem: RecipeListModel, newItem: RecipeListModel): Boolean {
		return oldItem == newItem
	}

	override fun areContentsTheSame(oldItem: RecipeListModel, newItem: RecipeListModel): Boolean {
		return oldItem.saveTime == newItem.saveTime
	}
}
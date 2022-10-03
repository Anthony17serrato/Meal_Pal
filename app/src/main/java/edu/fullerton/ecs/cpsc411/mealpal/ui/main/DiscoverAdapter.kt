package edu.fullerton.ecs.cpsc411.mealpal.ui.main

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import edu.fullerton.ecs.cpsc411.mealpal.R
import edu.fullerton.ecs.cpsc411.mealpal.db.RecipeListModel

class DiscoverAdapter(private val onClick: (String) -> Unit)
	: PagingDataAdapter<RecipeListModel, RecyclerView.ViewHolder>(RecipeListModelDiffCallback) {

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

	class TrendingViewHolder(itemView: View, val onClick: (String) -> Unit)
		: RecyclerView.ViewHolder(itemView) {
		private val trendingRecycler: RecyclerView = itemView.findViewById(R.id.nestedrecycler)
		private val recipeAdapter = TrendingAdapter {
			onClick(it)
		}

		init {
			trendingRecycler.apply {
				layoutManager = LinearLayoutManager(trendingRecycler.context, LinearLayoutManager.HORIZONTAL,false)
				adapter = recipeAdapter
			}


			trendingRecycler.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {

				override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

				override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
					when (e.action) {
						MotionEvent.ACTION_DOWN -> {
							trendingRecycler.parent?.requestDisallowInterceptTouchEvent(true)
						}
					}
					return false
				}

				override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
			})
		}

		/* Bind trending recipe data. */
		fun bind(trendingRecipes: List<RecipeListModel>) {
			recipeAdapter.submitList(trendingRecipes)
		}
	}

	/* Creates and inflates view and return RecipeViewHolder. */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return when(viewType) {
			TRENDING -> {
				val view = LayoutInflater.from(parent.context)
					.inflate(R.layout.item_trend, parent, false)
				TrendingViewHolder(view, onClick)
			}
			MEAL -> {
				val view = LayoutInflater.from(parent.context)
					.inflate(R.layout.item_meal, parent, false)
				RecipeViewHolder(view, onClick)
			}
			else -> throw IllegalArgumentException("No viewHolder found for item of viewType $viewType")
		}

	}

	override fun getItemViewType(position: Int): Int {
		return if (position == 0) {
			TRENDING
		} else {
			MEAL
		}
	}

	/* Gets current RecipeListModel and uses it to bind view. */
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when(holder) {
			is TrendingViewHolder -> {
				val layoutParams: StaggeredGridLayoutManager.LayoutParams =
			    		holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
				layoutParams.isFullSpan = true
				val trendingMeals = mutableListOf<RecipeListModel>()
				for (recipe in 0 .. TRENDING_SIZE) {
					getItem(recipe)?.let {
						trendingMeals.add(it)
					}
				}
				holder.bind(trendingMeals)
			}
			is RecipeViewHolder -> {
				val adjustedPosition = position + TRENDING_SIZE
				val recipe = getItem(if (adjustedPosition < itemCount) adjustedPosition else 0)
				recipe?.let {
					holder.bind(it)
				}
			}
		}
	}

	companion object {
		private const val TRENDING = 1
		private const val MEAL = 2
		private const val TRENDING_SIZE = 4
	}
}
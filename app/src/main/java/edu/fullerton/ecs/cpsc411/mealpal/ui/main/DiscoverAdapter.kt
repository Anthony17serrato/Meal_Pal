package edu.fullerton.ecs.cpsc411.mealpal.ui.main

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import edu.fullerton.ecs.cpsc411.mealpal.R
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.fragments.QuickPicks
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.DiscoverItemUiState
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.DiscoverViewModel
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.QuickPicks
import kotlinx.coroutines.delay

class DiscoverAdapter(
	private val onClick: (String) -> Unit,
	private val onSelectQuickPick: (QuickPicks) -> Unit,
	// TODO fix anti-pattern
	private val viewModel: DiscoverViewModel
) : PagingDataAdapter<DiscoverItemUiState, RecyclerView.ViewHolder>(DiscoverItemUiStateDiffCallback) {

	class RecipeViewHolder(itemView: View, val onClick: (String) -> Unit) : RecyclerView.ViewHolder(itemView) {
		private val mealTitle: TextView = itemView.findViewById(R.id.meal_title)
		private val calories: TextView = itemView.findViewById(R.id.caloriespreview)
		private val interactionText: TextView = itemView.findViewById(R.id.interactionText)
		private val interactionImage: ImageView = itemView.findViewById(R.id.interactionIcon)
		private val interactionCard: CardView = itemView.findViewById(R.id.interactionCard)
		private val mealImage: ImageView = itemView.findViewById(R.id.meal_img)
		private var url: String? = null

		/* Bind recipe data. */
		fun bind(discoverItem: DiscoverItemUiState) {
			val localContext = itemView.context
			itemView.setOnClickListener {
				discoverItem.onSelect()
				url?.let {
					onClick(it)
				}
			}
			discoverItem.recipeListModel.let { recipe ->
				url = recipe.url
				mealTitle.text = recipe.title
				calories.text = localContext.getString(R.string.calories_indicator, recipe.calories.toInt().toString())
				Glide.with(mealImage.context).load(recipe.image).into(mealImage)
				interactionText.apply {
					text = localContext.getString(discoverItem.recipeInteractions.interactionLabel)
				}
				interactionImage.apply {
					setImageDrawable(
						AppCompatResources.getDrawable(
							localContext,
							discoverItem.recipeInteractions.interactionIcon
						)
					)
				}
				interactionCard.setCardBackgroundColor(
					ContextCompat.getColor(localContext, discoverItem.recipeInteractions.cardColor)
				)
			}
		}
	}

	class TrendingViewHolder(
		itemView: View,
		val onClick: (String) -> Unit,
		private val onSelectQuickPick: (QuickPicks) -> Unit,
		viewModel: DiscoverViewModel
	)
		: RecyclerView.ViewHolder(itemView) {
		private val trendingRecycler: RecyclerView = itemView.findViewById(R.id.nestedrecycler)
		private val composeQuickPicks: ComposeView = itemView.findViewById(R.id.compose_quick_picks)

		private val recipeAdapter = TrendingAdapter {
			onClick(it)
		}

		init {
			composeQuickPicks.apply {
				// Dispose of the Composition when the view's LifecycleOwner
				// is destroyed
				setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
				setContent {
					// In Compose world
					Mdc3Theme {
						QuickPicks(onSelectQuickPick, viewModel)
					}
				}
			}
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
		fun bind(trendingRecipes: List<DiscoverItemUiState>) {
			recipeAdapter.submitList(trendingRecipes)
		}
	}

	/* Creates and inflates view and return RecipeViewHolder. */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return when(viewType) {
			TRENDING -> {
				val view = LayoutInflater.from(parent.context)
					.inflate(R.layout.item_trend, parent, false)
				TrendingViewHolder(view, onClick, onSelectQuickPick, viewModel)
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
				val trendingMeals = mutableListOf<DiscoverItemUiState>()
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

object DiscoverItemUiStateDiffCallback : DiffUtil.ItemCallback<DiscoverItemUiState>() {
	override fun areItemsTheSame(oldItem: DiscoverItemUiState, newItem: DiscoverItemUiState): Boolean {
		return oldItem == newItem
	}

	override fun areContentsTheSame(oldItem: DiscoverItemUiState, newItem: DiscoverItemUiState): Boolean {
		return oldItem.recipeListModel.saveTime == newItem.recipeListModel.saveTime
	}
}
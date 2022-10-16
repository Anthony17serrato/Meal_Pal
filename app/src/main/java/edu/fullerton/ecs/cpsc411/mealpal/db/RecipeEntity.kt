package edu.fullerton.ecs.cpsc411.mealpal.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import edu.fullerton.ecs.cpsc411.mealpal.network.NetworkRecipe
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.DiscoverQuery

@Entity(tableName = "recipe_table")
data class RecipeEntity(
	@PrimaryKey val url: String,
	val saveTime: Long = System.currentTimeMillis(),
	val title: String,
	val image: String,
	@Embedded val images: Images,
	val ingredients: List<String>,
	val calories: Double = 0.0,
	val yield: Double = 0.0,
	@ColumnInfo(name = "diet_labels")
	val dietLabels: List<String>,
	@ColumnInfo(name = "health_labels")
	val healthLabels: List<String>,
	val cautions: List<String>,
	@ColumnInfo(name = "is_saved")
	val isSaved: Boolean = false,
	@Embedded(prefix = "discover_")
	val query: DiscoverQuery
)

data class Images (
	val thumbnail: String?,
	val small: String?,
	val regular: String?,
	val large: String?
)

data class RecipeListModel(
	val url: String,
	val saveTime: Long,
	val title: String,
	val image: String,
	val calories: Double,
	@ColumnInfo(name = "diet_labels")
	val dietLabels: List<String>,
	@ColumnInfo(name = "health_labels")
	val healthLabels: List<String>,
	val cautions: List<String>
)

fun RecipeEntity.asRecipeListModel() = RecipeListModel(
	url = url,
	saveTime = saveTime,
	title = title,
	image = image,
	calories = calories,
	dietLabels = dietLabels,
	healthLabels = healthLabels,
	cautions = cautions
)
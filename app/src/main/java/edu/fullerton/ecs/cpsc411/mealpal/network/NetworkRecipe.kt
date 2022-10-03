package edu.fullerton.ecs.cpsc411.mealpal.network

import com.squareup.moshi.Json
import edu.fullerton.ecs.cpsc411.mealpal.db.Images
import edu.fullerton.ecs.cpsc411.mealpal.db.RecipeEntity

data class NetworkRecipe(
	val url: String,
	val label: String,
	val image: String,
	val images: ImageVariants,
	val ingredientLines: List<String>,
	val calories: Double = 0.0,
	val yield: Double = 0.0,
	val dietLabels: List<String>,
	val healthLabels: List<String>,
	val cautions: List<String>
)

data class ImageVariants(
	@Json(name = "THUMBNAIL") val thumbnail: VariantUrl?,
	@Json(name = "SMALL") val small: VariantUrl?,
	@Json(name = "REGULAR") val regular: VariantUrl?,
	@Json(name = "LARGE") val large: VariantUrl?
)

data class VariantUrl(
	val url: String?
)

fun NetworkRecipe.asEntity(pageId: String?) = RecipeEntity(
	title = label,
	image = image,
	images = Images(images.thumbnail?.url, images.small?.url, images.regular?.url, images.large?.url),
	ingredients = ingredientLines,
	url = url,
	calories = calories,
	yield = yield,
	dietLabels = dietLabels,
	healthLabels = healthLabels,
	cautions = cautions,
	pageId = pageId
)


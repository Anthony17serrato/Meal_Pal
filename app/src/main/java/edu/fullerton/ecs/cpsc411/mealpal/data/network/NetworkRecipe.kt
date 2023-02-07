package edu.fullerton.ecs.cpsc411.mealpal.data.network

import com.squareup.moshi.Json
import edu.fullerton.ecs.cpsc411.mealpal.data.local.entities.Images
import edu.fullerton.ecs.cpsc411.mealpal.data.local.entities.IngredientEntity
import edu.fullerton.ecs.cpsc411.mealpal.data.local.entities.RecipeEntity
import edu.fullerton.ecs.cpsc411.mealpal.data.local.entities.RecipeWithIngredients

data class NetworkRecipe(
	val url: String,
	val label: String,
	val image: String,
	val images: ImageVariants,
	val ingredients: List<Ingredient>,
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

data class Ingredient(
	val text: String,
	@Json(name = "image") val imageUrl: String?
)

data class VariantUrl(
	val url: String?
)

private fun Ingredient.asIngredientEntity(entityId: String) = IngredientEntity(
	entityId= entityId,
	text= text,
	imageUrl = imageUrl
)

private fun List<Ingredient>.asIngredientEntityList(entityId: String) = this.map { it.asIngredientEntity(entityId) }

fun NetworkRecipe.asRecipeWithIngredients(pageId: String?) = RecipeWithIngredients(
	recipe = RecipeEntity(
		title = label,
		image = image,
		images = Images(images.thumbnail?.url, images.small?.url, images.regular?.url, images.large?.url),
		url = url,
		calories = calories/yield,
		yield = yield,
		dietLabels = dietLabels,
		healthLabels = healthLabels,
		cautions = cautions,
		pageId = pageId
	),
	ingredients = ingredients.asIngredientEntityList(url)
)
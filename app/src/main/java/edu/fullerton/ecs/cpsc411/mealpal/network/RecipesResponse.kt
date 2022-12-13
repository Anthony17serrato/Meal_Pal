package edu.fullerton.ecs.cpsc411.mealpal.network

import com.squareup.moshi.Json

data class RecipesResponse(
	@Json(name = "hits") val hits: List<Hits>,
	@Json(name = "_links") val links: RecipeLinks
)

data class Hits(
	@Json(name = "recipe") val recipe: NetworkRecipe
)

data class RecipeLinks(
	@Json(name = "next") val nextPage: NextPage?
)

data class NextPage(
	@Json(name = "href") val url: String?
)

fun List<Hits>.asEntityList(pageId: String?) =
	this.map { it.recipe.asRecipeWithIngredients(pageId) }
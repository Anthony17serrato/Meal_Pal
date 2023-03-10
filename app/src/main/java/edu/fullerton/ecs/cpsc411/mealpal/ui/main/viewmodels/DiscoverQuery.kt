package edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DiscoverQuery(
	val keyword: String = "",
	val calThresh: String = "1800",
	val calMin: String = "200",
	val healthLabels: List<String>? = null,
	val dietLabels: List<String>? = null
) : Parcelable
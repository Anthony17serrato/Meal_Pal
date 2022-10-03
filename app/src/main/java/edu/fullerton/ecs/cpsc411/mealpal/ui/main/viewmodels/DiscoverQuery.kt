package edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DiscoverQuery(
	val keyword: String = "Italian",
	val calThresh: String = "800",
	val calMin: String = "200",
	val hLabel: String? = null
) : Parcelable
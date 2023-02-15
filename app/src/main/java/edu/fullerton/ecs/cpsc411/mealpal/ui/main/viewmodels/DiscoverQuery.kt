package edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DiscoverQuery(
	val keyword: String = "",
	val calThresh: String = "1000",
	val calMin: String = "200",
	val hLabel: String? = null
) : Parcelable
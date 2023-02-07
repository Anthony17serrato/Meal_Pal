package edu.fullerton.ecs.cpsc411.mealpal.shared

import androidx.annotation.StringRes
import edu.fullerton.ecs.cpsc411.mealpal.R

enum class DietLabels(val apiValue: String, @StringRes val resId: Int) {
    Balanced("balanced", R.string.diet_balanced),
    HighFiber("high-fiber", R.string.diet_high_fiber),
    HighProtein("high-protein", R.string.diet_high_protein),
    LowCarb("low-carb", R.string.diet_low_carb),
    LowFat("low-fat", R.string.diet_low_fat),
    LowSodium("low-sodium", R.string.diet_low_sodium)
}
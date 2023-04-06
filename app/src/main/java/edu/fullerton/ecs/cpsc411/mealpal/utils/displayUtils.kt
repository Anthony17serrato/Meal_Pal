package edu.fullerton.ecs.cpsc411.mealpal.utils

import android.content.res.Resources

fun Resources.dpToDevicePixels(dp: Int) = displayMetrics.density * dp
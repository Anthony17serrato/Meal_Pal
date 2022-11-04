package edu.fullerton.ecs.cpsc411.mealpal.ui.recipedetails

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.text.bold
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dagger.hilt.android.AndroidEntryPoint
import edu.fullerton.ecs.cpsc411.mealpal.MealPalApplication
import edu.fullerton.ecs.cpsc411.mealpal.R
import edu.fullerton.ecs.cpsc411.mealpal.databinding.ActivityRecipieDetailsBinding
import edu.fullerton.ecs.cpsc411.mealpal.db.RecipeEntity
import edu.fullerton.ecs.cpsc411.mealpal.utils.MEAL_URL
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecipeDetailsActivity : AppCompatActivity() {
    private val recipeDetailsViewModel: RecipeDetailsViewModel by viewModels()
    private lateinit var binding: ActivityRecipieDetailsBinding
    var tabBuilder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipieDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Set an id to the layout
        val currentLayout =
            findViewById<View>(R.id.recipieview) as CoordinatorLayout

        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // get recipe url
        val recipeUrl = intent.getStringExtra(MEAL_URL).toString()
        recipeDetailsViewModel.setRecipe(recipeUrl)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    recipeDetailsViewModel.recipeUiState
                        .map { it.recipeEntity }
                        .distinctUntilChanged()
                        .collect { recipe ->
                            recipe?.let {
                                displayRecipe(it)
                            }
                        }
                }
                launch {
                    val defaultLightColor = ContextCompat.getColor(this@RecipeDetailsActivity, R.color.colorPrimary)
                    val defaultVibrantColor = ContextCompat.getColor(this@RecipeDetailsActivity, R.color.colorBorderGrey)
                    val defaultDarkColor = ContextCompat.getColor(this@RecipeDetailsActivity, R.color.mtrlGrey)
                    recipeDetailsViewModel.recipeUiState
                        .map { it.palette }
                        .distinctUntilChanged()
                        .collect { palette ->
                            palette?.let {
                                it.getLightVibrantColor(defaultLightColor).let { color ->
                                    binding.recipieview.setBackgroundColor(color)
                                    window.statusBarColor = color
                                    window.navigationBarColor = color
                                    tabBuilder = CustomTabsIntent.Builder().setToolbarColor(color)
                                }
                                it.getLightVibrantColor(defaultVibrantColor).let { color ->
                                    binding.cardsave.setCardBackgroundColor(ColorUtils.setAlphaComponent(color, 100))
                                }
                                it.getDarkMutedColor(defaultDarkColor).let { color ->
                                    binding.recipeTitle.setTextColor(color)
                                    binding.tving.setTextColor(color)
                                }
                            }
                        }
                }
            }
        }

        binding.openRecipe.setOnClickListener {
            val customTabsIntent = tabBuilder.build()
            customTabsIntent.launchUrl(this, Uri.parse(recipeUrl))
        }

        binding.saveIcon.setOnClickListener {
            recipeDetailsViewModel.onSaveToggle()
//            Snackbar.make(save.rootView, "Recipe Saved", Snackbar.LENGTH_SHORT).setBackgroundTint(resources.getColor(R.color.colorAccent)).show()
        }
    }

    private fun displayRecipe(recipeEntity: RecipeEntity) {
        binding.saveIcon.setImageResource(if (recipeEntity.isSaved) R.drawable.ic_baseline_star_24 else R.drawable.ic_round_star_border_24)
        var ingredients = ""
        for(item in recipeEntity.ingredients){
            ingredients += "\u25CF $item\n"
        }
        binding.ingredients.text = ingredients
        val joined = ArrayList<String>()
        joined.addAll(recipeEntity.dietLabels)
        joined.addAll(recipeEntity.healthLabels)
        joined.addAll(recipeEntity.cautions)
        var info = ""
        for (item in joined) {
            info += "$item, "
        }
        val infoSpannable = SpannableStringBuilder().bold {
            append(getString(R.string.info_tag))
        }.append(info)
        binding.info.setText(infoSpannable, TextView.BufferType.SPANNABLE)
        val yieldSpannable = SpannableStringBuilder().bold {
            append(getString(R.string.yield_tag))
        }.append(recipeEntity.yield.toInt().toString())
        binding.yield.setText(yieldSpannable, TextView.BufferType.SPANNABLE)
        binding.calories.text = getString(R.string.calories_indicator, recipeEntity.calories.toInt().toString())
        binding.recipeTitle.text = recipeEntity.title
        Glide.with(binding.featuredimage.context)
            .load(
                when {
                    recipeEntity.images.large != null -> {
                        recipeEntity.images.large
                    }
                    recipeEntity.images.regular != null -> {
                        recipeEntity.images.regular
                    }
                    recipeEntity.images.small != null -> {
                        recipeEntity.images.small
                    }
                    recipeEntity.images.thumbnail != null -> {
                        recipeEntity.images.thumbnail
                    }
                    else -> {
                        recipeEntity.image
                    }
                }
            )
            .into(binding.featuredimage)
        Glide.with(this)
            .asBitmap()
            .load(recipeEntity.image)
            .into(object : CustomTarget<Bitmap>(){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    recipeDetailsViewModel.createPalette(resource)
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }
}
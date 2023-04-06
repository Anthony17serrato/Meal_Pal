package edu.fullerton.ecs.cpsc411.mealpal.ui.main.fragments

import android.animation.*
import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import androidx.core.view.marginTop
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.RangeSlider
import edu.fullerton.ecs.cpsc411.mealpal.R
import edu.fullerton.ecs.cpsc411.mealpal.databinding.FragmentSearchRecipeDialogBinding
import edu.fullerton.ecs.cpsc411.mealpal.shared.DietLabels
import edu.fullerton.ecs.cpsc411.mealpal.shared.HealthLabels
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.*
import edu.fullerton.ecs.cpsc411.mealpal.utils.dpToDevicePixels
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

private const val INITIAL_BOTTOM_SHEET_DELAY = 100L
class SearchRecipeDialogFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentSearchRecipeDialogBinding? = null
    private val binding get() = _binding!!
    private val discoverViewModel: DiscoverViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = BottomSheetDialog(requireContext(), theme)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchRecipeDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.bindState(
            uiState = discoverViewModel.discoverSearchState
        )
    }

    override fun getTheme(): Int {
        return R.style.ModalBottomSheetDialog
    }

    private fun FragmentSearchRecipeDialogBinding.bindState(
        uiState: StateFlow<DiscoverSearchState>
    ) {
        searchRecipeContainer.clipChildren = false
        uiState.value.apply {
            editTextKeyword.setText(currentKeyword)
            rangeSlider.values = listOf(minCalories.toFloat(), maxCalories.toFloat())
            if (selectedHealthLabels.isNotEmpty()) {
                editTextHealthLabels.setText(selectedHealthLabels[0].resId)
            }
            if (selectedDietLabels.isNotEmpty()) {
                editTextDietLabels.setText(selectedDietLabels[0].resId)
            }
            healthProfileFilterSwitch.isChecked = shouldUseHealthProfile
        }

        viewLifecycleOwner.lifecycleScope.launch {
            delay(INITIAL_BOTTOM_SHEET_DELAY)
            val heightChange = dietInput.measuredHeight.toFloat()
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                uiState.map { it.shouldUseHealthProfile }
                    .distinctUntilChanged()
                    .collect { shouldHideOptions ->
                        when {
                            shouldHideOptions && dietInput.visibility == View.VISIBLE -> {
                                // Create an alpha animation for both views
                                val alphaAnimation = AlphaAnimation(1.0f, 0.0f).apply {
                                    duration = 200
                                }

                                // Apply the animation to both views
                                dietInput.startAnimation(alphaAnimation)
                                healthInput.startAnimation(alphaAnimation)

                                // Set the visibility of both views to INVISIBLE when the animation ends
                                alphaAnimation.setAnimationListener(object : Animation.AnimationListener {
                                    override fun onAnimationStart(animation: Animation?) {}
                                    override fun onAnimationEnd(animation: Animation?) {
                                        val originalHeight = searchRecipeContainer.measuredHeight.toFloat()
                                        dietInput.visibility = View.GONE
                                        healthInput.visibility = View.GONE

                                        val animator = ValueAnimator.ofFloat(originalHeight, originalHeight - heightChange)
                                        animator.addUpdateListener { valueAnimator ->
                                            val currentHeight = valueAnimator.animatedValue as Float
                                            healthProfileFilterSwitch.updatePadding(
                                                bottom = (heightChange - (originalHeight - currentHeight)).toInt()
                                            )
                                            // Label visibility toggle required for bug documented here:
                                            // https://github.com/material-components/material-components-android/blob/master/docs/components/Slider.md#addingremoving-the-value-label
                                            rangeSlider.labelBehavior =  LabelFormatter.LABEL_GONE
                                            rangeSlider.labelBehavior =  LabelFormatter.LABEL_VISIBLE
                                        }
                                        val set = AnimatorSet()
                                        set.playTogether(animator)
                                        set.duration = 200
                                        set.start()
                                    }
                                    override fun onAnimationRepeat(animation: Animation?) {}
                                })

                                editTextHealthLabels.setText("")
                                editTextDietLabels.setText("")
                            }
                            !shouldHideOptions && dietInput.visibility == View.GONE -> {
                                val originalHeight = searchRecipeContainer.measuredHeight.toFloat()
                                val animator = ValueAnimator.ofFloat(originalHeight, originalHeight + (heightChange))
                                animator.addUpdateListener { valueAnimator ->
                                    val currentHeight = valueAnimator.animatedValue as Float
                                    healthProfileFilterSwitch.updatePadding(
                                        bottom = (currentHeight - originalHeight).toInt()
                                    )
                                    // Label visibility toggle required for bug documented here:
                                    // https://github.com/material-components/material-components-android/blob/master/docs/components/Slider.md#addingremoving-the-value-label
                                    rangeSlider.labelBehavior =  LabelFormatter.LABEL_GONE
                                    rangeSlider.labelBehavior =  LabelFormatter.LABEL_VISIBLE
                                }
                                AnimatorSet().apply {
                                    playTogether(animator)
                                    duration = 200
                                    addListener(object : AnimatorListenerAdapter() {
                                        override fun onAnimationEnd(animation: Animator) {
                                            // Create an alpha animation for both views
                                            val alphaAnimation = AlphaAnimation(0.0f, 1.0f).apply {
                                                duration = 200
                                            }

                                            // Apply the animation to both views
                                            dietInput.startAnimation(alphaAnimation)
                                            healthInput.startAnimation(alphaAnimation)

                                            // Set the visibility of both views to INVISIBLE when the animation ends
                                            alphaAnimation.setAnimationListener(object : Animation.AnimationListener {
                                                override fun onAnimationStart(animation: Animation?) {}
                                                override fun onAnimationEnd(animation: Animation?) {
                                                    dietInput.visibility = View.VISIBLE
                                                    healthInput.visibility = View.VISIBLE
                                                    healthProfileFilterSwitch.updatePadding(
                                                        bottom = 0
                                                    )
                                                }
                                                override fun onAnimationRepeat(animation: Animation?) {}
                                            })

                                        }
                                    })
                                    start()
                                }
                            }
                        }
                    }
            }
        }

        editTextKeyword.doOnTextChanged { text, _, _, _ ->
            discoverViewModel.updateCurrentKeyword(text)
        }
        healthProfileFilterSwitch.setOnCheckedChangeListener { _, isChecked ->
            discoverViewModel.setUseHealthProfile(isChecked)
        }
        val dietLabelsAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this@SearchRecipeDialogFragment.requireContext(),
            android.R.layout.select_dialog_item,
            listOf(getString(R.string.none_selection_option)) +
                    DietLabels.values().map {
                        getString(it.resId).apply {
                            discoverViewModel.indexLocalizedDietLabels(this, it)
                        }
                    }
        )
        editTextDietLabels.apply {
            setAdapter(dietLabelsAdapter)
            inputType = InputType.TYPE_NULL
            doOnTextChanged { text, _, _, _ ->
                discoverViewModel.setDietLabel(text)
            }
        }

        val healthLabelsAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this@SearchRecipeDialogFragment.requireContext(),
            android.R.layout.select_dialog_item,
            listOf(getString(R.string.none_selection_option)) +
                    HealthLabels.values().map {
                        getString(it.resId).apply {
                            discoverViewModel.indexLocalizedHealthLabels(this, it)
                        }
                    }
        )
        editTextHealthLabels.apply {
            setAdapter(healthLabelsAdapter)
            inputType = InputType.TYPE_NULL
            doOnTextChanged { text, _, _, _ ->
                discoverViewModel.setHealthLabel(text)
            }
        }

        rangeSlider.addOnChangeListener { rangeSlider, value, fromUser ->
            Timber.i("${rangeSlider.activeThumbIndex}, $value, $fromUser")
            when (rangeSlider.activeThumbIndex) {
                // min
                0 -> {
                    discoverViewModel.setMinCalories(value)
                }
                // max
                1 -> {
                    discoverViewModel.setMaxCalories(value)
                }
                else -> {
                    // invalid slider do nothing
                }
            }
        }

        buttonApply.setOnClickListener {
            discoverViewModel.executeSearch()
            dismiss()
        }
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}

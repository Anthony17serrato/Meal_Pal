package edu.fullerton.ecs.cpsc411.mealpal.ui.main.fragments

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import edu.fullerton.ecs.cpsc411.mealpal.MealPalApplication
import edu.fullerton.ecs.cpsc411.mealpal.R
import edu.fullerton.ecs.cpsc411.mealpal.databinding.FragmentSearchRecipeDialogBinding
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SearchRecipeDialogFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentSearchRecipeDialogBinding? = null
    private val binding get() = _binding!!
    private val discoverViewModel: DiscoverViewModel by activityViewModels {
        DiscoverViewModelFactory(
            this,
            (activity?.application as MealPalApplication).appContainer.repository
        )
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = BottomSheetDialog(requireContext(), theme)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchRecipeDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.bindState(
            uiState = discoverViewModel.discoverUiState,
            onQueryChanged = discoverViewModel.accept
        )
    }

    private fun FragmentSearchRecipeDialogBinding.bindState(
        uiState: StateFlow<DiscoverUiState>,
        onQueryChanged: (UiAction.Search) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                uiState.map { it.query }
                    .distinctUntilChanged()
                    .map { it.keyword }
                    .collect(editTextKeyword::setText)
            }
        }
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(this@SearchRecipeDialogFragment.requireContext(), android.R.layout.select_dialog_item, dLabels)
        editTextDietLabels.apply {
            setAdapter(adapter)
            inputType = InputType.TYPE_NULL
        }

        val adapterHealth: ArrayAdapter<String> = ArrayAdapter<String>(this@SearchRecipeDialogFragment.requireContext(), android.R.layout.select_dialog_item, hLabels)
        editTextHealthLabels.apply {
            setAdapter(adapterHealth)
            inputType = InputType.TYPE_NULL
        }

        calorieRangeSeekbar.apply {
            setMinStartValue(200f).setMaxStartValue(1800f).apply()

            setOnRangeSeekbarChangeListener { minValue, maxValue ->
                textMin1.text = getString(R.string.min_calories_label, minValue.toString())
                textMax1.text = getString(R.string.max_calories_label, maxValue.toString())
            }
        }

        buttonApply.setOnClickListener {
            // TODO: Build the entire query not just keyword
            editTextKeyword.text.trim().let {
                if (it.isNotEmpty()) {
                    onQueryChanged(UiAction.Search(query = DiscoverQuery(keyword = it.toString())))
                }
            }
            dismiss()
        }
    }

    companion object {
        private val dLabels = listOf("Balanced", "High-Fiber", "High-Protein", "Low-Carb","Low-Fat","Low-Sodium")
        private val hLabels = listOf("Alcohol-free","Celery-free","Crustacean-free","Dairy-free","Egg-free","Fish-free","Gluten-free","Keto","Kidney friendly","Kosher","Low potassium","Lupine-free","Mustard-free","No oil added","No-sugar","Paleo","Peanut-free","Pescatarian","Pork-free","Red meat-free","Sesame-free","Shelfish-free","Soy-free","Sugar-conscious","Tree-Nut-free","Vegan","Vegetarian","Wheat-free")
    }
}

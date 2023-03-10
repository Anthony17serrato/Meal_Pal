package edu.fullerton.ecs.cpsc411.mealpal.ui.main.fragments

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import edu.fullerton.ecs.cpsc411.mealpal.R
import edu.fullerton.ecs.cpsc411.mealpal.databinding.FragmentSearchRecipeDialogBinding
import edu.fullerton.ecs.cpsc411.mealpal.shared.DietLabels
import edu.fullerton.ecs.cpsc411.mealpal.shared.HealthLabels
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.*
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

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

    private fun FragmentSearchRecipeDialogBinding.bindState(
        uiState: StateFlow<DiscoverSearchState>
    ) {
        uiState.value.apply {
            editTextKeyword.setText(currentKeyword)
            rangeSlider.values = listOf(minCalories.toFloat(), maxCalories.toFloat())
            if (selectedHealthLabels.isNotEmpty()) {
                editTextHealthLabels.setText(selectedHealthLabels[0].resId)
            }
            if (selectedDietLabels.isNotEmpty()) {
                editTextDietLabels.setText(selectedDietLabels[0].resId)
            }
        }
        editTextKeyword.doOnTextChanged { text, _, _, _ ->
            discoverViewModel.updateCurrentKeyword(text)
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

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
import edu.fullerton.ecs.cpsc411.mealpal.R
import edu.fullerton.ecs.cpsc411.mealpal.databinding.FragmentSearchRecipeDialogBinding
import edu.fullerton.ecs.cpsc411.mealpal.shared.DietLabels
import edu.fullerton.ecs.cpsc411.mealpal.shared.HealthLabels
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Collections

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
        val dietLabelsAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this@SearchRecipeDialogFragment.requireContext(),
            android.R.layout.select_dialog_item,
            listOf(getString(R.string.none_selection_option)) + DietLabels.values().map { getString(it.resId) }
        )
        editTextDietLabels.apply {
            setAdapter(dietLabelsAdapter)
            inputType = InputType.TYPE_NULL
        }

        val healthLabelsAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this@SearchRecipeDialogFragment.requireContext(),
            android.R.layout.select_dialog_item,
            listOf(getString(R.string.none_selection_option)) + HealthLabels.values().map { getString(it.resId) }
        )
        editTextHealthLabels.apply {
            setAdapter(healthLabelsAdapter)
            inputType = InputType.TYPE_NULL
        }

        // TODO use if needed to store range in viewmodel(persists last entered calorie range)
//        rangeSlider.addOnChangeListener { rangeSlider, value, fromUser ->
//            Timber.i("${rangeSlider.activeThumbIndex}, $value, $fromUser")
//            // Responds to when slider's value is changed
//        }

        buttonApply.setOnClickListener {
            val minMax: Pair<Int, Int> = rangeSlider.values.let {
                Pair(Collections.min(it).toInt(), Collections.max(it).toInt())
            }
            // TODO: Build the entire query not just keyword
            editTextKeyword.text?.trim()?.let {
                if (it.isNotEmpty()) {
                    onQueryChanged(
                        UiAction.Search(
                            query = DiscoverQuery(
                                keyword = it.toString(),
                                calMin = minMax.first.toString(),
                                calThresh = minMax.second.toString()
                            )
                        )
                    )
                }
            }
            dismiss()
        }
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}

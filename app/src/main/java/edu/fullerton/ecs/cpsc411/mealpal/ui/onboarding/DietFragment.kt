package edu.fullerton.ecs.cpsc411.mealpal.ui.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import edu.fullerton.ecs.cpsc411.mealpal.R
import edu.fullerton.ecs.cpsc411.mealpal.databinding.FragmentDietBinding
import edu.fullerton.ecs.cpsc411.mealpal.shared.DietLabels

@AndroidEntryPoint
class DietFragment : Fragment() {
    private var _binding: FragmentDietBinding? = null
    private val binding get() = _binding!!
    private val onboardingViewModel: OnboardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDietBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onboardingViewModel.setProgress(screenNumber = 2)

        binding.bindUi()
    }

    private fun FragmentDietBinding.bindUi() {
        fun navigateNextFragment() {
            val action = DietFragmentDirections.actionDietFragmentToHealthFragment()
            findNavController().navigate(action)
        }
        floatingActionButton.setOnClickListener {
            onboardingViewModel.saveDietPreferences()
            navigateNextFragment()
        }
        skipDietButton.setOnClickListener {
            navigateNextFragment()
        }
        DietLabels.values().forEach { label ->
            dietChipGroup.addView(createDietChip(label))
        }
        dietChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            onboardingViewModel.updateSelectedDietLabels(checkedIds)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createDietChip(label: DietLabels): Chip {
        val chip = layoutInflater.inflate(R.layout.chip, binding.dietChipGroup, false) as Chip
        return chip.apply {
            text = getString(label.resId)
            id = label.ordinal
        }
    }
}
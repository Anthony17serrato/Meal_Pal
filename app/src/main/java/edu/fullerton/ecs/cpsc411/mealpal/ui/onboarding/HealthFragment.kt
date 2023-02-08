package edu.fullerton.ecs.cpsc411.mealpal.ui.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import edu.fullerton.ecs.cpsc411.mealpal.R
import edu.fullerton.ecs.cpsc411.mealpal.databinding.FragmentHealthBinding
import edu.fullerton.ecs.cpsc411.mealpal.shared.HealthLabels

@AndroidEntryPoint
class HealthFragment : Fragment() {
    private var _binding: FragmentHealthBinding? = null
    private val binding get() = _binding!!
    private val onboardingViewModel: OnboardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onboardingViewModel.setProgress(screenNumber = 3)
        HealthLabels.values().forEach { label ->
            binding.healthChipGroup.addView(createHealthChip(label))
        }
        binding.healthChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            onboardingViewModel.updateSelectedHealthLabels(checkedIds)
        }
        binding.healthNextFab.setOnClickListener {
            onboardingViewModel.saveHealthPreferencesAndCompleteOnboarding()
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createHealthChip(label: HealthLabels): Chip {
        val chip = layoutInflater.inflate(R.layout.chip, binding.healthChipGroup, false) as Chip
        return chip.apply {
            text = getString(label.resId)
            id = label.ordinal
        }
    }
}
package edu.fullerton.ecs.cpsc411.mealpal.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import edu.fullerton.ecs.cpsc411.mealpal.*
import edu.fullerton.ecs.cpsc411.mealpal.databinding.FragmentDiscoverBinding
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.DiscoverAdapter
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.RecipeListModelLoadStateAdapter
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.*
import edu.fullerton.ecs.cpsc411.mealpal.ui.recipedetails.RecipeDetailsActivity
import edu.fullerton.ecs.cpsc411.mealpal.utils.MEAL_URL
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class DiscoverFragment : Fragment() {
    private val discoverViewModel: DiscoverViewModel by activityViewModels {
        DiscoverViewModelFactory(
            this,
            (activity?.application as MealPalApplication).appContainer.repository
        )
    }
    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.i("Discover View created")

        binding.bindState(
            discoverUiState = discoverViewModel.discoverUiState,
            pagingData = discoverViewModel.pagingDataFlow,
            uiActions = discoverViewModel.accept
        )
    }

    private fun FragmentDiscoverBinding.bindState(
        discoverUiState: StateFlow<DiscoverUiState>,
        pagingData: Flow<PagingData<DiscoverItemUiState>>,
        uiActions: (UiAction) -> Unit
    ) {
        val discoverAdapter = DiscoverAdapter { url ->
            onClickItem(url)
        }
        discoverRecyclerview.apply {
            layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            adapter = discoverAdapter.withLoadStateFooter(
                footer = RecipeListModelLoadStateAdapter { discoverAdapter.retry() }
            )
        }

        bindList(
            discoverAdapter = discoverAdapter,
            uiState = discoverUiState,
            pagingData = pagingData,
            onScrollChanged = uiActions
        )
    }

    private fun FragmentDiscoverBinding.bindList(
        discoverAdapter: DiscoverAdapter,
        uiState: StateFlow<DiscoverUiState>,
        pagingData: Flow<PagingData<DiscoverItemUiState>>,
        onScrollChanged: (UiAction.Scroll) -> Unit
    ) {
        retryButton.setOnClickListener { discoverAdapter.retry() }
        discoverRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0) onScrollChanged(UiAction.Scroll(currentQuery = uiState.value.query))
            }
        })
        val notLoading = discoverAdapter.loadStateFlow
            // Only emit when REFRESH LoadState for the paging source changes.
            .distinctUntilChangedBy { it.source.refresh }
            // Only react to cases where REFRESH completes i.e., NotLoading.
            .map { it.source.refresh is LoadState.NotLoading }

        val hasNotScrolledForCurrentSearch = uiState
            .map { it.hasNotScrolledForCurrentSearch }
            .distinctUntilChanged()

        val shouldScrollToTop = combine(
            notLoading,
            hasNotScrolledForCurrentSearch,
            Boolean::and
        ).distinctUntilChanged()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    pagingData.collectLatest(discoverAdapter::submitData)
                }
                launch {
                    shouldScrollToTop.collect { shouldScroll ->
                        if (shouldScroll) discoverRecyclerview.scrollToPosition(0)
                    }
                }
                launch {
                    discoverAdapter.loadStateFlow.collect { loadState ->
                        val isListEmpty = loadState.refresh is LoadState.NotLoading && discoverAdapter.itemCount == 0
                        // show empty list
                        emptyList.isVisible = isListEmpty
                        // Only show the list if refresh succeeds
                        discoverRecyclerview.isVisible = !isListEmpty && loadState.source.refresh !is LoadState.Loading
                        // Show loading spinner during initial load or refresh.
                        progressBar.isVisible = loadState.source.refresh is LoadState.Loading
                        // Show the retry state if initial load or refresh fails.
                        retryButton.isVisible = loadState.source.refresh is LoadState.Error
                        // Toast on any error, regardless of whether it came from RemoteMediator or PagingSource
                        val errorState = loadState.source.append as? LoadState.Error
                            ?: loadState.source.prepend as? LoadState.Error
                            ?: loadState.append as? LoadState.Error
                            ?: loadState.prepend as? LoadState.Error
                        errorState?.let {
                            Toast.makeText(
                                this@DiscoverFragment.context,
                                "\uD83D\uDE28 Wooops ${it.error}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onClickItem(url: String) {
        Intent(context, RecipeDetailsActivity::class.java).apply {
            putExtra(MEAL_URL, url)
            startActivity(this)
        }
    }
}
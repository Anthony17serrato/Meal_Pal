package edu.fullerton.ecs.cpsc411.mealpal.ui.main.fragments

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.DiscoverViewModel
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.QuickPicks
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuickPicks(
    onSelectQuickPick: (QuickPicks) -> Unit,
    viewModel: DiscoverViewModel
) {
    val quickPicksState by viewModel.quickPicksState.collectAsState()

    val lazyListState = rememberLazyListState()

    LaunchedEffect(quickPicksState.sortedQuickPicks) {
        if (quickPicksState.sortedQuickPicks.isNotEmpty()) {
            // Check if the order of items has changed
            val hasOrderChanged = quickPicksState.sortedQuickPicks != lazyListState.layoutInfo.visibleItemsInfo
                .map { quickPicksState.sortedQuickPicks[it.index] }

            if (hasOrderChanged) {
                // Animate the items
                lazyListState.animateScrollToItem(0)

                // Wait for the animation to finish
                delay(500)

                // Scroll to the top
                lazyListState.scrollToItem(0)
            }
        }
    }

    LazyRow(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth(),
        state = lazyListState
    ) {
        items(quickPicksState.sortedQuickPicks, key = { it.ordinal }) {item ->
            QuickPickItem(
                name = item.cuisineName,
                icon = item.icon,
                isSelected = quickPicksState.selectedPick == item,
                modifier = Modifier
                    .animateItemPlacement()
                    .padding(start = 8.dp, end = 8.dp)
                    .clickable {
                        onSelectQuickPick(item)
                    }
            )
        }
    }
}

@Composable
fun QuickPickItem(
    name: String,
    icon: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val selectedColor = MaterialTheme.colorScheme.primaryContainer
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(46.dp)
                .drawBehind {
                    if (isSelected) {
                        drawCircle(selectedColor)
                    }
                }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = icon),
                contentDescription = "$name Icon",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(38.dp)
            )
        }
        Text(
            text = name,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                LocalTextStyle.current.color
            }
        )
    }
}
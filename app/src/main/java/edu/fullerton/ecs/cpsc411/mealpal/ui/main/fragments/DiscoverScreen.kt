package edu.fullerton.ecs.cpsc411.mealpal.ui.main.fragments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import edu.fullerton.ecs.cpsc411.mealpal.R
import edu.fullerton.ecs.cpsc411.mealpal.data.local.entities.RecipeWithIngredients
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.DiscoverTrendingState
import edu.fullerton.ecs.cpsc411.mealpal.ui.main.viewmodels.DiscoverViewModel
import okio.IOException
import timber.log.Timber
private const val API_LIMIT_ERROR = "HTTP 429"
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiscoverScreen(
    onClickItemDetails: (String) -> Unit,
    discoverViewModel: DiscoverViewModel
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        val lazyPagingItems = discoverViewModel.pagingDataFlow.collectAsLazyPagingItems()
        val trendingItems = discoverViewModel.trendingState.collectAsState()
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
        ) {
            item(
                span = StaggeredGridItemSpan.FullLine
            ) {
                TrendingView(trendingItems) { itemWithIngredients ->
                    if (itemWithIngredients!=null) {
                        discoverViewModel.viewItem(itemWithIngredients)
                        onClickItemDetails(itemWithIngredients.recipe.url)
                    }
                }
            }

            item(
                span = StaggeredGridItemSpan.FullLine
            ) {
                QuickPicks(
                    onSelectQuickPick = { pick -> discoverViewModel.setSelectedQuickPick(pick)},
                    viewModel = discoverViewModel
                )
            }

            when (val loadState = lazyPagingItems.loadState.refresh) {
                is LoadState.Error -> {
                    item(
                        span = StaggeredGridItemSpan.FullLine
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (loadState.error is IOException) {
                                AsyncImage(
                                    model = R.drawable.no_internet,
                                    contentDescription = stringResource(R.string.no_internet_description),
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier.fillMaxWidth(0.7f)
                                )
                                Text(text = "No Internet")
                            } else if (loadState.error.localizedMessage?.trim() == API_LIMIT_ERROR) {
                                Text(
                                    text = stringResource(id = R.string.api_limit_exceeded),
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    text = "Error: ${loadState.error.localizedMessage?.trim()}",
                                    textAlign = TextAlign.Center
                                )
                            }
                            Button(
                                onClick = {
                                    lazyPagingItems.retry()
                                    discoverViewModel.retryTrending()
                                }
                            ) {
                                Text(text = "Retry")
                            }
                        }
                    }
                }
                LoadState.Loading -> {
                    item(
                        span = StaggeredGridItemSpan.FullLine
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                                .wrapContentHeight(Alignment.CenterVertically)
                        )
                    }
                }
                is LoadState.NotLoading -> {
                    items(
                        count = lazyPagingItems.itemCount,
                        key = lazyPagingItems.itemKey { it.recipeListModel.url },
                        contentType = lazyPagingItems.itemContentType { "DiscoverItems" }
                    ) { index ->
                        val item = lazyPagingItems[index]
                        item?.let { discoverItem ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = colorResource(item.recipeInteractions.cardColor)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        discoverItem.onSelect()
                                        onClickItemDetails(discoverItem.recipeListModel.url)
                                    }
                                    .padding(start = 4.dp, end = 4.dp, bottom = 8.dp)
                            ) {
                                Column {
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(discoverItem.recipeListModel.image)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(120.dp)
                                                    .clip(shape = MaterialTheme.shapes.medium)
                                            )
                                            Text(
                                                text = discoverItem.recipeListModel.title,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(4.dp)
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp)
                                            ) {
                                                Image(
                                                    painterResource(id = R.drawable.calories),
                                                    contentDescription = stringResource(id = R.string.calories_icon),
                                                    modifier = Modifier
                                                        .width(25.dp)
                                                        .height(25.dp)
                                                )
                                                Text(
                                                    text = stringResource(
                                                        R.string.calories_indicator,
                                                        discoverItem.recipeListModel.calories.toInt().toString()
                                                    ),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.padding(start = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(item.recipeInteractions.interactionIcon),
                                            contentDescription = stringResource(R.string.interaction_icon),
                                            tint = colorResource(android.R.color.white)
                                        )
                                        Text(
                                            text = stringResource(item.recipeInteractions.interactionLabel),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = colorResource(android.R.color.white),
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            val errorState = lazyPagingItems.loadState.append as? LoadState.Error
                ?: lazyPagingItems.loadState.source.append as? LoadState.Error
            if (errorState != null) {
                item(
                    span = StaggeredGridItemSpan.FullLine
                ) {
                    when(val error = errorState.error.localizedMessage?.trim()) {
                        API_LIMIT_ERROR -> {
                            Text(
                                text = stringResource(id = R.string.api_limit_exceeded),
                                textAlign = TextAlign.Center
                            )
                        }
                        else -> {
                            Text(text = "Error: $error", textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrendingView(
    trendingItems: State<DiscoverTrendingState>,
    onClickTrendingItem: (item: RecipeWithIngredients?) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.trending_header),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
            ) {
                val buttonBackgroundColor = MaterialTheme.colorScheme.secondaryContainer
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = buttonBackgroundColor, radius = 20.dp.toPx())
                }
                IconButton(onClick = { /* doSomething() */ }) {
                    Icon(
                        ImageVector.vectorResource(R.drawable.baseline_arrow_forward_24),
                        contentDescription = stringResource(R.string.more)
                    )
                }
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (trendingItems.value.trendingRecipes.isEmpty()) {
                // Placeholder
                itemsIndexed((1..10).toList()) { index, _ ->
                    TrendingItem(
                        index = index,
                        recipeWithIngredients = null,
                        onItemClick = onClickTrendingItem
                    )
                }
            } else {
                itemsIndexed(trendingItems.value.trendingRecipes) { index, it ->
                    TrendingItem(
                        index = index,
                        recipeWithIngredients = it,
                        onItemClick = onClickTrendingItem
                    )
                }
            }
        }

        Divider(modifier = Modifier.padding(8.dp), color = colorResource(R.color.colorBorderGrey))
    }
}

@Composable
fun TrendingItem(
    index: Int,
    recipeWithIngredients: RecipeWithIngredients?,
    onItemClick: (item: RecipeWithIngredients?) -> Unit
) {
    Column(
        modifier = Modifier
            .clickable {
                onItemClick(recipeWithIngredients)
            }
            .width(275.dp)
            .padding(start = if (index == 0) 8.dp else 4.dp, end = 4.dp)
    ) {
        val loadingPainter = SolidColorPainter(Color.LightGray.copy(alpha = 0.5f))
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(recipeWithIngredients?.recipe?.image)
                .crossfade(true)
                .build(),
            contentDescription = null,
            placeholder = loadingPainter,
            fallback = loadingPainter,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(275.dp)
                .height(150.dp)
                .clip(shape = MaterialTheme.shapes.medium)
        )
        if (recipeWithIngredients?.recipe?.title == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.5f)
                    .padding(top = 4.dp, bottom = 4.dp)
                    .height(18.dp)
                    .background(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        } else {
            Text(
                text = recipeWithIngredients.recipe.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }
    }
}

class SolidColorPainter(private val color: Color) : Painter() {
    override val intrinsicSize: Size
        get() = Size.Unspecified

    override fun DrawScope.onDraw() {
        drawRect(color)
    }
}
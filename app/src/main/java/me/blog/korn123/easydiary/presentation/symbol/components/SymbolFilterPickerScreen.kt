package me.blog.korn123.easydiary.presentation.symbol.components

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.viewpager.widget.ViewPager
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.SymbolPagerAdapter
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.presentation.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.presentation.symbol.SymbolFilterPickerUiState
import me.blog.korn123.easydiary.views.SlidingTabLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymbolFilterPickerScreen(
    uiState: SymbolFilterPickerUiState,
    onRemoveSymbol: (Int) -> Unit,
    onAddSymbol: (Int) -> Unit,
    onDismissAlertDialog: () -> Unit,
    onCloseClick: () -> Unit,
    onScrollToBottomHandled: () -> Unit,
) {
    val gridState = rememberLazyGridState()
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(uiState.scrollToBottomEvent, uiState.selectedSymbols.size) {
        if (uiState.scrollToBottomEvent && uiState.selectedSymbols.isNotEmpty()) {
            gridState.animateScrollToItem(uiState.selectedSymbols.size - 1)
            onScrollToBottomHandled()
        }
    }

    Scaffold(
        topBar = {
            EasyDiaryActionBar(
                title = "Symbol Picker",
                useCloseIcon = true,
                close = onCloseClick,
            )
        },
        containerColor = Color(context.config.backgroundColor),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Selected Symbol List (Top Grid)
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                state = gridState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp),
            ) {
                items(uiState.selectedSymbols) { sequence ->
                    SelectedSymbolItem(
                        sequence = sequence,
                        onRemoveClick = { onRemoveSymbol(sequence) },
                    )
                }
            }

            // Bottom Symbol Picker ViewPager (using SymbolPagerAdapter like openFeelingSymbolDialog)
            if (activity != null && uiState.categories.isNotEmpty()) {
                val itemList = remember(uiState.categories) {
                    ArrayList(uiState.categories.map { cat -> cat.symbols.map { it.info }.toTypedArray() })
                }
                val categoryList = remember(uiState.categories) {
                    uiState.categories.map { cat -> cat.categoryName }
                }

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    factory = { ctx ->
                        val inflater = LayoutInflater.from(ctx)
                        val view = inflater.inflate(R.layout.dialog_feeling_pager, null)
                        // Hide guide/close header since TopBar is already used
                        view.findViewById<View>(R.id.diarySymbolGuide)?.parent?.let {
                            (it as? View)?.visibility = View.GONE
                        }

                        val viewPager = view.findViewById<ViewPager>(R.id.viewpager).apply {
                            setBackgroundColor(ctx.config.backgroundColor)
                        }
                        val slidingTabLayout = view.findViewById<SlidingTabLayout>(R.id.sliding_tabs).apply {
                            setBackgroundColor(ctx.config.backgroundColor)
                        }

                        val adapter = SymbolPagerAdapter(
                            activity = activity,
                            items = itemList,
                            categories = categoryList,
                            selectedSymbolSequence = 0,
                            callback = { symbolSequence ->
                                onAddSymbol(symbolSequence)
                            },
                        )
                        viewPager.adapter = adapter
                        slidingTabLayout.setViewPager(viewPager)
                        view
                    },
                )
            }
        }
    }

    uiState.alertDialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = onDismissAlertDialog,
            confirmButton = {
                TextButton(onClick = onDismissAlertDialog) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            },
            text = {
                Text(text = message)
            },
        )
    }
}

@Composable
fun SelectedSymbolItem(
    sequence: Int,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .padding(dimensionResource(id = R.dimen.component_margin_small))
            .clickable { onRemoveClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(
                factory = { ctx ->
                    ImageView(ctx).apply {
                        scaleType = ImageView.ScaleType.FIT_XY
                        adjustViewBounds = true
                    }
                },
                update = { imageView ->
                    FlavorUtils.initWeatherView(
                        imageView.context,
                        imageView,
                        sequence,
                        false,
                    )
                },
                modifier = Modifier.size(50.dp),
            )
            Image(
                painter = painterResource(id = R.drawable.ic_delete),
                contentDescription = "Remove symbol",
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.TopEnd),
            )
        }
    }
}

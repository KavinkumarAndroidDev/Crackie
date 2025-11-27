package com.kkdev.crackie.ui.favorites

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kkdev.crackie.db.Fortune
import com.kkdev.crackie.db.FortuneRarity
import com.kkdev.crackie.ui.home.AnimatedTiledBackground
import com.kkdev.crackie.ui.home.HomeViewModel
import com.kkdev.crackie.ui.home.SortBy
import com.kkdev.crackie.ui.home.SortOrder
import com.kkdev.crackie.ui.theme.GoldenGlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val favoriteFortunes by viewModel.favoriteFortunes.collectAsState(initial = emptyList())
    val sortBy by viewModel.sortBy.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            AnimatedTiledBackground()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("My Saved Fortunes") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            containerColor = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SortOptions(sortBy = sortBy, sortOrder = sortOrder, onSortByChange = { viewModel.setSortBy(it) }, onSortOrderChange = { viewModel.setSortOrder(it) })
                if (favoriteFortunes.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "You haven\'t saved any fortunes yet!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(favoriteFortunes) { fortune ->
                            FortuneListItem(fortune = fortune)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortOptions(
    sortBy: SortBy,
    sortOrder: SortOrder,
    onSortByChange: (SortBy) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Sort by",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        val chipColors = FilterChipDefaults.filterChipColors(
            containerColor = Color.Transparent,
            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            selectedLabelColor = MaterialTheme.colorScheme.primary
        )

        FilterChip(
            selected = sortBy == SortBy.DATE,
            onClick = { onSortByChange(SortBy.DATE) },
            label = { Text("Date") },
            colors = chipColors,
            border = BorderStroke(
                width = 1.dp,
                color = if (sortBy == SortBy.DATE) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                }
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        FilterChip(
            selected = sortBy == SortBy.RARITY,
            onClick = { onSortByChange(SortBy.RARITY) },
            label = { Text("Rarity") },
            colors = chipColors,
            border = BorderStroke(
                width = 1.dp,
                color = if (sortBy == SortBy.RARITY) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                }
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = {
            val newOrder = if (sortOrder == SortOrder.ASC) SortOrder.DESC else SortOrder.ASC
            onSortOrderChange(newOrder)
        }) {
            val sortOrderText = when (sortBy) {
                SortBy.DATE -> if (sortOrder == SortOrder.DESC) "Newest" else "Oldest"
                SortBy.RARITY -> if (sortOrder == SortOrder.DESC) "Rarest" else "Common"
            }
            Text(sortOrderText)
            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
            Icon(
                imageVector = if (sortOrder == SortOrder.DESC) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                contentDescription = "Sort order is ${if (sortOrder == SortOrder.DESC) "Descending" else "Ascending"}"
            )
        }
    }
}

@Composable
fun FortuneListItem(fortune: Fortune) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fortune.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = fortune.rarity.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = when (fortune.rarity) {
                        FortuneRarity.MYSTIC -> Color(0xFFD0BCFF)
                        FortuneRarity.GOLDEN -> GoldenGlow
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }
                )
            }
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Favorite",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}
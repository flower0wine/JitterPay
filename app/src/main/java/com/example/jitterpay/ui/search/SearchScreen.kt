package com.example.jitterpay.ui.search

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jitterpay.ui.animation.AnimationConstants
import com.example.jitterpay.ui.components.search.FilterChips
import com.example.jitterpay.ui.components.search.SearchBar
import com.example.jitterpay.ui.components.search.SearchResults

@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedDateRange by viewModel.selectedDateRange.collectAsState()
    var showFilters by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            SearchTopBar(
                onNavigateBack = onNavigateBack,
                onFilterClick = { showFilters = !showFilters }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = showFilters,
                enter = expandVertically(
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.SHORT,
                        easing = AnimationConstants.Easing.Exit
                    )
                ) + fadeOut()
            ) {
                FilterChips(
                    selectedType = selectedType,
                    onTypeSelected = { viewModel.updateSelectedType(it) },
                    selectedDateRange = selectedDateRange,
                    onDateRangeSelected = { viewModel.updateSelectedDateRange(it) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SearchResults(
                transactions = uiState.filteredTransactions,
                searchQuery = searchQuery,
                selectedType = selectedType,
                selectedDateRange = selectedDateRange,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    onNavigateBack: () -> Unit,
    onFilterClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "SEARCH",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = onFilterClick) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filters",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black
        )
    )
}

package com.proyek.foolens.ui.allergens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.proyek.foolens.R
import com.proyek.foolens.domain.model.Allergen
import com.proyek.foolens.domain.model.UserAllergen
import com.proyek.foolens.ui.component.AllergenItem
import com.proyek.foolens.ui.component.AllergenItemExpandable
import com.proyek.foolens.ui.component.EmptyAllergenState
import com.proyek.foolens.ui.theme.Typography

@Composable
fun AllergensScreen(
    viewModel: AllergensViewModel = hiltViewModel(),
    onNavigateToAddAllergen: () -> Unit = {},
    onNavigateToAllergenDetail: (UserAllergen) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val myBlack = Color(0xFF062207)

    // Load common allergens when the tab is selected
    LaunchedEffect(state.selectedTab) {
        if (state.selectedTab == AllergensState.Tab.COMMON && state.commonAllergens.isEmpty()) {
            viewModel.loadCommonAllergens()
        }
    }

    Scaffold(
        floatingActionButton = {
            if (state.selectedTab == AllergensState.Tab.PERSONAL) {
                FloatingActionButton(
                    onClick = onNavigateToAddAllergen,
                    containerColor = myBlack,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, "Add Allergen")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            // Screen header
            Text(
                text = "Allergens",
                style = Typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 24.dp)
            )

            // Custom tab implementation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Personal Tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(36.dp))
                        .background(
                            if (state.selectedTab == AllergensState.Tab.PERSONAL)
                                myBlack
                            else
                                Color.Transparent
                        )
                        .clickable { viewModel.setSelectedTab(AllergensState.Tab.PERSONAL) }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Personal",
                        style = Typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (state.selectedTab == AllergensState.Tab.PERSONAL)
                            Color.White
                        else
                            Color(0xFFA3A4A3),
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Common Tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(36.dp))
                        .background(
                            if (state.selectedTab == AllergensState.Tab.COMMON)
                                myBlack
                            else
                                Color.Transparent
                        )
                        .clickable { viewModel.setSelectedTab(AllergensState.Tab.COMMON) }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Common",
                        style = Typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (state.selectedTab == AllergensState.Tab.COMMON)
                            Color.White
                        else
                            Color(0xFFA3A4A3),
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content based on selected tab
            when (state.selectedTab) {
                AllergensState.Tab.PERSONAL -> {
                    PersonalAllergensContent(
                        isLoading = state.isLoading,
                        isRefreshing = state.isRefreshing,
                        allergens = state.userAllergens,
                        errorMessage = state.errorMessage,
                        onRefresh = { viewModel.refreshAllergens() },
                        onAllergenClick = onNavigateToAllergenDetail
                    )
                }
                AllergensState.Tab.COMMON -> {
                    CommonAllergensContent(
                        allergens = state.commonAllergens,
                        isLoading = state.isLoadingCommon,
                        errorMessage = state.commonErrorMessage,
                        onSearch = { viewModel.searchCommonAllergens(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun PersonalAllergensContent(
    isLoading: Boolean,
    isRefreshing: Boolean,
    allergens: List<UserAllergen>,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onAllergenClick: (UserAllergen) -> Unit
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = onRefresh
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading && !isRefreshing && allergens.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF2B6247)
                )
            } else if (errorMessage != null && allergens.isEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else if (allergens.isEmpty()) {
                // Empty state
                EmptyAllergenState(
                    message = "Anda belum menambahkan alergen"
                )
            } else {
                // List of allergens
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(allergens) { allergen ->
                        AllergenItem(
                            allergen = allergen,
                            onClick = onAllergenClick
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    // Add extra space at the bottom for FAB
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonAllergensContent(
    allergens: List<Allergen>,
    isLoading: Boolean,
    errorMessage: String?,
    onSearch: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Remember expanded state for each allergen
    val expandedItems = remember { mutableStateOf(setOf<Int>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Search bar
        TextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                onSearch(it)
            },
            placeholder = { Text("Search...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFEDEDED),
                focusedContainerColor = Color(0xFFEDEDED),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.Black
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2B6247))
            }
        } else if (errorMessage != null && allergens.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
            }
        } else if (allergens.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No allergens found",
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Allergens list with expandable items
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(allergens) { allergen ->
                    val isExpanded = expandedItems.value.contains(allergen.id)

                    CommonAllergenItem(
                        allergen = allergen,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedItems.value = if (isExpanded) {
                                expandedItems.value - allergen.id
                            } else {
                                expandedItems.value + allergen.id
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Add extra space at the bottom
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
fun CommonAllergenItem(
    allergen: Allergen,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val allergenForDisplay = UserAllergen(
        id = allergen.id,
        name = allergen.name,
        description = allergen.description,
        alternativeNames = allergen.alternativeNames,
        category = com.proyek.foolens.domain.model.AllergenCategory(
            id = 0,
            name = "",
            icon = null
        ),
        severityLevel = 0,
        notes = null,
        createdAt = "",
        updatedAt = ""
    )

    // Use the existing AllergenItemExpandable component
    AllergenItemExpandable(
        allergen = allergenForDisplay,
        isExpanded = isExpanded,
        onClick = onToggleExpand
    )
}
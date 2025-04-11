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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.proyek.foolens.domain.model.UserAllergen
import com.proyek.foolens.ui.component.AllergenItem
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
                    CommonAllergensContent()
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

@Composable
fun CommonAllergensContent() {
    // For now, just showing a placeholder
    Text(
        text = "Common allergens will appear here",
        color = Color.Gray,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

//package com.proyek.foolens.ui.allergens
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.FloatingActionButton
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Tab
//import androidx.compose.material3.TabRow
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.google.accompanist.swiperefresh.SwipeRefresh
//import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
//import com.proyek.foolens.R
//import com.proyek.foolens.domain.model.UserAllergen
//import com.proyek.foolens.ui.component.AllergenItem
//import com.proyek.foolens.ui.component.EmptyAllergenState
//
//@Composable
//fun AllergensScreen(
//    viewModel: AllergensViewModel = hiltViewModel(),
//    onNavigateToAddAllergen: () -> Unit = {},
//    onNavigateToAllergenDetail: (UserAllergen) -> Unit = {}
//) {
//    val state by viewModel.state.collectAsState()
//
//    Scaffold(
//        floatingActionButton = {
//            if (state.selectedTab == AllergensState.Tab.PERSONAL) {
//                FloatingActionButton(
//                    onClick = onNavigateToAddAllergen,
//                    containerColor = Color(0xFF0D5622),
//                    contentColor = Color.White
//                ) {
//                    Icon(Icons.Filled.Add, "Add Allergen")
//                }
//            }
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//        ) {
//            // Screen header
//            Text(
//                text = "Allergens",
//                fontSize = 24.sp,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(16.dp)
//            )
//
//            // Tabs
//            TabRow(
//                selectedTabIndex = if (state.selectedTab == AllergensState.Tab.PERSONAL) 0 else 1,
//                containerColor = Color.Transparent,
//                contentColor = Color(0xFF0D5622),
//                divider = { Spacer(modifier = Modifier.height(1.dp)) }
//            ) {
//                Tab(
//                    selected = state.selectedTab == AllergensState.Tab.PERSONAL,
//                    onClick = { viewModel.setSelectedTab(AllergensState.Tab.PERSONAL) },
//                    text = {
//                        Text(
//                            text = "Personal",
//                            color = if (state.selectedTab == AllergensState.Tab.PERSONAL)
//                                Color(0xFF0D5622) else Color.Gray,
//                            fontWeight = if (state.selectedTab == AllergensState.Tab.PERSONAL)
//                                FontWeight.Bold else FontWeight.Normal
//                        )
//                    },
//                    modifier = Modifier.background(
//                        color = if (state.selectedTab == AllergensState.Tab.PERSONAL)
//                            Color(0xFF0D5622).copy(alpha = 0.1f) else Color.Transparent,
//                        shape = RoundedCornerShape(16.dp)
//                    ).padding(4.dp)
//                )
//
//                Tab(
//                    selected = state.selectedTab == AllergensState.Tab.COMMON,
//                    onClick = { viewModel.setSelectedTab(AllergensState.Tab.COMMON) },
//                    text = {
//                        Text(
//                            text = "Common",
//                            color = if (state.selectedTab == AllergensState.Tab.COMMON)
//                                Color(0xFF0D5622) else Color.Gray,
//                            fontWeight = if (state.selectedTab == AllergensState.Tab.COMMON)
//                                FontWeight.Bold else FontWeight.Normal
//                        )
//                    },
//                    modifier = Modifier.background(
//                        color = if (state.selectedTab == AllergensState.Tab.COMMON)
//                            Color(0xFF0D5622).copy(alpha = 0.1f) else Color.Transparent,
//                        shape = RoundedCornerShape(16.dp)
//                    ).padding(4.dp)
//                )
//            }
//
//            // Content based on selected tab
//            when (state.selectedTab) {
//                AllergensState.Tab.PERSONAL -> {
//                    PersonalAllergensContent(
//                        isLoading = state.isLoading,
//                        isRefreshing = state.isRefreshing,
//                        allergens = state.userAllergens,
//                        errorMessage = state.errorMessage,
//                        onRefresh = { viewModel.refreshAllergens() },
//                        onAllergenClick = onNavigateToAllergenDetail
//                    )
//                }
//                AllergensState.Tab.COMMON -> {
//                    CommonAllergensContent()
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun PersonalAllergensContent(
//    isLoading: Boolean,
//    isRefreshing: Boolean,
//    allergens: List<UserAllergen>,
//    errorMessage: String?,
//    onRefresh: () -> Unit,
//    onAllergenClick: (UserAllergen) -> Unit
//) {
//    SwipeRefresh(
//        state = rememberSwipeRefreshState(isRefreshing),
//        onRefresh = onRefresh
//    ) {
//        Box(modifier = Modifier.fillMaxSize()) {
//            if (isLoading && !isRefreshing && allergens.isEmpty()) {
//                CircularProgressIndicator(
//                    modifier = Modifier.align(Alignment.Center),
//                    color = Color(0xFF0D5622)
//                )
//            } else if (errorMessage != null && allergens.isEmpty()) {
//                Column(
//                    modifier = Modifier
//                        .align(Alignment.Center)
//                        .padding(16.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Text(
//                        text = errorMessage,
//                        color = Color.Red,
//                        textAlign = TextAlign.Center
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Button(
//                        onClick = onRefresh,
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFF0D5622)
//                        )
//                    ) {
//                        Text("Coba Lagi")
//                    }
//                }
//            } else if (allergens.isEmpty()) {
//                // Empty state
//                EmptyAllergenState(
//                    message = "Anda belum menambahkan alergen"
//                )
//            } else {
//                // List of allergens
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(horizontal = 16.dp)
//                ) {
//                    items(allergens) { allergen ->
//                        AllergenItem(
//                            allergen = allergen,
//                            onClick = onAllergenClick
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun CommonAllergensContent() {
//    // Ini adalah contoh implementasi tab Common Allergens
//    // Nantinya bisa diimplementasikan dengan data statis atau dari API
//
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(horizontal = 16.dp)
//    ) {
//        item {
//            // Search bar (dummy)
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 8.dp)
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(Color.LightGray.copy(alpha = 0.3f))
//                    .padding(12.dp)
//            ) {
//                Text(
//                    text = "Search...",
//                    color = Color.Gray,
//                    fontSize = 14.sp
//                )
//            }
//        }
//
//        // List of common allergens (dummy data)
//        item {
//            CommonAllergenItem(
//                name = "Gluten",
//                isExpanded = true,
//                description = "Lorem ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text."
//            )
//        }
//
//        item { CommonAllergenItem(name = "Fish", isExpanded = false) }
//        item { CommonAllergenItem(name = "Eggs", isExpanded = false) }
//        item { CommonAllergenItem(name = "Milk", isExpanded = false) }
//        item { CommonAllergenItem(name = "Soya", isExpanded = false) }
//    }
//}
//
//@Composable
//fun CommonAllergenItem(
//    name: String,
//    isExpanded: Boolean = false,
//    description: String? = null
//) {
//    var expanded by remember { mutableStateOf(isExpanded) }
//
//    Surface(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp),
//        color = Color.White,
//        shape = RoundedCornerShape(8.dp),
//        shadowElevation = 2.dp
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = name,
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.Medium,
//                    color = Color.Black,
//                    modifier = Modifier.weight(1f)
//                )
//
//                // Expand/collapse icon
//                Box(
//                    modifier = Modifier
//                        .size(24.dp)
//                        .clip(CircleShape)
//                        .background(
//                            if (expanded) Color(0xFF0D5622).copy(alpha = 0.1f)
//                            else Color.Transparent
//                        )
//                        .padding(4.dp)
//                        .align(Alignment.CenterVertically),
//                    contentAlignment = Alignment.Center
//                ) {
//                    // Dummy chevron icon (replace with actual icon)
//                    Text(
//                        text = if (expanded) "▼" else "▶",
//                        fontSize = 12.sp,
//                        color = if (expanded) Color(0xFF0D5622) else Color.Gray
//                    )
//                }
//            }
//
//            // Expanded content
//            if (expanded && description != null) {
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Text(
//                    text = description,
//                    fontSize = 14.sp,
//                    color = Color.DarkGray
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                // Alternatives section
//                Text(
//                    text = "Alternatives",
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.Medium,
//                    color = Color.Gray
//                )
//
//                Spacer(modifier = Modifier.height(4.dp))
//
//                // Example alternatives
//                Row {
//                    AlternativeChip(text = "Cereal")
//                    Spacer(modifier = Modifier.width(4.dp))
//                    AlternativeChip(text = "Gandum")
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun AlternativeChip(text: String) {
//    Box(
//        modifier = Modifier
//            .clip(RoundedCornerShape(4.dp))
//            .background(Color(0xFF0D5622).copy(alpha = 0.1f))
//            .padding(horizontal = 8.dp, vertical = 4.dp)
//    ) {
//        Text(
//            text = text,
//            fontSize = 12.sp,
//            color = Color(0xFF0D5622)
//        )
//    }
//}
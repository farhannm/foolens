package com.proyek.foolens.ui.allergens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
    onNavigateToAllergenDetail: (UserAllergen) -> Unit = {},
    onNavigateToGuide: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val myBlack = Color(0xFF062207)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddAllergen,
                containerColor = myBlack,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, "Add Allergen")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Personal\nAllergens",
                    style = Typography.headlineMedium,
                    fontWeight = FontWeight.Medium
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF062207))
                        .clickable { onNavigateToGuide() }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Help ?",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }


            PersonalAllergensContent(
                isLoading = state.isLoading,
                isRefreshing = state.isRefreshing,
                allergens = state.userAllergens,
                errorMessage = state.errorMessage,
                onRefresh = { viewModel.refreshAllergens() },
                onAllergenClick = onNavigateToAllergenDetail
            )
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
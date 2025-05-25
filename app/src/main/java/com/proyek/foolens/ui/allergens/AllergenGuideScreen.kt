package com.proyek.foolens.ui.allergens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proyek.foolens.domain.model.Allergen
import com.proyek.foolens.domain.model.UserAllergen
import com.proyek.foolens.ui.component.AllergenItemExpandable
import com.proyek.foolens.ui.theme.Typography

@Composable
fun AllergenGuideScreen(
    viewModel: AllergensViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        if (state.commonAllergens.isEmpty()) {
            viewModel.loadCommonAllergens()
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
//                Icon(
//                    imageVector = Icons.Default.ArrowBack,
//                    contentDescription = "Back",
//                    tint = Color.Black,
//                    modifier = Modifier
//                        .size(24.dp)
//                        .clickable { onBack() }
//                )
//                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Allergen\nGuide",
                    style = Typography.headlineMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    ) { innerPadding ->
        AllergenGuideContent(
            allergens = state.commonAllergens,
            isLoading = state.isLoadingCommon,
            errorMessage = state.commonErrorMessage,
            onSearch = { viewModel.searchCommonAllergens(it) },
            modifier = Modifier.padding(innerPadding)
        )
    }
}


@Composable
fun AllergenGuideContent(
    allergens: List<Allergen>,
    isLoading: Boolean,
    errorMessage: String?,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val expandedItems = remember { mutableStateOf(setOf<Int>()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        TextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                onSearch(it)
            },
            placeholder = { Text("Search...") },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(100.dp)),
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

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2B6247))
                }
            }
            errorMessage != null && allergens.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = errorMessage, color = Color.Red, textAlign = TextAlign.Center)
                }
            }
            allergens.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No allergens found", color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(allergens) { allergen ->
                        val isExpanded = expandedItems.value.contains(allergen.id)
                        CommonAllergenItem(
                            allergen = allergen,
                            isExpanded = isExpanded,
                            onToggleExpand = {
                                expandedItems.value = if (isExpanded)
                                    expandedItems.value - allergen.id
                                else
                                    expandedItems.value + allergen.id
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
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
            id = 0, name = "", icon = null
        ),
        severityLevel = 0,
        notes = null,
        createdAt = "",
        updatedAt = ""
    )

    AllergenItemExpandable(
        allergen = allergenForDisplay,
        isExpanded = isExpanded,
        onClick = onToggleExpand
    )
}

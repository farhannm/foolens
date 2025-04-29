package com.proyek.foolens.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyek.foolens.R
import com.proyek.foolens.domain.model.UserAllergen

@Composable
fun AllergenItem(
    allergen: UserAllergen,
    onClick: (UserAllergen) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick(allergen) },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gunakan nama alergen
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = allergen.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }

            // Ikon panah ke kanan
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_right),
                contentDescription = "Lihat detail",
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun AllergenItemExpandable(
    allergen: UserAllergen,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    // Create a transition that tracks the isExpanded state
    val transition = updateTransition(targetState = isExpanded, label = "expandTransition")

    // Animate the rotation of the chevron icon
    val iconRotation by transition.animateFloat(
        label = "iconRotation",
        transitionSpec = { tween(durationMillis = 300) }
    ) { expanded ->
        if (expanded) 180f else 0f
    }

    // Create AnimatedVisibility state for content
    val visibleState = remember {
        MutableTransitionState(false).apply {
            targetState = isExpanded
        }
    }

    // Update the target state when isExpanded changes
    visibleState.targetState = isExpanded

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header row with allergen name and animate chevron icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = allergen.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                // Animated rotation for the chevron icon
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_down),
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(iconRotation)
                )
            }

            // Animated visibility for expanded content
            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(animationSpec = tween(300)) +
                        expandVertically(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300)) +
                        shrinkVertically(animationSpec = tween(300))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    // Description if available
                    allergen.description?.let { description ->
                        Text(
                            text = description,
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Only show category if name is not empty
                    if (allergen.category.name.isNotEmpty()) {
                        Text(
                            text = "Category: ${allergen.category.name}",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Alternatives section
                    Text(
                        text = "Alternatives",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 10.dp),
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Display alternatives
                    if (allergen.alternativeNames != null && allergen.alternativeNames.isNotEmpty()) {
                        Text(
                            text = allergen.alternativeNames ?: "Tidak ada nama alternatif",
                            fontSize = 14.sp,
                            color = Color.Blue,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Italic
                        )
                    } else {
                        // Default for Gluten
                        Text(
                            text = "No known alternatives",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AllergenCategoryHeader(
    categoryName: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Text(
            text = categoryName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun EmptyAllergenState(
    message: String = "Anda belum menambahkan alergen"
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.empty_state),
                contentDescription = "Empty state",
                modifier = Modifier.size(100.dp),
                tint = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                fontSize = 16.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tambahkan alergi Anda untuk membantu deteksi bahan berbahaya",
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
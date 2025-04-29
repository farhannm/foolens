package com.proyek.foolens.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SeveritySelectionBottomSheet(
    currentSeverity: Int,
    onDismiss: () -> Unit,
    onSeveritySelected: (Int) -> Unit
) {
    var severity by remember { mutableStateOf(currentSeverity) }
    val scope = rememberCoroutineScope()

    // Get color based on severity level - using the blue/purple color from the screenshot
    val severityColor = Color(0xFF6C63FF)

    // Animatable for smooth sliding
    val bottomSheetOffsetY = remember { Animatable(1f) }

    // Height of the bottom sheet
    val bottomSheetHeight = 350.dp
    val bottomSheetHeightPx = with(LocalDensity.current) { bottomSheetHeight.toPx() }

    // Function to handle bottom sheet dismissal
    val dismissBottomSheet: () -> Unit = {
        scope.launch {
            bottomSheetOffsetY.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            onDismiss()
        }
    }

    // Animate bottom sheet in when first displayed
    LaunchedEffect(Unit) {
        bottomSheetOffsetY.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    // Handle back button press
    BackHandler {
        dismissBottomSheet()
    }

    // Draggable bottom sheet for smooth interaction
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { dismissBottomSheet() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .offset {
                    // Convert animatable value to pixel offset
                    IntOffset(
                        x = 0,
                        y = (bottomSheetOffsetY.value * bottomSheetHeightPx).roundToInt()
                    )
                }
                .draggable(
                    state = rememberDraggableState { delta ->
                        // Allow dragging down to dismiss
                        scope.launch {
                            val newOffset = bottomSheetOffsetY.value + (delta / bottomSheetHeightPx)
                            if (newOffset in 0f..1f) {
                                bottomSheetOffsetY.snapTo(newOffset)
                            }

                            // Dismiss if dragged more than 50%
                            if (newOffset > 0.5f) {
                                dismissBottomSheet()
                            }
                        }
                    },
                    orientation = Orientation.Vertical
                )
                .fillMaxWidth()
                .height(bottomSheetHeight)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
                .clickable(enabled = false) { /* Prevent click through */ }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close button (X) - Top Right
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(
                        onClick = { dismissBottomSheet() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Title - Severity dengan teks yang lebih kecil
                Text(
                    text = "Severity",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    textAlign = TextAlign.Center
                )

                // Slider dengan padding yang dikurangi
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Slider(
                        value = severity.toFloat(),
                        onValueChange = { severity = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = severityColor,
                            activeTrackColor = severityColor,
                            inactiveTrackColor = Color.LightGray.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Severity numbers - dengan spacing yang dikurangi
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Just text numbers without circle backgrounds
                    for (i in 1..5) {
                        Text(
                            text = i.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (i <= severity) severityColor else Color.Gray.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Save button - dengan padding yang dikurangi
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    OutlinedButton(
                        onClick = {
                            onSeveritySelected(severity)
                            dismissBottomSheet()
                        },
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Save",
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}
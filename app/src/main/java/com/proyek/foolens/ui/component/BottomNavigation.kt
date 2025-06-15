package com.proyek.foolens.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.proyek.foolens.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow

enum class BottomNavItem(
    val route: String,
    val activeIconResId: Int,
    val inactiveIconResId: Int,
    val contentDescription: String
) {
    Home(
        "home",
        R.drawable.ic_home_active,
        R.drawable.ic_home_inactive,
        "Home"
    ),
    Allergens(
        "allergens",
        R.drawable.ic_allergens_active,
        R.drawable.ic_allergens_inactive,
        "Allergens"
    )
}

@Composable
fun FoolensBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onCameraClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Black line at the top of the navigation bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Transparent)
                .align(Alignment.TopCenter)
        )

        // Main navigation bar
        Box (
            modifier = Modifier
                .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp),
                clip = false
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
                    .background(Color.White)
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home navigation item
                BottomNavItem.values().forEach { navItem ->
                    if (navItem == BottomNavItem.Home) {
                        NavItem(
                            selected = currentRoute == navItem.route,
                            iconResId = if (currentRoute == navItem.route)
                                navItem.activeIconResId
                            else
                                navItem.inactiveIconResId,
                            contentDescription = navItem.contentDescription,
                            onClick = { onNavigate(navItem.route) }
                        )
                    }
                }

                // Center camera button
                CameraButton(onClick = onCameraClick)

                // Allergens navigation item
                BottomNavItem.values().forEach { navItem ->
                    if (navItem == BottomNavItem.Allergens) {
                        NavItem(
                            selected = currentRoute == navItem.route,
                            iconResId = if (currentRoute == navItem.route)
                                navItem.activeIconResId
                            else
                                navItem.inactiveIconResId,
                            contentDescription = navItem.contentDescription,
                            onClick = { onNavigate(navItem.route) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NavItem(
    selected: Boolean,
    iconResId: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // We keep a consistent space at the top
        Spacer(modifier = Modifier.height(4.dp))

        // Icon in the middle
        IconButton(
            onClick = onClick
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = contentDescription,
                tint = Color.Unspecified, // No tint, use the original image colors
                modifier = Modifier.size(24.dp)
            )
        }

        // Indicator dot at the bottom or empty space to maintain layout
        if (selected) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(Color.Black, CircleShape)
            )
        } else {
            // Empty space with the same height as the dot to prevent layout shifts
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun CameraButton(onClick: () -> Unit) {
    Image(
        painter = painterResource(id = R.drawable.ic_camera),
        contentDescription = "Camera",
        modifier = Modifier
            .size(46.dp)
            .clickable(onClick = onClick)
    )
}
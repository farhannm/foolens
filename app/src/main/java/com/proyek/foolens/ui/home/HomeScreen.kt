package com.proyek.foolens.ui.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.proyek.foolens.R
import com.proyek.foolens.ui.component.ConfirmationDialog
import com.proyek.foolens.ui.theme.Typography
import com.proyek.foolens.data.util.ImageUtils
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onProfileClick: () -> Unit,
    onHistoryClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Track if logout dialog is showing
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Get current date and time
    val currentDate = remember { LocalDate.now() }
    val formattedDate = remember {
        val dayOfWeek = when (currentDate.dayOfWeek) {
            DayOfWeek.MONDAY -> "Monday"
            DayOfWeek.TUESDAY -> "Tuesday"
            DayOfWeek.WEDNESDAY -> "Wednesday"
            DayOfWeek.THURSDAY -> "Thursday"
            DayOfWeek.FRIDAY -> "Friday"
            DayOfWeek.SATURDAY -> "Saturday"
            DayOfWeek.SUNDAY -> "Sunday"
            else -> ""
        }
        val month = when (currentDate.monthValue) {
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Apr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Aug"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Dec"
            else -> ""
        }
        "$dayOfWeek, $month ${currentDate.dayOfMonth}"
    }

    // Show error message as snackbar if present
    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) {
            scope.launch {
                snackbarHostState.showSnackbar(state.errorMessage ?: "An error occurred")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                state.user != null -> {
                    HomeContent(
                        state = state,
                        formattedDate = formattedDate,
                        onRefresh = { viewModel.loadUserData() },
                        onLogout = { showLogoutDialog = true },
                        onProfileClick = onProfileClick,
                        onHistoryClick = onHistoryClick,
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.errorMessage ?: "No user data available",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.loadUserData() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC7F131)
                            )
                        ) {
                            Text(
                                text = "Refresh",
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showLogoutDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red
                            )
                        ) {
                            Text(
                                text = "Logout",
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        ConfirmationDialog(
            title = "Keluar",
            message = "Apakah Anda yakin untuk keluar dari akun?",
            icon = painterResource(id = R.drawable.ilustration_sticker),
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            }
        )
    }
}

@Composable
fun HomeContent(
    state: HomeState,
    formattedDate: String,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val imageLoader = remember { ImageUtils.createProfileImageLoader(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Top bar with date and logout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formattedDate,
                style = Typography.bodySmall,
                color = Color.Gray
            )

            Row {
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.Gray
                    )
                }
            }
        }

        // Row for greeting and profile image
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Column for greeting text
            Column {
                Text(
                    text = "Hallo,",
                    style = Typography.headlineSmall,
                    fontWeight = FontWeight.Normal
                )

                Text(
                    text = state.user?.name ?: "",
                    style = Typography.headlineMedium,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = state.user?.email ?: "",
                    style = Typography.bodyMedium,
                    color = Color.Gray
                )
            }

            // Profile image (clickable to navigate to profile)
            Box(
                modifier = Modifier
                    .clickable(onClick = onProfileClick)
            ) {
                if (state.user?.profilePicture != null && state.user.profilePicture.isNotEmpty()) {
                    AsyncImage(
                        model = ImageUtils.createProfileImageRequest(
                            context,
                            state.user.profilePicture,
                            R.drawable.profile_image
                        ),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        imageLoader = imageLoader
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.profile_image),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TotalScannedProductCard(
            totalScanned = state.scanCount?.totalCount ?: 0,
            safeCount = state.productSafetyStats?.safeCount ?: 0,
            unsafeCount = state.productSafetyStats?.unsafeCount ?: 0,
            safePercentage = state.productSafetyStats?.safePercentage ?: 0.0,
            newScanned = state.scanCount?.todayCount ?: 0
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column{
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Recently Scanned",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "See All",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Blue,
                    modifier = Modifier
                        .clickable(onClick = onHistoryClick)
                )
            }

            Spacer(Modifier.height(16.dp))

            HistoryScan(name = "Oreo", allergens = "Milk", onClick = { println("Click") })
            HistoryScan(name = "Indomie", allergens = "Gluten, Soy", onClick = { println("Click") })
            HistoryScan(name = "SilverQueen", allergens = "Milk, Almond", onClick = { println("Click") })
        }
    }
}

@Composable
fun TotalScannedProductCard(
    totalScanned: Int,
    safeCount: Int,
    unsafeCount: Int,
    safePercentage: Double,
    newScanned: Int
) {
    // Gunakan safePercentage dari response (0.0–100.0), konversi ke 0.0–1.0
    val progress = if (safePercentage > 0) (safePercentage / 100).toFloat() else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Scanned Product",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = Color(0xFF666666),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "$totalScanned",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 68.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.padding(horizontal = 1.dp))
                Text(
                    text = "items",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = Color.Black,
                    fontWeight = FontWeight.Medium,
                )
            }

            Text(
                text = "Lest Compare!",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = Color(0xFF062207),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.Start)
                    .padding(bottom = 16.dp),
                fontWeight = FontWeight.SemiBold
            )

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(100.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(Color(0xFF4169E1), RoundedCornerShape(100.dp))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Safe Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = "Safe",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.Start)
                            .padding(bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(2.dp)
                            .background(Color(0xFF4169E1))
                            .align(Alignment.Start)
                            .padding(bottom = 8.dp)
                    )
                    Text(
                        text = "$safeCount product",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 16.sp,
                            color = Color(0xFFAAAAAA)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.Start)
                            .padding(top = 8.dp)
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(64.dp)
                        .background(Color(0xFFE0E0E0))
                )

                // Unsafe Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = "Unsafe",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.Start)
                            .padding(bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(1.dp)
                            .background(Color(0xFFE0E0E0))
                            .align(Alignment.Start)
                            .padding(bottom = 8.dp)
                    )
                    val unsafePercentage = 100.0 - safePercentage
                    Text(
                        text = "$unsafeCount product",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 16.sp,
                            color = Color(0xFFAAAAAA)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.Start)
                            .padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color(0xFFCEEB44), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$newScanned product scanned today",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun HistoryScan(name: String, allergens: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Text(
                    text = "Risky Ingredients",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black
                )

                Text(
                    text = allergens,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = Color.Blue
                )
            }

            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Next",
                tint = Color.Black
            )
        }
    }
}
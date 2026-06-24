package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.animation.animateColorAsState
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.PastelAccent
import com.example.ui.theme.PastelError
import com.example.ui.theme.PastelPrimary
import com.example.ui.theme.PastelSurfaceVariant
import com.example.ui.theme.PastelTertiary
import com.example.ui.viewmodel.CalendarDay
import com.example.ui.viewmodel.CigaretteViewModel
import com.example.ui.viewmodel.DailyStat
import com.example.ui.viewmodel.MonthlyStat
import com.example.ui.viewmodel.WeeklyStat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CigaretteTrackerScreen(
    viewModel: CigaretteViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Home, 1: Stats, 2: Calendar, 3: Settings

    // Settings Dialog Toggle
    var showEditDialog by remember { mutableStateOf(false) }

    val currentCount by viewModel.currentCount.collectAsStateWithLifecycle()

    // Permission launcher for POST_NOTIFICATIONS
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updateRemindersEnabled(isGranted)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Elegant Clean Minimalism Bottom Navigation Bar
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.height(80.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tab 1: Αρχική
                    val homeSelected = selectedTab == 0
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 8.dp)
                            .testTag("nav_home_tab"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Αρχική",
                            tint = if (homeSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Αρχική",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (homeSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                        )
                    }

                    // Tab 2: Στατιστικά
                    val statsSelected = selectedTab == 1
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 8.dp)
                            .testTag("nav_stats_tab"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Στατιστικά",
                            tint = if (statsSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Στατιστικά",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (statsSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                        )
                    }

                    // Tab 3: Ημερολόγιο
                    val calendarSelected = selectedTab == 2
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 2 }
                            .padding(vertical = 8.dp)
                            .testTag("nav_calendar_tab"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Ημερολόγιο",
                            tint = if (calendarSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ημερολόγιο",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (calendarSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                        )
                    }

                    // Tab 4: Ρυθμίσεις
                    val settingsSelected = selectedTab == 3
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 3 }
                            .padding(vertical = 8.dp)
                            .testTag("nav_settings_tab"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ρυθμίσεις",
                            tint = if (settingsSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ρυθμίσεις",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (settingsSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> HomeTab(
                    viewModel = viewModel,
                    onOpenSettings = { selectedTab = 3 },
                    onOpenEditCount = { showEditDialog = true }
                )
                1 -> StatsTab(viewModel = viewModel)
                2 -> CalendarTab(viewModel = viewModel)
                3 -> SettingsTab(
                    viewModel = viewModel,
                    onRequestPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val status = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                            if (status != PackageManager.PERMISSION_GRANTED) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.updateRemindersEnabled(true)
                            }
                        } else {
                            viewModel.updateRemindersEnabled(true)
                        }
                    }
                )
            }
        }
    }

    // Direct input edit dialog
    if (showEditDialog) {
        CustomCountDialog(
            initialCount = currentCount,
            onDismiss = { showEditDialog = false },
            onConfirm = { newCount ->
                viewModel.setCustomCount(newCount)
                showEditDialog = false
            }
        )
    }
}

// ==========================================
// TAB 1: HOME VIEW
// ==========================================
@Composable
fun HomeTab(
    viewModel: CigaretteViewModel,
    onOpenSettings: () -> Unit,
    onOpenEditCount: () -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val currentCount by viewModel.currentCount.collectAsStateWithLifecycle()
    val dailyGoal by viewModel.dailyGoal.collectAsStateWithLifecycle()

    val formattedDateTitle = remember(selectedDate) {
        formatSelectedDateGreekTitle(selectedDate)
    }
    val isDateToday = remember(selectedDate) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        selectedDate == todayStr
    }

    val targetCircleBgColor = remember(currentCount) {
        when {
            currentCount >= 20 -> Color(0xFFFCE4E4)       // Soft pastel light red
            currentCount >= 13 -> Color(0xFFE3F2FD)       // Soft pastel light blue
            else -> PastelTertiary                        // Default pastel mint green
        }
    }
    val targetCircleTextColor = remember(currentCount) {
        when {
            currentCount >= 20 -> Color(0xFF78281F)       // Deep elegant dark red for contrast
            currentCount >= 13 -> Color(0xFF1B4F72)       // Deep elegant dark blue for contrast
            else -> PastelAccent                          // Default deep sage/emerald
        }
    }
    val targetPrimaryThemeColor = remember(currentCount) {
        when {
            currentCount >= 20 -> PastelError             // Soft pastel coral-red
            currentCount >= 13 -> Color(0xFF5DADE2)       // Gentle pastel blue
            else -> PastelPrimary                         // Default elegant pastel green
        }
    }

    val circleBgColor by animateColorAsState(targetValue = targetCircleBgColor, label = "circleBg")
    val circleTextColor by animateColorAsState(targetValue = targetCircleTextColor, label = "circleText")
    val primaryThemeColor by animateColorAsState(targetValue = targetPrimaryThemeColor, label = "primaryTheme")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // BESPOKE HEADER ROW (Clean Minimalism)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isDateToday) "ΣΗΜΕΡΑ" else "ΕΠΙΛΕΓΜΕΝΗ ΗΜΕΡΑ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formattedDateTitle,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Settings Gear button to configure goals
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ρυθμίσεις Στόχου",
                    tint = primaryThemeColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // HERO VISUAL: Large Counter Circle Plate
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .shadow(elevation = 16.dp, shape = CircleShape, clip = false)
                    .background(
                        color = circleBgColor,
                        shape = CircleShape
                    )
                    .border(
                        border = BorderStroke(8.dp, Color.White),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentCount.toString(),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = circleTextColor,
                        modifier = Modifier.testTag("cigarettes_count_display")
                    )
                    Text(
                        text = if (currentCount == 1) "ΤΣΙΓΑΡΟ" else "ΤΣΙΓΑΡΑ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryThemeColor,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // VISUAL GOAL PROGRESS FEEDBACK CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val progress = if (dailyGoal > 0) (currentCount.toFloat() / dailyGoal).coerceIn(0f, 1f) else 0f
                val isLimitExceeded = currentCount > dailyGoal
                val isLimitReached = currentCount == dailyGoal

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isLimitExceeded) Icons.Default.Warning else Icons.Default.Check,
                            contentDescription = "Κατάσταση",
                            tint = if (isLimitExceeded) MaterialTheme.colorScheme.error else primaryThemeColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Ημερήσιος Στόχος",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "$currentCount / $dailyGoal",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLimitExceeded) MaterialTheme.colorScheme.error else primaryThemeColor
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (isLimitExceeded) MaterialTheme.colorScheme.error else primaryThemeColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Text(
                    text = when {
                        isLimitExceeded -> "Υπέρβαση ορίου κατά ${currentCount - dailyGoal} τσιγάρα!"
                        isLimitReached -> "Έφτασες ακριβώς το όριο σου για σήμερα!"
                        else -> "Έχεις ακόμη ${dailyGoal - currentCount} τσιγάρα ελεύθερα."
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isLimitExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                )
            }
        }

        // CONTROLS ROW (Clean Minimalism 3-Button Arrangement)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Decrement Button
            IconButton(
                onClick = { viewModel.decrementCount() },
                enabled = currentCount > 0,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = if (currentCount > 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("decrement_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp, 3.dp)
                        .background(
                            color = if (currentCount > 0) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(1.5.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.width(28.dp))

            // 2. Large Hero Increment Button
            IconButton(
                onClick = { viewModel.incrementCount() },
                modifier = Modifier
                    .size(96.dp)
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(40.dp))
                    .background(
                        color = primaryThemeColor,
                        shape = RoundedCornerShape(40.dp)
                    )
                    .testTag("increment_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Αύξηση",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.width(28.dp))

            // 3. Quick Edit / Custom Count Button
            IconButton(
                onClick = onOpenEditCount,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("quick_edit_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Ορισμός",
                    tint = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ==========================================
// TAB 2: STATISTICS VIEW
// ==========================================
@Composable
fun StatsTab(viewModel: CigaretteViewModel) {
    val statsDaily by viewModel.statsDaily.collectAsStateWithLifecycle()
    val statsWeekly by viewModel.statsWeekly.collectAsStateWithLifecycle()
    val statsMonthly by viewModel.statsMonthly.collectAsStateWithLifecycle()
    val averageDaily by viewModel.averageDaily.collectAsStateWithLifecycle()
    val reductionRate by viewModel.reductionRate.collectAsStateWithLifecycle()

    var selectedChartPeriod by remember { mutableStateOf(0) } // 0: Daily, 1: Weekly, 2: Monthly

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Title
        Text(
            text = "Στατιστικά",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 12.dp)
        )

        // METRICS CARDS GRID
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Average Daily
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Μέση Κατανάλωση",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = String.format(Locale.US, "%.1f", averageDaily),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Μέσος όρος / ημέρα",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Card 2: Reduction Rate
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val isReduction = (reductionRate ?: 0.0) >= 0.0
                    val rateText = if (reductionRate == null) {
                        "—"
                    } else {
                        String.format(Locale.US, "%+.1f%%", -reductionRate!!) // Flip sign so negative is reduction, or format clearly
                    }

                    Icon(
                        imageVector = if (isReduction && reductionRate != null) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = "Τάση",
                        tint = if (isReduction && reductionRate != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = if (reductionRate == null) "—" else {
                                if (reductionRate!! > 0.0) {
                                    String.format(Locale.US, "%.1f%%", reductionRate)
                                } else {
                                    String.format(Locale.US, "%.1f%%", -reductionRate!!)
                                }
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (reductionRate == null) {
                                MaterialTheme.colorScheme.onBackground
                            } else if (reductionRate!! > 0) {
                                MaterialTheme.colorScheme.primary // Green/Primary for positive reduction
                            } else {
                                MaterialTheme.colorScheme.error // Red/Error for increased smoking
                            }
                        )
                        Text(
                            text = if (reductionRate == null) "Μη διαθ. δεδομένα" else {
                                if (reductionRate!! >= 0) "Μείωση καπνίσματος" else "Αύξηση καπνίσματος"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // CHART PERIOD SELECTOR ROW
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val periods = listOf("ΗΜΕΡΑ", "ΕΒΔΟΜΑΔΑ", "ΜΗΝΑΣ")
            periods.forEachIndexed { index, period ->
                val selected = selectedChartPeriod == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { selectedChartPeriod = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = period,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // CHART PANEL CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = when (selectedChartPeriod) {
                        0 -> "Κατανάλωση (Τελευταίες 7 Ημέρες)"
                        1 -> "Κατανάλωση (Τελευταίες 4 Εβδομάδες)"
                        else -> "Κατανάλωση (Τελευταίοι 6 Μήνες)"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 0.5.sp
                )

                // Beautiful custom Bar Chart implementation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    when (selectedChartPeriod) {
                        0 -> DailyChart(statsDaily)
                        1 -> WeeklyChart(statsWeekly)
                        2 -> MonthlyChart(statsMonthly)
                    }
                }
            }
        }
    }
}

// Custom bar drawing helpers
@Composable
fun DailyChart(data: List<DailyStat>) {
    val maxCount = remember(data) { data.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1 }
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { stat ->
            val fillPercent = stat.count.toFloat() / maxCount
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = stat.count.toString(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height((fillPercent * 120).dp.coerceAtLeast(4.dp))
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stat.dayLabel,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun WeeklyChart(data: List<WeeklyStat>) {
    val maxCount = remember(data) { data.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1 }
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { stat ->
            val fillPercent = stat.count.toFloat() / maxCount
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = stat.count.toString(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .height((fillPercent * 120).dp.coerceAtLeast(4.dp))
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stat.weekLabel,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun MonthlyChart(data: List<MonthlyStat>) {
    val maxCount = remember(data) { data.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1 }
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { stat ->
            val fillPercent = stat.count.toFloat() / maxCount
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = stat.count.toString(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height((fillPercent * 120).dp.coerceAtLeast(4.dp))
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stat.monthLabel,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

// ==========================================
// TAB 3: CALENDAR VIEW
// ==========================================
@Composable
fun CalendarTab(viewModel: CigaretteViewModel) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val viewedCalendar by viewModel.viewedCalendar.collectAsStateWithLifecycle()
    val calendarDays by viewModel.calendarDays.collectAsStateWithLifecycle()

    val monthName = remember(viewedCalendar) {
        getGreekMonthName(viewedCalendar.get(Calendar.MONTH))
    }
    val yearName = remember(viewedCalendar) {
        viewedCalendar.get(Calendar.YEAR).toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Ημερολόγιο Καπνίσματος",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize()
            ) {
                // Month Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.prevMonth() },
                        modifier = Modifier.testTag("prev_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Προηγούμενος Μήνας",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "$monthName $yearName",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    IconButton(
                        onClick = { viewModel.nextMonth() },
                        modifier = Modifier.testTag("next_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Επόμενος Μήνας",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Days of week header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val weekdays = listOf("ΔΕ", "ΤΡ", "ΤΕ", "ΠΕ", "ΠΑ", "ΣΑ", "ΚΥ")
                    weekdays.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Grid of Days
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(calendarDays) { day ->
                        CalendarDayCell(
                            day = day,
                            onSelect = { dateString ->
                                viewModel.selectDate(dateString)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// CUSTOM DIALOG: GOALS & REMINDERS SETTINGS
// ==========================================
@Composable
fun GoalSettingsDialog(
    viewModel: CigaretteViewModel,
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit
) {
    val dailyGoal by viewModel.dailyGoal.collectAsStateWithLifecycle()
    val remindersEnabled by viewModel.remindersEnabled.collectAsStateWithLifecycle()

    var localGoal by remember { mutableStateOf(dailyGoal) }
    var localReminders by remember { mutableStateOf(remindersEnabled) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Στόχοι & Ειδοποιήσεις",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // 1. Goal Stepper Control
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Ημερήσιο Όριο Τσιγάρων",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(
                            onClick = { if (localGoal > 1) localGoal-- },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                .size(36.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp, 2.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(1.dp))
                            )
                        }

                        Text(
                            text = localGoal.toString(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.widthIn(min = 40.dp),
                            textAlign = TextAlign.Center
                        )

                        IconButton(
                            onClick = { localGoal++ },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Αύξηση ορίου",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                // 2. Reminders Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Υπενθυμίσεις ορίου",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Λήψη ειδοποίησης όταν πλησιάζεις ή ξεπερνάς το όριο.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Switch(
                        checked = localReminders,
                        onCheckedChange = { localReminders = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ακύρωση", color = MaterialTheme.colorScheme.secondary)
                    }

                    Button(
                        onClick = {
                            viewModel.updateDailyGoal(localGoal)
                            if (localReminders && !remindersEnabled) {
                                // Request permission or enable
                                onRequestPermission()
                            } else {
                                viewModel.updateRemindersEnabled(localReminders)
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Αποθήκευση", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// PRE-EXISTING CUSTOM DIALOG & HELPERS
// ==========================================
@Composable
fun CalendarDayCell(
    day: CalendarDay,
    onSelect: (String) -> Unit
) {
    if (day.dateString == null) {
        Box(modifier = Modifier.aspectRatio(1f))
    } else {
        val isZero = day.count == 0
        val isSelected = day.isSelected
        val isToday = day.isToday

        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    when {
                        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        isToday -> MaterialTheme.colorScheme.surfaceVariant
                        else -> Color.Transparent
                    }
                )
                .border(
                    width = when {
                        isSelected -> 2.dp
                        isToday -> 1.dp
                        else -> 0.dp
                    },
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        else -> Color.Transparent
                    },
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable { onSelect(day.dateString) }
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = day.dayNumber.toString(),
                    fontSize = 11.sp,
                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onBackground
                    },
                    modifier = Modifier.padding(top = 2.dp)
                )

                if (!isZero) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 2.dp)
                            .background(
                                color = if (day.count > 10) MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                            .sizeIn(minWidth = 14.dp, minHeight = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.count.toString(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 3.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 5.dp)
                            .size(3.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun CustomCountDialog(
    initialCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var countText by remember { mutableStateOf(initialCount.toString()) }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Ορισμός Τσιγάρων",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Εισάγετε τον ακριβή αριθμό τσιγάρων για την επιλεγμένη ημέρα:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = countText,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && newValue.length < 4) {
                            countText = newValue
                            isError = false
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = isError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_count_text_field"),
                    label = { Text("Αριθμός τσιγάρων") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_cancel_button")
                    ) {
                        Text("Ακύρωση", color = MaterialTheme.colorScheme.secondary)
                    }

                    Button(
                        onClick = {
                            val countVal = countText.toIntOrNull()
                            if (countVal != null && countVal >= 0) {
                                onConfirm(countVal)
                            } else {
                                isError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_confirm_button")
                    ) {
                        Text("Αποθήκευση", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun getGreekMonthName(monthIndex: Int): String {
    return when (monthIndex) {
        Calendar.JANUARY -> "Ιανουάριος"
        Calendar.FEBRUARY -> "Φεβρουάριος"
        Calendar.MARCH -> "Μάρτιος"
        Calendar.APRIL -> "Απρίλιος"
        Calendar.MAY -> "Μάιος"
        Calendar.JUNE -> "Ιούνιος"
        Calendar.JULY -> "Ιούλιος"
        Calendar.AUGUST -> "Αύγουστος"
        Calendar.SEPTEMBER -> "Σεπτέμβριος"
        Calendar.OCTOBER -> "Οκτώβριος"
        Calendar.NOVEMBER -> "Νοέμβριος"
        Calendar.DECEMBER -> "Δεκέμβριος"
        else -> ""
    }
}

private fun formatSelectedDateGreekTitle(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = parser.parse(dateString) ?: return dateString

        val cal = Calendar.getInstance()
        cal.time = date

        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val monthName = when (cal.get(Calendar.MONTH)) {
            Calendar.JANUARY -> "Ιανουαρίου"
            Calendar.FEBRUARY -> "Φεβρουαρίου"
            Calendar.MARCH -> "Μαρτίου"
            Calendar.APRIL -> "Απριλίου"
            Calendar.MAY -> "Μαΐου"
            Calendar.JUNE -> "Ιουνίου"
            Calendar.JULY -> "Ιουλίου"
            Calendar.AUGUST -> "Αυγούστου"
            Calendar.SEPTEMBER -> "Σεπτεμβρίου"
            Calendar.OCTOBER -> "Οκτωβρίου"
            Calendar.NOVEMBER -> "Νοεμβρίου"
            Calendar.DECEMBER -> "Δεκεμβρίου"
            else -> ""
        }
        "$dayOfMonth $monthName"
    } catch (e: Exception) {
        dateString
    }
}

@Composable
fun SettingsTab(
    viewModel: CigaretteViewModel,
    onRequestPermission: () -> Unit
) {
    val dailyGoal by viewModel.dailyGoal.collectAsStateWithLifecycle()
    val remindersEnabled by viewModel.remindersEnabled.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Ρυθμίσεις Εφαρμογής",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 12.dp)
        )

        // CARD 1: Ημερήσιος Στόχος
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ημερήσιος Στόχος",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Ορίστε το επιθυμητό όριο τσιγάρων (έως 40)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$dailyGoal τσ.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // The slider - η μπάρα του ημερήσιου στόχου με όριο τα 40 τσιγάρα
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = { if (dailyGoal > 1) viewModel.updateDailyGoal(dailyGoal - 1) },
                        enabled = dailyGoal > 1,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .size(36.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp, 2.dp)
                                .background(
                                    if (dailyGoal > 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                                    RoundedCornerShape(1.5.dp)
                                )
                        )
                    }

                    Slider(
                        value = dailyGoal.toFloat(),
                        onValueChange = { viewModel.updateDailyGoal(it.toInt()) },
                        valueRange = 1f..40f,
                        steps = 38,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_goal_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    IconButton(
                        onClick = { if (dailyGoal < 40) viewModel.updateDailyGoal(dailyGoal + 1) },
                        enabled = dailyGoal < 40,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Αύξηση ορίου",
                            tint = if (dailyGoal < 40) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "1 τσιγάρο", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    Text(text = "40 τσιγάρα", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // CARD 2: Ειδοποιήσεις
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Υπενθυμίσεις ορίου",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Λήψη ειδοποίησης όταν πλησιάζετε ή ξεπερνάτε το ημερήσιο όριο.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Switch(
                    checked = remindersEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            onRequestPermission()
                        } else {
                            viewModel.updateRemindersEnabled(false)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("settings_reminders_switch")
                )
            }
        }
    }
}


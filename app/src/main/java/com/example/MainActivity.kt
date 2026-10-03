package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TradingViewModel
import com.example.ui.screens.ArchivesScreen
import com.example.ui.screens.LiveSignalsScreen
import com.example.ui.screens.MT5BridgeScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: TradingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val selectedTab by viewModel.selectedTab.collectAsState()
                val pushMsg by viewModel.pushNotificationMessage.collectAsState()
                val context = LocalContext.current

                LaunchedEffect(pushMsg) {
                    pushMsg?.let { msg ->
                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                        viewModel.clearPushNotification()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        VowiiBottomNavigation(
                            selectedTab = selectedTab,
                            onTabSelected = { viewModel.selectTab(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            0 -> ScannerScreen(viewModel = viewModel)
                            1 -> LiveSignalsScreen(viewModel = viewModel)
                            2 -> ArchivesScreen(viewModel = viewModel)
                            3 -> MT5BridgeScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VowiiBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        modifier = Modifier.border(width = 1.dp, color = SurfaceCardBorder),
        containerColor = NavBackground,
        contentColor = TextPrimary,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        val navItems = listOf(
            NavItem("Scanner", Icons.Default.BarChart, 0),
            NavItem("Live Signal", Icons.Default.ShowChart, 1),
            NavItem("Archives", Icons.Default.Inventory2, 2),
            NavItem("MT5 Bridge", Icons.Default.FlashOn, 3)
        )

        navItems.forEach { item ->
            val isSelected = selectedTab == item.index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.index) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) NeonGreen else TextMuted
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) NeonGreen else TextMuted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0x1100FF88)
                )
            )
        }
    }
}

private data class NavItem(
    val label: String,
    val icon: ImageVector,
    val index: Int
)


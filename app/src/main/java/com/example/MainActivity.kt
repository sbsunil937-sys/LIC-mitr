package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.LibraryAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.screens.AdminPortalScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PolicyDetailScreen
import com.example.ui.screens.PolicyRegistrationScreen
import com.example.ui.screens.PremiumHistoryScreen
import com.example.ui.theme.LicGoldAccent
import com.example.ui.theme.LicNavyDark
import com.example.ui.theme.LicNavyPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LicViewModel
import kotlinx.coroutines.flow.collectLatest

enum class AppDestination {
    DASHBOARD,
    REGISTER,
    DETAIL,
    HISTORY,
    ADMIN
}

class MainActivity : ComponentActivity() {
    private val viewModel: LicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LicMitrApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LicMitrApp(viewModel: LicViewModel) {
    var currentScreen by remember { mutableStateOf(AppDestination.DASHBOARD) }
    var selectedPolicyIdForDetail by remember { mutableLongStateOf(0L) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen to messages from ViewModel
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Android Hardware / Gesture BackHandler
    BackHandler(enabled = currentScreen != AppDestination.DASHBOARD) {
        currentScreen = when (currentScreen) {
            AppDestination.HISTORY -> AppDestination.DETAIL
            AppDestination.DETAIL -> AppDestination.DASHBOARD
            AppDestination.REGISTER -> AppDestination.DASHBOARD
            AppDestination.ADMIN -> AppDestination.DASHBOARD
            AppDestination.DASHBOARD -> AppDestination.DASHBOARD
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentScreen == AppDestination.DASHBOARD ||
                currentScreen == AppDestination.ADMIN ||
                currentScreen == AppDestination.REGISTER
            ) {
                NavigationBar(
                    containerColor = LicNavyDark,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppDestination.DASHBOARD,
                        onClick = { currentScreen = AppDestination.DASHBOARD },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppDestination.DASHBOARD) Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                                contentDescription = "Dashboard"
                            )
                        },
                        label = {
                            Text(
                                "Dashboard",
                                fontWeight = if (currentScreen == AppDestination.DASHBOARD) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LicNavyDark,
                            selectedTextColor = LicGoldAccent,
                            indicatorColor = LicGoldAccent,
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = Color.White.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.testTag("nav_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppDestination.REGISTER,
                        onClick = { currentScreen = AppDestination.REGISTER },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppDestination.REGISTER) Icons.Default.AddCircle else Icons.Outlined.LibraryAdd,
                                contentDescription = "Add Policy"
                            )
                        },
                        label = {
                            Text(
                                "Add Policy",
                                fontWeight = if (currentScreen == AppDestination.REGISTER) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LicNavyDark,
                            selectedTextColor = LicGoldAccent,
                            indicatorColor = LicGoldAccent,
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = Color.White.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.testTag("nav_register")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppDestination.ADMIN,
                        onClick = { currentScreen = AppDestination.ADMIN },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppDestination.ADMIN) Icons.Default.AdminPanelSettings else Icons.Outlined.AdminPanelSettings,
                                contentDescription = "Agent Portal"
                            )
                        },
                        label = {
                            Text(
                                "Agent Portal",
                                fontWeight = if (currentScreen == AppDestination.ADMIN) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LicNavyDark,
                            selectedTextColor = LicGoldAccent,
                            indicatorColor = LicGoldAccent,
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = Color.White.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.testTag("nav_admin")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 700.dp)
            ) {
                when (currentScreen) {
                    AppDestination.DASHBOARD -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToRegister = { currentScreen = AppDestination.REGISTER },
                            onNavigateToDetails = { id ->
                                selectedPolicyIdForDetail = id
                                currentScreen = AppDestination.DETAIL
                            },
                            onNavigateToHistory = { id ->
                                selectedPolicyIdForDetail = id
                                currentScreen = AppDestination.HISTORY
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppDestination.REGISTER -> {
                        PolicyRegistrationScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = AppDestination.DASHBOARD },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppDestination.DETAIL -> {
                        PolicyDetailScreen(
                            policyId = selectedPolicyIdForDetail,
                            viewModel = viewModel,
                            onBack = { currentScreen = AppDestination.DASHBOARD },
                            onNavigateToHistory = { id ->
                                selectedPolicyIdForDetail = id
                                currentScreen = AppDestination.HISTORY
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppDestination.HISTORY -> {
                        PremiumHistoryScreen(
                            policyId = selectedPolicyIdForDetail,
                            viewModel = viewModel,
                            onBack = { currentScreen = AppDestination.DETAIL },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppDestination.ADMIN -> {
                        AdminPortalScreen(
                            viewModel = viewModel,
                            onNavigateToRegister = { currentScreen = AppDestination.REGISTER },
                            onNavigateToDetails = { id ->
                                selectedPolicyIdForDetail = id
                                currentScreen = AppDestination.DETAIL
                            },
                            onNavigateToHistory = { id ->
                                selectedPolicyIdForDetail = id
                                currentScreen = AppDestination.HISTORY
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

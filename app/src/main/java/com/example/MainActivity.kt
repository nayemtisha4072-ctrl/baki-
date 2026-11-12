package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AdMobInterstitialDialog
import com.example.ui.components.BakiKhataTopAppBar
import com.example.ui.screens.AddTransactionScreen
import com.example.ui.screens.CustomerDashboardScreen
import com.example.ui.screens.CustomerDetailScreen
import com.example.ui.screens.CustomerListScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsAndComplianceScreen
import com.example.ui.screens.ShopDashboardScreen
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BakiKhataViewModel
import com.example.ui.viewmodel.UserRole
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class Screen {
    object Dashboard : Screen()
    object Customers : Screen()
    object AddTransaction : Screen()
    object Reports : Screen()
    object Settings : Screen()
    data class CustomerDetail(val customerId: String) : Screen()
}

data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val screen: Screen
)

class MainActivity : ComponentActivity() {

    private val viewModel: BakiKhataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                BakiKhataMainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BakiKhataMainApp(viewModel: BakiKhataViewModel) {
    val currentRole by viewModel.currentRole.collectAsState()
    val shop by viewModel.currentShop.collectAsState()
    val notifications by viewModel.customerNotifications.collectAsState()
    val unreadNotifCount = notifications.count { !it.isRead }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var transactionInitialTab by remember { mutableIntStateOf(0) }
    var transactionPreselectedCustId by remember { mutableStateOf<String?>(null) }

    var showRoleSwitchDialog by remember { mutableStateOf(false) }
    var interstitialTitle by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Listen to UI messages
    LaunchedEffect(Unit) {
        viewModel.uiMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Listen to AdMob Interstitial triggers
    LaunchedEffect(Unit) {
        viewModel.showInterstitialEvent.collectLatest { title ->
            interstitialTitle = title
        }
    }

    // Handle Back Press
    BackHandler(enabled = currentScreen !is Screen.Dashboard || currentRole == UserRole.CUSTOMER) {
        if (currentScreen is Screen.CustomerDetail) {
            currentScreen = Screen.Customers
        } else if (currentScreen !is Screen.Dashboard) {
            currentScreen = Screen.Dashboard
        } else if (currentRole == UserRole.CUSTOMER) {
            viewModel.switchRole(UserRole.SHOPKEEPER)
        }
    }

    val navItems = listOf(
        NavItem("হোম", Icons.Filled.Home, Icons.Outlined.Home, Screen.Dashboard),
        NavItem("গ্রাহক", Icons.Filled.People, Icons.Outlined.People, Screen.Customers),
        NavItem("বাকি/জমা", Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline, Screen.AddTransaction),
        NavItem("রিপোর্ট", Icons.Filled.Assessment, Icons.Outlined.Assessment, Screen.Reports),
        NavItem("সেটিংস", Icons.Filled.Settings, Icons.Outlined.Settings, Screen.Settings)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (currentScreen !is Screen.CustomerDetail) {
                BakiKhataTopAppBar(
                    title = if (currentRole == UserRole.SHOPKEEPER) (shop?.name ?: "বাকি খাতা") else "আমার বাকি খাতা",
                    subtitle = if (currentRole == UserRole.SHOPKEEPER) "দোকানদার ড্যাশবোর্ড" else "গ্রাহক ভিউ (কাস্টমার খাতা)",
                    currentRole = currentRole,
                    unreadNotifCount = unreadNotifCount,
                    onRoleSwitchClick = { showRoleSwitchDialog = true },
                    onNotificationClick = {
                        if (currentRole == UserRole.SHOPKEEPER) {
                            currentScreen = Screen.Reports
                        } else {
                            viewModel.markNotificationsRead()
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (currentRole == UserRole.SHOPKEEPER && currentScreen !is Screen.CustomerDetail) {
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("shopkeeper_bottom_nav"),
                    containerColor = Color.White,
                    tonalElevation = 6.dp
                ) {
                    navItems.forEach { item ->
                        val selected = when (item.screen) {
                            Screen.Dashboard -> currentScreen is Screen.Dashboard
                            Screen.Customers -> currentScreen is Screen.Customers
                            Screen.AddTransaction -> currentScreen is Screen.AddTransaction
                            Screen.Reports -> currentScreen is Screen.Reports
                            Screen.Settings -> currentScreen is Screen.Settings
                            else -> false
                        }

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (item.screen == Screen.AddTransaction) {
                                    transactionInitialTab = 0
                                    transactionPreselectedCustId = null
                                }
                                currentScreen = item.screen
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EmeraldGreenDark,
                                indicatorColor = EmeraldContainer,
                                selectedTextColor = EmeraldGreenDark
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentRole == UserRole.CUSTOMER) {
                CustomerDashboardScreen(viewModel = viewModel)
            } else {
                when (val screen = currentScreen) {
                    is Screen.Dashboard -> {
                        ShopDashboardScreen(
                            viewModel = viewModel,
                            onNavigateToAddDue = {
                                transactionInitialTab = 0
                                transactionPreselectedCustId = null
                                currentScreen = Screen.AddTransaction
                            },
                            onNavigateToReceivePayment = {
                                transactionInitialTab = 1
                                transactionPreselectedCustId = null
                                currentScreen = Screen.AddTransaction
                            },
                            onNavigateToCustomers = {
                                currentScreen = Screen.Customers
                            },
                            onNavigateToReports = {
                                currentScreen = Screen.Reports
                            },
                            onSelectCustomer = { custId ->
                                currentScreen = Screen.CustomerDetail(custId)
                            }
                        )
                    }

                    is Screen.Customers -> {
                        CustomerListScreen(
                            viewModel = viewModel,
                            onCustomerClick = { custId ->
                                currentScreen = Screen.CustomerDetail(custId)
                            }
                        )
                    }

                    is Screen.AddTransaction -> {
                        AddTransactionScreen(
                            viewModel = viewModel,
                            initialTab = transactionInitialTab,
                            preselectedCustomerId = transactionPreselectedCustId,
                            onTransactionSaved = { custId ->
                                currentScreen = Screen.CustomerDetail(custId)
                            }
                        )
                    }

                    is Screen.Reports -> {
                        ReportsScreen(viewModel = viewModel)
                    }

                    is Screen.Settings -> {
                        SettingsAndComplianceScreen(viewModel = viewModel)
                    }

                    is Screen.CustomerDetail -> {
                        CustomerDetailScreen(
                            viewModel = viewModel,
                            customerId = screen.customerId,
                            onBackClick = { currentScreen = Screen.Customers },
                            onAddDueClick = { custId ->
                                transactionInitialTab = 0
                                transactionPreselectedCustId = custId
                                currentScreen = Screen.AddTransaction
                            },
                            onReceivePaymentClick = { custId ->
                                transactionInitialTab = 1
                                transactionPreselectedCustId = custId
                                currentScreen = Screen.AddTransaction
                            }
                        )
                    }
                }
            }
        }
    }

    // Role Switch Dialog (Allows switching between Shopkeeper and Customer mode)
    if (showRoleSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showRoleSwitchDialog = false },
            title = { Text("ব্যবহারকারীর ভূমিকা পরিবর্তন (User Role)") },
            text = {
                Text(
                    "বাকি খাতা অ্যাপে দোকানদার এবং গ্রাহক উভয়ের জন্য আলাদা ইন্টারফেস রয়েছে। আপনি কোন ভূমিকায় অ্যাপটি দেখতে চান?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.switchRole(UserRole.SHOPKEEPER)
                        currentScreen = Screen.Dashboard
                        showRoleSwitchDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
                ) {
                    Text("দোকানদার (Shopkeeper)")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.switchRole(UserRole.CUSTOMER)
                        showRoleSwitchDialog = false
                    }
                ) {
                    Text("গ্রাহক (Customer View)")
                }
            }
        )
    }

    // AdMob Interstitial Dialog
    interstitialTitle?.let { title ->
        AdMobInterstitialDialog(
            actionContext = title,
            onDismiss = { interstitialTitle = null }
        )
    }
}

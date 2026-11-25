package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.UserRole
import com.example.ui.screens.AuthFlowScreen
import com.example.ui.screens.BriefsScreen
import com.example.ui.screens.CaseDetailScreen
import com.example.ui.screens.CasesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DocumentsExplorerScreen
import com.example.ui.screens.LegalResourcesScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StagesTrackerScreen
import com.example.ui.screens.TeamAccessScreen
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

object VakilRoutes {
    const val DASHBOARD = "dashboard"
    const val CASES = "cases"
    const val CASE_DETAIL = "case_detail/{caseId}"
    const val DOCUMENTS = "documents"
    const val STAGES = "stages"
    const val BRIEFS = "briefs"
    const val LEGAL = "legal"
    const val REPORTS = "reports"
    const val TEAM_ACCESS = "team_access"
    const val SETTINGS = "settings"

    fun caseDetail(caseId: Long) = "case_detail/$caseId"
}

private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val leadOnly: Boolean = false
)

@Composable
fun VakilApp(
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel
) {
    val workspaceState by mainViewModel.workspaceState.collectAsStateWithLifecycle()
    val toastMsg by mainViewModel.toastMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            snackbarHostState.showSnackbar(it)
            mainViewModel.consumeToast()
        }
    }

    if (workspaceState.currentUser == null) {
        AuthFlowScreen(authViewModel = authViewModel)
        return
    }

    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: VakilRoutes.DASHBOARD
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val isLead = workspaceState.currentUser?.role == UserRole.LEAD_LAWYER

    val bottomNavItems = remember {
        listOf(
            NavItem(VakilRoutes.DASHBOARD, "داشبورد", Icons.Default.Dashboard),
            NavItem(VakilRoutes.CASES, "پرونده‌ها", Icons.Default.Folder),
            NavItem(VakilRoutes.BRIEFS, "لوایح", Icons.Default.EditNote),
            NavItem(VakilRoutes.LEGAL, "قوانین و آراء", Icons.Default.MenuBook),
            NavItem(VakilRoutes.REPORTS, "گزارش‌ها", Icons.Default.Assessment)
        )
    }

    val allDrawerItems = remember(isLead) {
        listOf(
            NavItem(VakilRoutes.DASHBOARD, "داشبورد", Icons.Default.Dashboard),
            NavItem(VakilRoutes.CASES, "پرونده‌ها", Icons.Default.Folder),
            NavItem(VakilRoutes.DOCUMENTS, "آرشیو مدارک", Icons.Default.Description),
            NavItem(VakilRoutes.STAGES, "مراحل رسیدگی", Icons.Default.Timeline),
            NavItem(VakilRoutes.BRIEFS, "لوایح و مکاتبات", Icons.Default.EditNote),
            NavItem(VakilRoutes.LEGAL, "قوانین و آرای قضایی", Icons.Default.MenuBook),
            NavItem(VakilRoutes.REPORTS, "گزارش‌گیری و خروجی", Icons.Default.Assessment),
            NavItem(VakilRoutes.TEAM_ACCESS, "دسترسی همکاران", Icons.Default.AdminPanelSettings, leadOnly = true),
            NavItem(VakilRoutes.SETTINGS, "تنظیمات و امنیت", Icons.Default.Settings)
        ).filter { !it.leadOnly || isLead }
    }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val showBottomBar = !currentRoute.startsWith("case_detail/")

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Row {
                        Icon(Icons.Default.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("دفتر وکالت وکیل‌یار", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        workspaceState.currentUser?.fullName ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        workspaceState.currentUser?.role?.titleFa ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    allDrawerItems.forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(item.label) },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navigateTopLevel(item.route)
                            }
                        )
                    }
                }
            }
        }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 600.dp
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                contentWindowInsets = WindowInsets.navigationBars,
                bottomBar = {
                    if (showBottomBar && !isWideScreen) {
                        NavigationBar(windowInsets = WindowInsets.navigationBars) {
                            bottomNavItems.forEach { item ->
                                NavigationBarItem(
                                    selected = currentRoute == item.route,
                                    onClick = { navigateTopLevel(item.route) },
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label, maxLines = 1) }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isWideScreen && showBottomBar) {
                        NavigationRail(modifier = Modifier.fillMaxHeight()) {
                            bottomNavItems.forEach { item ->
                                NavigationRailItem(
                                    selected = currentRoute == item.route,
                                    onClick = { navigateTopLevel(item.route) },
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label) }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = VakilRoutes.DASHBOARD
                        ) {
                            composable(VakilRoutes.DASHBOARD) {
                                DashboardScreen(
                                    state = workspaceState,
                                    onMenuClick = { scope.launch { drawerState.open() } },
                                    onOpenCase = { id -> navController.navigate(VakilRoutes.caseDetail(id)) },
                                    onNavigateToCases = { navigateTopLevel(VakilRoutes.CASES) },
                                    onNavigateToBriefs = { navigateTopLevel(VakilRoutes.BRIEFS) },
                                    onNavigateToLegal = { navigateTopLevel(VakilRoutes.LEGAL) },
                                    onNavigateToReports = { navigateTopLevel(VakilRoutes.REPORTS) },
                                    onNavigateToDocuments = { navigateTopLevel(VakilRoutes.DOCUMENTS) }
                                )
                            }
                            composable(VakilRoutes.CASES) {
                                CasesScreen(
                                    state = workspaceState,
                                    viewModel = mainViewModel,
                                    onMenuClick = { scope.launch { drawerState.open() } },
                                    onOpenCase = { id -> navController.navigate(VakilRoutes.caseDetail(id)) }
                                )
                            }
                            composable(
                                route = VakilRoutes.CASE_DETAIL,
                                arguments = listOf(navArgument("caseId") { type = NavType.LongType })
                            ) { entry ->
                                val cid = entry.arguments?.getLong("caseId") ?: 0L
                                CaseDetailScreen(
                                    caseId = cid,
                                    state = workspaceState,
                                    viewModel = mainViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(VakilRoutes.DOCUMENTS) {
                                DocumentsExplorerScreen(
                                    state = workspaceState,
                                    viewModel = mainViewModel,
                                    onMenuClick = { scope.launch { drawerState.open() } },
                                    onOpenCase = { id -> navController.navigate(VakilRoutes.caseDetail(id)) }
                                )
                            }
                            composable(VakilRoutes.STAGES) {
                                StagesTrackerScreen(
                                    state = workspaceState,
                                    viewModel = mainViewModel,
                                    onMenuClick = { scope.launch { drawerState.open() } },
                                    onOpenCase = { id -> navController.navigate(VakilRoutes.caseDetail(id)) }
                                )
                            }
                            composable(VakilRoutes.BRIEFS) {
                                BriefsScreen(
                                    state = workspaceState,
                                    viewModel = mainViewModel,
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable(VakilRoutes.LEGAL) {
                                LegalResourcesScreen(
                                    state = workspaceState,
                                    viewModel = mainViewModel,
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable(VakilRoutes.REPORTS) {
                                ReportsScreen(
                                    state = workspaceState,
                                    viewModel = mainViewModel,
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable(VakilRoutes.TEAM_ACCESS) {
                                TeamAccessScreen(
                                    state = workspaceState,
                                    viewModel = mainViewModel,
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable(VakilRoutes.SETTINGS) {
                                SettingsScreen(
                                    state = workspaceState,
                                    viewModel = mainViewModel,
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

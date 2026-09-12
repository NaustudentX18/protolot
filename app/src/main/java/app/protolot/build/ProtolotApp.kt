package app.protolot.build

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.protolot.build.navigation.ProtolotDestinations
import app.protolot.build.ui.home.HomeScreen
import app.protolot.build.ui.kits.KitsScreen
import app.protolot.build.ui.live.LiveScreen
import app.protolot.build.ui.lots.LotsScreen
import app.protolot.build.ui.makerhub.MakerHubScreen
import app.protolot.build.ui.more.MoreScreen
import app.protolot.build.ui.project.ProjectScreen
import app.protolot.build.ui.providers.ProvidersScreen
import app.protolot.build.ui.settings.SettingsScreen

private data class TabItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun ProtolotApp() {
    val navController = rememberNavController()
    val tabs = listOf(
        TabItem(ProtolotDestinations.HOME, "Ideas", Icons.Filled.Home),
        TabItem(ProtolotDestinations.LOTS, "Lots", Icons.Filled.Inventory2),
        TabItem(ProtolotDestinations.KITS, "Kits", Icons.Outlined.Category),
        TabItem(ProtolotDestinations.MORE, "More", Icons.Filled.MoreHoriz),
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val route = currentDestination?.route.orEmpty()
    val hideBottom = route.startsWith("project")
    val showBottomBar = !hideBottom && (
        tabs.any { tab -> currentDestination?.hierarchy?.any { it.route == tab.route } == true } ||
            route in listOf(
                ProtolotDestinations.PROVIDERS,
                ProtolotDestinations.SETTINGS,
                ProtolotDestinations.LIVE,
                ProtolotDestinations.MAKER_HUB,
                ProtolotDestinations.LOTS_IMPORT,
            )
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        val selected = when (tab.route) {
                            ProtolotDestinations.MORE -> route in listOf(
                                ProtolotDestinations.MORE,
                                ProtolotDestinations.PROVIDERS,
                                ProtolotDestinations.SETTINGS,
                                ProtolotDestinations.LIVE,
                                ProtolotDestinations.MAKER_HUB,
                            )
                            else -> currentDestination?.hierarchy?.any { it.route == tab.route } == true
                        }
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ProtolotDestinations.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(ProtolotDestinations.HOME) {
                HomeScreen(
                    onOpenProject = { id ->
                        navController.navigate(ProtolotDestinations.project(id))
                    },
                    onOpenProviders = {
                        navController.navigate(ProtolotDestinations.PROVIDERS)
                    },
                    onOpenSettings = {
                        navController.navigate(ProtolotDestinations.SETTINGS)
                    },
                )
            }
            composable(
                route = ProtolotDestinations.PROJECT,
                arguments = listOf(
                    navArgument("projectId") { type = NavType.StringType },
                    navArgument("tab") {
                        type = NavType.StringType
                        defaultValue = "overview"
                    },
                ),
            ) { entry ->
                val id = entry.arguments?.getString("projectId") ?: "unknown"
                val tab = entry.arguments?.getString("tab") ?: "overview"
                ProjectScreen(
                    projectId = id,
                    initialTab = tab,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(ProtolotDestinations.LOTS) {
                LotsScreen(
                    onImport = { navController.navigate(ProtolotDestinations.LOTS_IMPORT) },
                    onOpenProject = { id ->
                        navController.navigate(ProtolotDestinations.project(id))
                    },
                )
            }
            composable(ProtolotDestinations.LOTS_IMPORT) {
                LotsScreen(
                    onImport = {},
                    importFocused = true,
                    onOpenProject = { id ->
                        navController.navigate(ProtolotDestinations.project(id))
                    },
                )
            }
            composable(ProtolotDestinations.KITS) { KitsScreen() }
            composable(ProtolotDestinations.MORE) {
                MoreScreen(
                    onProviders = { navController.navigate(ProtolotDestinations.PROVIDERS) },
                    onSettings = { navController.navigate(ProtolotDestinations.SETTINGS) },
                    onLive = { navController.navigate(ProtolotDestinations.LIVE) },
                    onMakerHub = { navController.navigate(ProtolotDestinations.MAKER_HUB) },
                )
            }
            composable(ProtolotDestinations.PROVIDERS) { ProvidersScreen() }
            composable(ProtolotDestinations.SETTINGS) { SettingsScreen() }
            composable(ProtolotDestinations.LIVE) { LiveScreen() }
            composable(ProtolotDestinations.MAKER_HUB) { MakerHubScreen() }
        }
    }
}

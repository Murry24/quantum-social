package app.quantumsocial.nav

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.quantumsocial.ui.onboarding.OnboardingPrefs
import app.quantumsocial.ui.onboarding.OnboardingScreen
import app.quantumsocial.ui.screens.ProfileScreen
import app.quantumsocial.ui.screens.StarMapScreen
import app.quantumsocial.ui.screens.StarProfileScreen
import app.quantumsocial.ui.screens.WishScreen
import app.quantumsocial.ui.screens.ZenScreen

private object Routes {
    const val HOME = "home"
    const val HOME_FILTERS = "home_filters"
    const val HOME_FOCUS = "home_focus/{starId}"
    const val WISH = "wish"
    const val PROFILE = "profile"
    const val STAR = "star/{starId}"
    const val ZEN = "zen"
    const val ONBOARDING = "onboarding"

    fun homeFocusEncoded(starId: String) = "home_focus/${Uri.encode(starId)}"

    fun starEncoded(starId: String) = "star/${Uri.encode(starId)}"
}

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val context = LocalContext.current
    // bod 18: pri prvom spustení začína „Zrodenie hviezdy“
    // stav, nie len hodnota: graf sa môže postaviť nanovo (napr. pri zmene šetriča) a musí mať aktuálny štart
    var start by remember { mutableStateOf(if (OnboardingPrefs.isDone(context)) Routes.HOME else Routes.ONBOARDING) }
    val rootEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            if (rootEntry?.destination?.route != Routes.ONBOARDING) {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    NavigationBarItem(
                        selected =
                            currentRoute == Routes.HOME ||
                                currentRoute == Routes.HOME_FILTERS ||
                                currentRoute == Routes.HOME_FOCUS,
                        onClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                    )

                    NavigationBarItem(
                        selected = currentRoute == Routes.WISH,
                        onClick = {
                            navController.navigate(Routes.WISH) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.Star, contentDescription = "WishNet") },
                        label = { Text("WishNet") },
                    )

                    NavigationBarItem(
                        selected = currentRoute == Routes.PROFILE,
                        onClick = {
                            navController.navigate(Routes.PROFILE) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.Person, contentDescription = "Profil") },
                        label = { Text("Profil") },
                    )

                    NavigationBarItem(
                        selected = currentRoute == Routes.HOME_FILTERS,
                        onClick = {
                            navController.navigate(Routes.HOME_FILTERS) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.Tune, contentDescription = "Filtre") },
                        label = { Text("Filtre") },
                    )
                }
            }
        },
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = start,
            modifier = Modifier.padding(inner),
        ) {
            // Mapa (bežná)
            composable(Routes.HOME) {
                StarMapScreen(
                    openFilters = false,
                    onOpenStar = { id -> navController.navigate(Routes.starEncoded(id)) },
                    focusStarId = null,
                    onOpenZen = { navController.navigate(Routes.ZEN) },
                )
            }

            // Mapa s otvoreným filtrom
            composable(Routes.HOME_FILTERS) {
                StarMapScreen(
                    openFilters = true,
                    onOpenStar = { id -> navController.navigate(Routes.starEncoded(id)) },
                    focusStarId = null,
                    onOpenZen = { navController.navigate(Routes.ZEN) },
                )
            }

            // Mapa so soft-focusom na hviezdu
            composable(
                route = Routes.HOME_FOCUS,
                arguments = listOf(navArgument("starId") { type = NavType.StringType }),
            ) { backStack ->
                val starId =
                    backStack.arguments?.getString("starId")?.let { Uri.decode(it) }
                        ?: return@composable
                StarMapScreen(
                    openFilters = false,
                    onOpenStar = { id -> navController.navigate(Routes.starEncoded(id)) },
                    focusStarId = starId,
                    onOpenZen = { navController.navigate(Routes.ZEN) },
                )
            }

            // Wish
            composable(Routes.WISH) { WishScreen() }

            // ZenNet – minúta ticha (bod 8)
            composable(Routes.ZEN) { ZenScreen(onClose = { navController.popBackStack() }) }

            // Profile (user)
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onRebirth = { navController.navigate(Routes.ONBOARDING) },
                )
            }

            // Zrodenie hviezdy (bod 18)
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onDone = {
                        // odteraz je domovom mapa (aj pre spodnú lištu) a späť sa do onboardingu nevracia
                        start = Routes.HOME
                        navController.navigate(Routes.HOME) { popUpTo(navController.graph.id) { inclusive = true } }
                    },
                )
            }

            // Profil hviezdy
            composable(
                route = Routes.STAR,
                arguments = listOf(navArgument("starId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val starId =
                    backStackEntry.arguments?.getString("starId")?.let { Uri.decode(it) }
                        ?: return@composable

                StarProfileScreen(
                    starId = starId,
                    onBack = { navController.popBackStack() },
                    onViewOnMap = { id ->
                        navController.navigate(Routes.homeFocusEncoded(id)) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        }
    }
}

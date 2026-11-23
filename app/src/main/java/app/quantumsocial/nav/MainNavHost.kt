package app.quantumsocial.nav

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import app.quantumsocial.core.i18n.tr
import app.quantumsocial.notify.GentleReminders
import app.quantumsocial.ui.galaxies.FriendGalaxyScreen
import app.quantumsocial.ui.nav.ActionOrb
import app.quantumsocial.ui.nav.ActionsSheet
import app.quantumsocial.ui.onboarding.OnboardingPrefs
import app.quantumsocial.ui.onboarding.OnboardingScreen
import app.quantumsocial.ui.screens.ChallengeScreen
import app.quantumsocial.ui.screens.ChordPrefs
import app.quantumsocial.ui.screens.ConstellationsScreen
import app.quantumsocial.ui.screens.DailyPrefs
import app.quantumsocial.ui.screens.DreamScreen
import app.quantumsocial.ui.screens.EchoScreen
import app.quantumsocial.ui.screens.GalacticMomentOverlay
import app.quantumsocial.ui.screens.GratitudeScreen
import app.quantumsocial.ui.screens.MapRequest
import app.quantumsocial.ui.screens.MapRequests
import app.quantumsocial.ui.screens.MemoryScreen
import app.quantumsocial.ui.screens.MoodWavesScreen
import app.quantumsocial.ui.screens.ProfileScreen
import app.quantumsocial.ui.screens.SeedsScreen
import app.quantumsocial.ui.screens.SerendipityScreen
import app.quantumsocial.ui.screens.SoulLinksScreen
import app.quantumsocial.ui.screens.StarDustPrefs
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
    const val GALAXY = "galaxy/{galaxyId}"
    const val GRATITUDE = "gratitude"
    const val CHALLENGE = "challenge"
    const val MOOD = "mood"
    const val DREAM = "dream"
    const val SEEDS = "seeds"
    const val ECHO = "echo"
    const val MEMORY = "memory"
    const val BRIDGE = "bridge"
    const val CONSTELLATIONS = "constellations"
    const val SOULLINKS = "soullinks"

    fun galaxyEncoded(galaxyId: String) = "galaxy/${Uri.encode(galaxyId)}"

    fun homeFocusEncoded(starId: String) = "home_focus/${Uri.encode(starId)}"

    fun starEncoded(starId: String) = "star/${Uri.encode(starId)}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val context = LocalContext.current
    // bod 18: pri prvom spustení začína „Zrodenie hviezdy“
    // stav, nie len hodnota: graf sa môže postaviť nanovo (napr. pri zmene šetriča) a musí mať aktuálny štart
    var start by remember { mutableStateOf(if (OnboardingPrefs.isDone(context)) Routes.HOME else Routes.ONBOARDING) }
    val rootEntry by navController.currentBackStackEntryAsState()
    val openGalaxy: (String) -> Unit = { id -> navController.navigate(Routes.galaxyEncoded(id)) }
    var showActions by remember { mutableStateOf(false) }
    // bod 20: StarDust sa načíta pri štarte a ukladá pri každej zmene
    LaunchedEffect(Unit) { StarDustPrefs.keepSaved(context) }
    LaunchedEffect(Unit) { DailyPrefs.keepSaved(context) }
    LaunchedEffect(Unit) { ChordPrefs.load(context) }
    // bod 15: večerná jemná pripomienka podľa nastavenia
    LaunchedEffect(Unit) { GentleReminders.apply(context) }
    val goHome = {
        navController.navigate(Routes.HOME) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

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
                        icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = tr("Galaxia")) },
                        label = { Text(tr("Galaxia")) },
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
                        icon = { Icon(imageVector = Icons.Filled.Star, contentDescription = tr("WishNet")) },
                        label = { Text(tr("WishNet")) },
                    )

                    // ✦ = akcie: Do chaosu, ZenNet, Moje galaxie (namiesto tlačidiel na mape)
                    NavigationBarItem(
                        selected = false,
                        onClick = { showActions = true },
                        icon = { ActionOrb() },
                        label = { Text(tr("Vesmír")) },
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
                        icon = { Icon(imageVector = Icons.Filled.Person, contentDescription = tr("Profil")) },
                        label = { Text(tr("Profil")) },
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
                        icon = { Icon(imageVector = Icons.Filled.Tune, contentDescription = tr("Filtre")) },
                        label = { Text(tr("Filtre")) },
                    )
                }
            }
        },
    ) { inner ->
        if (showActions) {
            ModalBottomSheet(
                onDismissRequest = { showActions = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            ) {
                ActionsSheet(
                    onChaos = {
                        showActions = false
                        MapRequests.request(MapRequest.Chaos)
                        goHome()
                    },
                    onZen = {
                        showActions = false
                        navController.navigate(Routes.ZEN)
                    },
                    onGalaxies = {
                        showActions = false
                        MapRequests.request(MapRequest.Galaxies)
                        goHome()
                    },
                    onGratitude = {
                        showActions = false
                        navController.navigate(Routes.GRATITUDE)
                    },
                    onChallenge = {
                        showActions = false
                        navController.navigate(Routes.CHALLENGE)
                    },
                    onMood = {
                        showActions = false
                        navController.navigate(Routes.MOOD)
                    },
                    onDream = {
                        showActions = false
                        navController.navigate(Routes.DREAM)
                    },
                    onSeeds = {
                        showActions = false
                        navController.navigate(Routes.SEEDS)
                    },
                    onEcho = {
                        showActions = false
                        navController.navigate(Routes.ECHO)
                    },
                    onMemory = {
                        showActions = false
                        navController.navigate(Routes.MEMORY)
                    },
                    onBridge = {
                        showActions = false
                        navController.navigate(Routes.BRIDGE)
                    },
                    onConstellations = {
                        showActions = false
                        navController.navigate(Routes.CONSTELLATIONS)
                    },
                    onSoulLinks = {
                        showActions = false
                        navController.navigate(Routes.SOULLINKS)
                    },
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
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
                        onOpenGalaxy = openGalaxy,
                    )
                }

                // Mapa s otvoreným filtrom
                composable(Routes.HOME_FILTERS) {
                    StarMapScreen(
                        openFilters = true,
                        onOpenStar = { id -> navController.navigate(Routes.starEncoded(id)) },
                        focusStarId = null,
                        onOpenGalaxy = openGalaxy,
                    )
                }

                // Galaxia priateľov (bod 31) – červia diera nahradí aktuálnu galaxiu, biela diera = späť na mapu
                composable(
                    route = Routes.GALAXY,
                    arguments = listOf(navArgument("galaxyId") { type = NavType.StringType }),
                ) { backStack ->
                    val galaxyId =
                        backStack.arguments?.getString("galaxyId")?.let { Uri.decode(it) } ?: return@composable
                    FriendGalaxyScreen(
                        galaxyId = galaxyId,
                        onLeave = { navController.popBackStack() },
                        onJump = { id ->
                            navController.navigate(Routes.galaxyEncoded(id)) {
                                popUpTo(Routes.GALAXY) { inclusive = true }
                            }
                        },
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
                        onOpenGalaxy = openGalaxy,
                    )
                }

                // Wish
                composable(Routes.WISH) { WishScreen() }

                // ZenNet – minúta ticha (bod 8)
                composable(Routes.ZEN) { ZenScreen(onClose = { navController.popBackStack() }) }

                // body 14, 13, 26: vďačnosť, výzva dňa, vlny nálad
                composable(Routes.GRATITUDE) { GratitudeScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.CHALLENGE) { ChallengeScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.MOOD) { MoodWavesScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.DREAM) { DreamScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.SEEDS) { SeedsScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.ECHO) { EchoScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.MEMORY) { MemoryScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.BRIDGE) {
                    SerendipityScreen(
                        onBack = { navController.popBackStack() },
                        onOpenStar = { id -> navController.navigate(Routes.starEncoded(id)) },
                    )
                }
                composable(Routes.CONSTELLATIONS) { ConstellationsScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.SOULLINKS) {
                    SoulLinksScreen(
                        onBack = { navController.popBackStack() },
                        onOpenStar = { id -> navController.navigate(Routes.starEncoded(id)) },
                    )
                }

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
                            // odteraz je domovom mapa a späť sa do onboardingu nevracia.
                            // Prvé spustenie: zmena štartu prestaví graf a ten sám otvorí mapu
                            // (navigate by ju staval 2×).
                            if (start == Routes.HOME) {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            } else {
                                start = Routes.HOME
                            }
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

            // bod 17: galaktický moment pri novom leveli
            GalacticMomentOverlay()
        }
    }
}

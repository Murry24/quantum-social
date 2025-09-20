package app.quantumsocial.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.quantumsocial.ui.screens.ProfileScreen
import app.quantumsocial.ui.screens.StarMapScreen
import app.quantumsocial.ui.screens.WishScreen

@Composable
fun MainScaffold() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Screen.all.forEach { scr ->
                    val icon =
                        when (scr) {
                            Screen.Home -> Icons.Filled.Home
                            Screen.Wish -> Icons.Filled.Star
                            Screen.Profile -> Icons.Filled.Person
                        }

                    NavigationBarItem(
                        selected = currentRoute == scr.route,
                        onClick = {
                            navController.navigate(scr.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = icon, contentDescription = scr.label) },
                        label = { Text(scr.label) },
                    )
                }
            }
        },
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(inner),
        ) {
            composable(Screen.Home.route) { StarMapScreen() }
            composable(Screen.Wish.route) { WishScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
        }
    }
}

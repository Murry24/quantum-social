package app.quantumsocial.nav

sealed class Screen(val route: String, val label: String) {
    data object Home : Screen("home", "Home")

    data object Wish : Screen("wish", "WishNet")

    data object Profile : Screen("profile", "Profile")

    companion object {
        val all = listOf(Home, Wish, Profile)
    }
}

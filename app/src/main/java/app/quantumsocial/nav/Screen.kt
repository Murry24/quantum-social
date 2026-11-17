package app.quantumsocial.nav

sealed class Screen(
    val route: String,
    val label: String,
) {
    data object Home : Screen("home", "Home")

    data object Wish : Screen("wish", "WishNet")

    data object Profile : Screen("profile", "Profile")

    // nie je v bottom bare – používa sa len pre detail hviezdy
    data object StarProfile : Screen("star_profile", "StarProfile")

    companion object {
        // položky v spodnom bare
        val bottomBar = listOf(Home, Wish, Profile)
    }
}

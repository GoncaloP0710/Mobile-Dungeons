package com.example.sololeveling.navigation

sealed class Screens(val route: String) {
    object HomeScreen : Screens("home_screen/{id}")
    object Dailies : Screens("dailies_screen/{id}")
    object Storage : Screens("storage_screen/{id}")
    object Map : Screens("map_screen/{id}")
    object Guild : Screens("guild_screen/{id}")
    object Portal : Screens("portal_screen/{id}")
}
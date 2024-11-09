package com.example.sololeveling.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sololeveling.ui.screens.Dailies
import com.example.sololeveling.ui.screens.Guild
import com.example.sololeveling.ui.screens.HomeScreen
import com.example.sololeveling.ui.screens.Map
import com.example.sololeveling.ui.screens.Storage

@Composable
fun NavGraph (navController: NavHostController){
    NavHost(
        navController = navController,
        startDestination = Screens.HomeScreen.route)
    {

        // Home Screen
        composable(
            route = Screens.HomeScreen.route + "?id={id}"
        ) { navBackStack ->
            val id: Int = navBackStack.arguments?.getString("id")?.toIntOrNull() ?: 1
            HomeScreen(navController = navController, id = id)
        }

        // Storage
        composable(
            route = Screens.Storage.route + "?id={id}"
        ) { navBackStack ->
            val id: Int = navBackStack.arguments?.getString("id")?.toIntOrNull() ?: 1
            Storage(navController = navController, id = id)
        }

        // Dailies
        composable(
            route = Screens.Dailies.route + "?id={id}"
        ) { navBackStack ->
            val id: Int = navBackStack.arguments?.getString("id")?.toIntOrNull() ?: 1
            Dailies(navController = navController, id = id)
        }

        // Guild
        composable(
            route = Screens.Guild.route + "?id={id}"
        ) { navBackStack ->
            val id: Int = navBackStack.arguments?.getString("id")?.toIntOrNull() ?: 1
            Guild(navController = navController, id = id)
        }

        // Map
        composable(
            route = Screens.Map.route + "?id={id}"
        ) { navBackStack ->
            val id: Int = navBackStack.arguments?.getString("id")?.toIntOrNull() ?: 1
            Map(navController = navController, id = id)
        }

    }
}
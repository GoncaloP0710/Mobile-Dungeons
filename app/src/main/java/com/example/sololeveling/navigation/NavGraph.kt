package com.example.sololeveling.navigation

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sololeveling.ui.screens.Dailies
import com.example.sololeveling.ui.screens.Guild
import com.example.sololeveling.ui.screens.HomeScreen
import com.example.sololeveling.ui.screens.Map
import com.example.sololeveling.ui.screens.Portal
import com.example.sololeveling.ui.screens.Storage
import com.google.firebase.database.FirebaseDatabase


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(navController: NavHostController, context: Context, db: FirebaseDatabase) {
    NavHost(
        navController = navController,
        startDestination = Screens.HomeScreen.route+"?id=6"+ "&username=",
    ) {
        // Home Screen
        composable(
            route = Screens.HomeScreen.route + "?id={id}" + "&username={username}"
        ) { navBackStack ->
            var id: Int = navBackStack.arguments?.getString("id")?.toIntOrNull()?:1
            val name: String? = navBackStack.arguments?.getString("username")
            var Notlogged: Boolean = false
            if(id == 6){
                Notlogged = true
                id = 5
            }
            println("USER4: $name")
            println("ID4: $id")
            if(name == "placeholder"){
                HomeScreen(navController = navController, id = id, db, Notlogged, "")
            }
            if(name != null){
                HomeScreen(navController = navController, id = id, db, Notlogged, name)
            }else{
                HomeScreen(navController = navController, id = id, db, Notlogged, "")
            }

        }

        // Storage
        composable(
            route = Screens.Storage.route + "?id={id}&username={username}"
        ) { navBackStack ->
            val id: Int = navBackStack.arguments?.getString("id")?.toIntOrNull() ?: 1
            val name: String? = navBackStack.arguments?.getString("username")
            if (name != null) {
                println("name is not null")
                Storage(navController = navController, id = id, db, name, context)
            } else {
                println("id: $id")
                println("name is null")
            }
        }


        // Dailies
        composable(
            route = Screens.Dailies.route + "?id={id}&username={username}"
        ) { navBackStack ->
            val id: Int = navBackStack.arguments?.getString("id")?.toIntOrNull() ?: 1
            val name: String? = navBackStack.arguments?.getString("username")

            if (name != null) {
                println("name is not null")
                Dailies(navController = navController, id = id, context = context, db, name)
            } else {
                println("id: $id")
                println("name is null: $name")
            }
        }

        // Guild
        composable(
            route = Screens.Guild.route + "?id={id}&username={username}"
        ) { navBackStack ->
            val id: Int = navBackStack.arguments?.getString("id")?.toIntOrNull() ?: 1
            val name: String? = navBackStack.arguments?.getString("username")
            if (name != null) {
                println("name is not null")
                Guild(navController = navController, id = id, db, name)
            } else {
                println("id: $id")
                println("name is null")
            }
        }

        // Map
        composable(
            route = Screens.Map.route + "?id={id}&username={username}"
        ) { navBackStack ->
            val id: Int = navBackStack.arguments?.getString("id")?.toIntOrNull() ?: 1
            val name: String? = navBackStack.arguments?.getString("username")
            if (name != null) {
                println("name is not null")
                Map(navController = navController, id = id, context = LocalContext.current, db, name)
            } else {
                println("id: $id")
                println("name is null")
            }
        }

        //Portal
        composable(
            route = Screens.Portal.route + "?id={id}&username={username}"
        ) { navBackStack ->
            val id: Int = navBackStack.arguments?.getString("id")?.toIntOrNull() ?: 1
            val name: String? = navBackStack.arguments?.getString("username")
            if(name != null){
                Portal(navController = navController, id = id, context = context, name = name, db)
            }
            else{
                Portal(navController = navController, id = id, context = context, name = "", db)
            }
        }
    }
}
package com.example.sololeveling.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun Storage(
    navController: NavController,
    id: Int
) {




    // Home Screen
    Button(onClick = { navController.navigate("home_screen/$id") }) {
        Text("Home")
    }
}
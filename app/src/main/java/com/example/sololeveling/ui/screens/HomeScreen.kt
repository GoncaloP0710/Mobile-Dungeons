package com.example.sololeveling.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sololeveling.R

@Composable
fun HomeScreen(
    navController: NavController,
    id: Int
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Image as background
        Image(
            painter = painterResource(id = R.drawable.userfullbody),
            contentDescription = "Home Image",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .fillMaxSize()
                .height(250.dp), // Adjust height as needed
        )

        // Buttons at the bottom of the screen, overlaid on top of the image
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { navController.navigate("storage_screen/$id") }) {
                Text("Storage")
            }

            Button(onClick = { navController.navigate("map_screen/$id") }) {
                Text("Map")
            }

            Button(onClick = { navController.navigate("dailies_screen/$id") }) {
                Text("Dailies")
            }

            Button(onClick = { navController.navigate("guild_screen/$id") }) {
                Text("Guild")
            }
        }
    }
}

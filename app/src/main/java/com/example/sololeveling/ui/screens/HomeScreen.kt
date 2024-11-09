package com.example.sololeveling.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph


@Composable
fun HomeScreen(
    navController: NavController,
    id: Int
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Button(onClick = { navController.navigate("storage_screen/$id") }) {
                Text("Storage")
            }
            Button(onClick = { /*TODO*/ }) {
                Text("Map")
            }
            Button(onClick = { /*TODO*/ }) {
                Text("Dailies")
            }
        }
    }
}
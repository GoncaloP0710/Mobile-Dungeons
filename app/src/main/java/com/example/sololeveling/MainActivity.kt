package com.example.sololeveling

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.sololeveling.navigation.NavGraph
import com.example.sololeveling.ui.theme.SoloLevelingTheme
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

class MainActivity : ComponentActivity() {
    private lateinit var db: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoloLevelingTheme {
                val navController = rememberNavController()
                db = Firebase.database
                NavGraph(navController = navController, context = this, db)
            }
        }
    }
}

@Preview
@Composable
fun SoloLevelingApp() {
    val id = 0
    rememberNavController().navigate("home_screen/?$id&username=placeholder")
}
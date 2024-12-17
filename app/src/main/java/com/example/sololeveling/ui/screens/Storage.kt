package com.example.sololeveling.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sololeveling.R
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue

@Composable
fun Storage(
    navController: NavController,
    id: Int,
    db: FirebaseDatabase,
    userName: String
) {

    // Use state to hold values
    var age by remember { mutableStateOf(0) }
    var description by remember { mutableStateOf("") }
    var dungeonsSpotted by remember { mutableStateOf(0) }
    var powerLevel by remember { mutableStateOf("") }

    val usersRef = db.getReference("UsersInfo")

    // Fetch data from Firebase
    LaunchedEffect(userName) { // Ensures this runs only once for the user
        usersRef.child(userName).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                age = snapshot.child("Age").getValue<Int>() ?: 0
                description = snapshot.child("Description").getValue<String>().orEmpty()
                dungeonsSpotted = snapshot.child("DungeonsSpotted").getValue<Int>() ?: 0
                powerLevel = snapshot.child("PowerLevel").getValue<String>().orEmpty()
            } else {
                println("User not found.")
            }
        }.addOnFailureListener {
            println("Error: ${it.message}")
        }
    }

    // --------------------------------------------------------------------------------------

    // Image as background
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Image(
            painter = painterResource(id = R.drawable.huntersguildgate),
            contentDescription = "Background Image",
            modifier = Modifier
                .fillMaxSize(), // Make the image fill the entire screen
            contentScale = ContentScale.Crop // Ensure the image covers the entire area
        )


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Rotating profile picture
            Image(
                painter = painterResource(id = R.drawable.usericon),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(40.dp)) // Space between image and text

            // Place text below the image
            Text(
                text = "Name: $userName",
                fontSize = 30.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp)) // Adjusted spacing
            Text(
                text = "Age: $age",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(1.dp)) // Adjusted spacing
            Text(
                text = "Description: $description",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(1.dp)) // Adjusted spacing
            Text(
                text = "Dungeons Spotted: $dungeonsSpotted",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(1.dp)) // Adjusted spacing
            Text(
                text = "Power Level: $powerLevel",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
            )
        }
    }


    // Home Screen
    //Button(onClick = { navController.navigate("home_screen/$id") }) {
    //    Text("Home")
    //}
}
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
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

    var age = 0
    var description = "0"
    var dungeonsSpotted = 0
    var powerLevel = "0"
    println(userName)

    val usersRef = db.getReference("UsersInfo")
    usersRef.child(userName).get().addOnSuccessListener { snapshot ->
        if (snapshot.exists()) {
            age = snapshot.child("Age").getValue<Int>()!!
            description = snapshot.child("Description").getValue<String>().toString()
            dungeonsSpotted = snapshot.child("DungeonsSpotted").getValue<Int>()!!
            powerLevel = snapshot.child("PowerLevel").getValue<String>().toString()
        } else {
            println("User not found.")
        }
    }

    // --------------------------------------------------------------------------------------


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
                .size(150.dp)
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


    // Home Screen
    //Button(onClick = { navController.navigate("home_screen/$id") }) {
    //    Text("Home")
    //}
}
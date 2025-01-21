package com.example.sololeveling.ui.screens

import android.os.CountDownTimer
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sololeveling.R
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
fun Storage(
    navController: NavController,
    id: Int,
    db: FirebaseDatabase,
    userName: String,
    context: Context // Pass context to access SensorManager
) {

    var userName2 by remember { mutableStateOf(userName) }
    // Use state to hold values
    var firstTime by remember { mutableStateOf(0) }
    var age by remember { mutableStateOf(0) }
    var description by remember { mutableStateOf("") }
    var dungeonsSpotted by remember { mutableStateOf(0) }
    var powerLevel by remember { mutableStateOf("") }
    var scanning_string by remember { mutableStateOf("Scanning") }
    var wait by remember { mutableStateOf(true) }
    var scanning by remember { mutableStateOf(false) }
    var magneticField by remember { mutableDoubleStateOf(0.0) }
    var edit by remember { mutableStateOf(false) }
    var age2 by remember { mutableStateOf(age.toString()) }
    var dungeonsSpotted2 by remember { mutableStateOf(dungeonsSpotted.toString()) }

    val usersRef = db.getReference("UsersInfo")

    if(firstTime == 0){
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
        firstTime +=1
    }


    // --------------------------------------------------------------------------------------

    // Image as background
    Box(
        modifier = Modifier.fillMaxSize()
    ) {


        Image(
            painter = painterResource(id = R.drawable.background8),
            contentDescription = "Background Image",
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logout),
                contentDescription = "logout Image",
                modifier = Modifier
                    .align(Alignment.End)
                    .size(50.dp)
                    .clickable {navController.navigate("home_screen/?6&username=")  }

            )
            // Rotating profile picture
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(40.dp)) // Space between image and text


                // Place text below the image
                Text(
                    text = "$userName2",
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            if(!edit){
                Spacer(modifier = Modifier.height(1.dp)) // Adjusted spacing
                PowerLevel(powerLevel)
                Spacer(modifier = Modifier.height(2.dp)) // Adjusted spacing

                if(wait && scanning){
                    Text(scanning_string, color = Color.White)
                }else if(!wait){
                    measureMagneticField(context) { magneticField = it }
                    when(magneticField){
                        in 0.0 .. 33.0 -> {
                            powerLevel = "E"
                        }

                        in 33.0 .. 66.0 -> {
                            powerLevel = "D"
                        }

                        in 66.0 .. 100.0 -> {
                            powerLevel = "C"
                        }

                        in 100.0 .. 133.0 -> {
                            powerLevel = "B"
                        }

                        in 133.0 .. 166.0 ->{
                            powerLevel = "A"
                        }

                        in 166.0 .. 200.0 -> {
                            powerLevel = "S"
                        }
                        else -> {
                            Text("Erro: + $magneticField")
                            powerLevel = ""
                        }

                    }
                    val updateInfo = mapOf(
                        "Age" to age,
                        "Name" to userName2,
                        "Description" to description,
                        "DungeonsSpotted" to dungeonsSpotted.toInt(),
                        "PowerLevel" to powerLevel
                    )
                    usersRef.child(userName).updateChildren(updateInfo)
                        .addOnSuccessListener { /* Handle success if needed */ }
                        .addOnFailureListener { /* Handle error if needed */ }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Spacer(modifier = Modifier.height(16.dp))




            }
            Spacer(modifier = Modifier.height(8.dp)) // Adjusted spacing
            if(!edit){
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp), // Add padding for a clean look
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = "User Details",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    // User Information Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)), // Dark background for the card
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp) // Space between rows
                        ) {
                            // Age Row
                            InfoRow(label = "Age:", value = "$age")

                            // Dungeons Spotted Row
                            InfoRow(label = "Dungeons Spotted:", value = "$dungeonsSpotted")

                            // Description Row
                            InfoRow(label = "Description:", value = "$description")
                        }
                    }
                }
            }

            if (edit) {
                val originalAge = remember { age } // Guardar o valor original de age
                val originalDescription = remember { description } // Guardar o valor original da descrição

                // Input fields
                OutlinedTextField(
                    value = age2,
                    onValueChange = { age2 = it },
                    label = { Text("Age", color = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        cursorColor = Color.White
                    ),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", color = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        cursorColor = Color.White
                    ),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons in a Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            // Cancel: Restore original values
                            edit = false
                            age2 = originalAge.toString()
                            description = originalDescription
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f))
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            // Save: Apply changes
                            edit = false
                            age = age2.toInt()
                            val updateInfo = mapOf(
                                "Age" to age,
                                "Name" to userName2,
                                "Description" to description,
                                "DungeonsSpotted" to dungeonsSpotted.toInt(),
                                "PowerLevel" to powerLevel
                            )
                            usersRef.child(userName).updateChildren(updateInfo)
                                .addOnSuccessListener { /* Handle success if needed */ }
                                .addOnFailureListener { /* Handle error if needed */ }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f))
                    ) {
                        Text("Save")
                    }
                }
            } else {

                    Button(onClick = {edit = true},
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f))

                    ) {

                        Text("Edit Profile")
                    }
                    //Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        val timer = object: CountDownTimer(5000, 500) {
                            override fun onTick(millisUntilFinished: Long) {
                                when(scanning_string){
                                    "Scanning...." -> scanning_string = "Scanning"
                                    "Scanning..." -> scanning_string = "Scanning...."
                                    "Scanning.." -> scanning_string = "Scanning..."
                                    "Scanning." -> scanning_string = "Scanning.."
                                    "Scanning" -> scanning_string = "Scanning."
                                }
                            }

                            override fun onFinish() {
                                wait = false
                                scanning = false
                            }
                        }
                        scanning = true
                        wait = true
                        timer.start()


                    },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f))
                    ){
                        Text("Measure Power")
                    }


                Spacer(modifier = Modifier.height(50.dp))
                Button(onClick = { navController.navigate("map_screen/?$id&username=$userName2") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f))
                ) {
                    Text("Back")
                }
            }
        }
    }



    // Home Screen
    //Button(onClick = { navController.navigate("home_screen/$id") }) {
    //    Text("Home")
    //}

}


@Composable
fun PowerLevel(power : String){
//    Text(
//        text = "Power Level: ",
//        fontSize = 20.sp,
//        textAlign = TextAlign.Center,
//    )

    when(power){
        "E" -> Text(text="E rank",
            style = TextStyle(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF808080), Color(0xFFB0C4DE)) // Gradient colors
                ))
        )

        "D" -> Text(text="D rank",
            style = TextStyle(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF1E90FF), Color(0xFF808080)) // Gradient colors
                ))
        )

        "C" -> Text(text="C rank",
            style = TextStyle(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF32CD32), Color(0xFF1E90FF)) // Gradient colors
                ))
        )

        "B" -> Text(text="B rank",
            style = TextStyle(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFD700), Color(0xFF32CD32)) // Gradient colors
                ))
        )

        "A" -> Text(text="A rank",
            style = TextStyle(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFA500), Color(0xFFFFD700)) // Gradient colors
                ))
        )

        "S" -> Text(text="S rank",
            style = TextStyle(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFD700), Color(0xFFFF4500)) // Gradient colors
                ))
        )


        else -> Text("-")
    }

}

// Reusable composable for rows
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween, // Spread items apart
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
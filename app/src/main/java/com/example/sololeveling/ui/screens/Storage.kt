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
import androidx.compose.runtime.mutableDoubleStateOf

@Composable
fun Storage(
    navController: NavController,
    id: Int,
    db: FirebaseDatabase,
    userName: String,
    context: Context // Pass context to access SensorManager
) {

    // Use state to hold values
    var age by remember { mutableStateOf(0) }
    var description by remember { mutableStateOf("") }
    var dungeonsSpotted by remember { mutableStateOf(0) }
    var powerLevel by remember { mutableStateOf("") }
    var scanning_string by remember { mutableStateOf("Scanning") }
    var wait by remember { mutableStateOf(true) }
    var scanning by remember { mutableStateOf(false) }
    var magneticField by remember { mutableDoubleStateOf(0.0) }

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
        Button(onClick = { navController.navigate("home_screen/$id?username=$userName\"") }) {
            Text("Home")
        }

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
            PowerLevel(powerLevel)
            Spacer(modifier = Modifier.height(2.dp)) // Adjusted spacing

            if(wait && scanning){
                Text(scanning_string)
            }else if(!wait){
                measureMagneticField(context) { magneticField = it }
                when(magneticField){
                    in 0.0 .. 33.0 -> Text(text="E rank",
                        style = TextStyle(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF808080), Color(0xFFB0C4DE)) // Gradient colors
                            ))
                    )

                    in 33.0 .. 66.0 -> Text(text="D rank",
                        style = TextStyle(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF1E90FF), Color(0xFF808080)) // Gradient colors
                            ))
                    )

                    in 66.0 .. 100.0 -> Text(text="C rank",
                        style = TextStyle(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF32CD32), Color(0xFF1E90FF)) // Gradient colors
                            ))
                    )

                    in 100.0 .. 133.0 -> Text(text="B rank",
                        style = TextStyle(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFFFD700), Color(0xFF32CD32)) // Gradient colors
                            ))
                    )

                    in 133.0 .. 166.0 -> Text(text="A rank",
                        style = TextStyle(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFFFA500), Color(0xFFFFD700)) // Gradient colors
                            ))
                    )

                    in 166.0 .. 200.0 -> Text(text="S rank",
                        style = TextStyle(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFFFD700), Color(0xFFFF4500)) // Gradient colors
                            ))
                    )


                    else -> Text("Erro: + $magneticField")
                }

            }
            Spacer(modifier = Modifier.height(16.dp))
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


            }){
                Text("Measure Power")
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
    Text(
        text = "Power Level: ",
        fontSize = 20.sp,
        textAlign = TextAlign.Center,
    )

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
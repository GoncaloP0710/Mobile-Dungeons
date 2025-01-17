package com.example.sololeveling.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.CountDownTimer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.ImageLoader
import com.example.sololeveling.R
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Portal(
    navController: NavController,
    id: Int,
    context: Context // Pass context to access SensorManager
){
    var magneticField by remember { mutableDoubleStateOf(0.0) }
    var wait by remember { mutableStateOf(true) }
    

    val timer = object: CountDownTimer(2000, 100) {
        override fun onTick(millisUntilFinished: Long) {

        }

        override fun onFinish() {wait = false}
    }
    timer.start()
    Scaffold (
        topBar = {
            // You can add a top bar if necessary
            Button(onClick = { navController.navigate("home_screen/$id") }) {
                Text("Home")
            }
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                if(wait){
                    Text("Scanning...")
                }else{
                    measureMagneticField(context) { magneticField = it }
                    when(magneticField){
                        in -100.0 .. -67.0 -> Text(text="E rank",
                            style = TextStyle(
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFff6f61), Color(0xFF6a1b9a)) // Gradient colors
                                )))

                        in -67.0 .. -34.0 -> Text(text="D rank",
                            style = TextStyle(
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFff6f61), Color(0xFF6a1b9a)) // Gradient colors
                                )))

                        in -34.0 .. 0.0 -> Text(text="C rank",
                            style = TextStyle(
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFff6f61), Color(0xFF6a1b9a)) // Gradient colors
                                )))

                        in 0.0 .. 33.0 -> Text(text="B rank",
                            style = TextStyle(
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFff6f61), Color(0xFF6a1b9a)) // Gradient colors
                                )))

                        in 33.0 .. 66.0 -> Text(text="A rank",
                            style = TextStyle(
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFff6f61), Color(0xFF6a1b9a)) // Gradient colors
                                )))

                        in 66.0 .. 100.0 -> Text(text="S rank",
                            style = TextStyle(
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFff6f61), Color(0xFF6a1b9a)) // Gradient colors
                                )))

                        else -> Text("Erro: + $magneticField")
                    }
                    Spacer(Modifier.size(32.dp))
                    Button(onClick = { navController.navigate("dailies_screen/$id") }) {
                        Text("Enter Portal")
                    }
                }

            }

        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    //.align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f)) // Semi-transparent background
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
    )

}
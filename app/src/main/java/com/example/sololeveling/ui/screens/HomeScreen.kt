package com.example.sololeveling.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.sololeveling.R
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    id: Int,
    db: FirebaseDatabase,
    notlogged: Boolean = true
) {
    var showDialog by rememberSaveable { mutableStateOf(notlogged) }
    var username by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginSuccessful by remember { mutableStateOf(false) }
    var loginErrorMessage by remember { mutableStateOf("") }

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
                .padding(16.dp) // Adjust padding as needed
                .fillMaxWidth(),
            horizontalAlignment = Alignment.End // Align content to the start (left)

        ) {
            Box(modifier = Modifier.clickable { navController.navigate("storage_screen/$id?username=$username") }) {
                Image(
                    painter = painterResource(id = R.drawable.usericon),
                    contentDescription = "Icon Image",
                    modifier = Modifier
                        .size(160.dp) // Adjust size as needed
                )
            }
        }

        Image(
            painter = painterResource(id = R.drawable.userfullbody),
            contentDescription = "Home Image",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .fillMaxSize()
                .height(250.dp), // Adjust height as needed
        )

        // Login Dialog
        if (showDialog) {
            Dialog(onDismissRequest = { /* Do nothing to keep it open until valid login */ }) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .wrapContentSize()
                ) {

                    // Username input
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username", color = Color.White) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White,
                            cursorColor = Color.White // Optionally, change the cursor color
                        ),
                    )

                    // Password input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = Color.White) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White,
                            cursorColor = Color.White // Optionally, change the cursor color
                        ),
                        visualTransformation = PasswordVisualTransformation()
                    )

                    // Show login error message if any
                    if (loginErrorMessage.isNotEmpty()) {
                        Text(text = loginErrorMessage, color = Color.Red)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Login Button
                    Button(onClick = {
                        loginUser(db, username, password, {
                            showDialog = false
                            isLoginSuccessful = true
                        }, { errorMessage ->
                            loginErrorMessage = errorMessage
                        })
                    }) {
                        Text("Login")
                    }
                }
            }
        }

        // Buttons at the bottom of the screen, overlaid on top of the image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
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

                Button(onClick = { navController.navigate("portal_screen/$id") }) {
                    Text("Portal")
                }
            }
        }
    }
}

fun loginUser(
    db: FirebaseDatabase,
    username: String,
    password: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val usersRef = db.getReference("Users")
    usersRef.child(username).get().addOnSuccessListener { snapshot ->
        if (snapshot.exists()) {
            val storedPassword = snapshot.child("Pass").getValue<String>()
            if (storedPassword == password) {
                onSuccess() // Successfully logged in
            } else {
                onError("Incorrect password.")
            }
        } else {
            onError("User not found.")
        }
    }.addOnFailureListener {
        onError("Login failed. Please try again.")
    }
}

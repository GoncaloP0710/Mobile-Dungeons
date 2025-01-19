package com.example.sololeveling.ui.screens

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
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
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    id: Int,
    db: FirebaseDatabase,
    notlogged: Boolean = true,
    user: String = ""
) {
    println("USeR: " + user)
    var user2 = user
    if(user == "placeholder"){
        user2 = ""
    }
    println("2: "+user2)
    var showDialog by rememberSaveable { mutableStateOf(notlogged) }
    var username by rememberSaveable { mutableStateOf(user2) }
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
        println("DIALOG")
        println(username)
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

                    // Buttons in a horizontal row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        // Login Button
                        Button(onClick = {
                            println("LOGIN")
                            println(username)
                            println(password)

                            loginUser(db, username, password, {
                                showDialog = false
                                isLoginSuccessful = true
                            }, { errorMessage ->
                                loginErrorMessage = errorMessage
                            })
                        }) {
                            Text("Login")
                        }

                        // Register Button
                        Button(onClick = {
                            val newUser = mapOf(
                                "Name" to username,
                                "Pass" to password
                            )
                            db.reference.child("Users").child(username).setValue(newUser)
                                .addOnSuccessListener { /* Handle success if needed */ }
                                .addOnFailureListener { /* Handle error if needed */ }
                            showDialog = false
                        }) {
                            Text("Create Account")
                        }
                    }
                }
            }
        } else {
            ListenForFriendRequestsScreen(db, username)
            ListenForHelpRequestsScreen(db, username)
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
                println("USERNAME: $username")
                Button(onClick = { navController.navigate("map_screen/?$id&username=$username") }) {
                    Text("Map")
                }

                Button(onClick = { navController.navigate("dailies_screen/?$id&username=$username") }) {
                    Text("Dailies")
                }

                Button(onClick = { navController.navigate("guild_screen/?$id&username=$username") }) {
                    Text("Guild")
                }

                Button(onClick = { navController.navigate("portal_screen/?$id&username=$username") }) {
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

@Composable
fun ListenForFriendRequestsScreen(db: FirebaseDatabase, userName: String) {
    val showDialog = remember { mutableStateOf(false) }
    var newRequestName by remember { mutableStateOf("") }
    // Reference to the FriendRequests node for the current user
    val friendRequestsRef = db.reference.child("FriendRequests").child(userName)
    // Set up a listener for new friend requests
    DisposableEffect(friendRequestsRef) {
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // New friend request detected
                val requestName = snapshot.getValue(String::class.java)
                if (requestName != null && requestName.endsWith("@n")) {
                    // Update newRequestName for the UI
                    newRequestName = requestName.removeSuffix("@n")
                    showDialog.value = true
                    // Remove "@n" from the database
                    snapshot.ref.setValue(newRequestName)
                        .addOnSuccessListener {
                            Log.d("FirebaseUpdate", "Successfully removed '@n' from $requestName")
                        }
                        .addOnFailureListener {
                            Log.e("FirebaseUpdateError", it.message ?: "Error removing '@n'")
                        }
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", error.message)
            }
        }
        // Attach listener
        friendRequestsRef.addChildEventListener(listener)
        // Clean up when Composable is disposed
        onDispose {
            friendRequestsRef.removeEventListener(listener)
        }
    }
    // Show Notification Dialog when a new friend request arrives
    if (showDialog.value) {
        NotificationDialog(
            showDialog = showDialog,
            name = newRequestName
        )
    }
}

@Composable
fun NotificationDialog(showDialog: MutableState<Boolean>, name: String) {
    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        title = { Text("New Friend Request") },
        text = { Text("You have a new friend request from $name!") },
        confirmButton = {
            Button(onClick = { showDialog.value = false }) {
                Text("Dismiss")
            }
        }
    )
}

@Composable
fun ListenForHelpRequestsScreen(db: FirebaseDatabase, userName: String) {
    val showDialog = remember { mutableStateOf(false) }
    var helpRequester by remember { mutableStateOf("") }
    var helpRequestTime by remember { mutableStateOf("") }

    // Reference to the UserHelp node for the current user
    val userHelpRef = db.reference.child("UserHelp").child(userName)

    DisposableEffect(userHelpRef) {
        val listener = object : ChildEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // New help request detected
                val requestTime = snapshot.getValue(String::class.java)
                if (requestTime != null) {
                    // Parse the request time from Firebase
                    val requestDateTime = LocalDateTime.parse(requestTime, DateTimeFormatter.ISO_DATE_TIME)
                    val now = LocalDateTime.now()

                    // Check if the request is within the last 5 minutes
                    if (Duration.between(requestDateTime, now).toMinutes() <= 5) {
                        helpRequester = snapshot.key ?: "Unknown"
                        helpRequestTime = requestTime
                        showDialog.value = true
                    } else {
                        // Outdated request: Remove from Firebase
                        snapshot.ref.removeValue()
                            .addOnSuccessListener {
                                Log.d("FirebaseCleanup", "Discarded outdated help request from $helpRequester")
                            }
                            .addOnFailureListener {
                                Log.e("FirebaseCleanupError", it.message ?: "Error discarding outdated request")
                            }
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", error.message)
            }
        }

        // Attach the listener
        userHelpRef.addChildEventListener(listener)

        // Clean up when Composable is disposed
        onDispose {
            userHelpRef.removeEventListener(listener)
        }
    }

    // Show Notification Dialog when a new help request arrives
    if (showDialog.value) {
        HelpNotificationDialog(
            showDialog = showDialog,
            name = helpRequester,
            time = helpRequestTime
        )
    }
}


@Composable
fun HelpNotificationDialog(
    showDialog: MutableState<Boolean>,
    name: String,
    time: String
) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
        cameraManager.getCameraCharacteristics(id)
            .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
    }

    LaunchedEffect(showDialog.value) {
        if (showDialog.value) {
            // Vibration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(500)
            }

            // Flashlight toggle
            if (cameraId != null) {
                try {
                    repeat(6) { // Flash 3 times
                        cameraManager.setTorchMode(cameraId, true) // Turn on
                        delay(300) // 300ms
                        cameraManager.setTorchMode(cameraId, false) // Turn off
                        delay(300)
                    }
                } catch (e: Exception) {
                    Log.e("FlashlightError", e.message ?: "Error toggling flashlight")
                }
            }
        } else if (cameraId != null) {
            // Ensure flashlight is off
            cameraManager.setTorchMode(cameraId, false)
        }
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = {
                Text(text = "Help Request")
            },
            text = {
                Column {
                    Text(text = "User $name is requesting help.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Request Time: $time")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    Log.d("HelpRequest", "Accepted help request from $name")
                    showDialog.value = false
                }) {
                    Text(text = "Help")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog.value = false
                }) {
                    Text(text = "Dismiss")
                }
            }
        )
    }
}


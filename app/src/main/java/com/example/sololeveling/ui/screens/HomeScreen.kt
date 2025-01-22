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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay
import org.osmdroid.views.MapView
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
    var user2 = if (user == "placeholder") "" else user
    var showDialog by rememberSaveable { mutableStateOf(notlogged) }
    var username by rememberSaveable { mutableStateOf(user2) }
    var password by remember { mutableStateOf("") }
    var isLoginSuccessful by remember { mutableStateOf(false) }
    var loginErrorMessage by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.background8),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize().alpha(0.8f),
            contentScale = ContentScale.Crop
        )

        // Login Dialog

            Dialog(onDismissRequest = { } ) {
                Column(
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxWidth()
                        .background(Color(0xAA000000), RoundedCornerShape(16.dp))
                        .padding(32.dp)
                ) {
                    // Título
                    Text(
                        text = "Login",
                        color = Color.White,
                        style = TextStyle(fontSize = 24.sp),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

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
                            cursorColor = Color.White
                        ),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = Color.White) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White,
                            cursorColor = Color.White
                        ),
                        visualTransformation = PasswordVisualTransformation()
                    )

                    // Show login error message if any
                    if (loginErrorMessage.isNotEmpty()) {
                        Text(
                            text = loginErrorMessage,
                            color = Color.Red,
                            style = TextStyle(fontSize = 14.sp),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Login Button
                        Button(
                            onClick = {
                                loginUser(db, username, password, {
                                    navController.navigate("map_screen/?$id&username=$username")
                                    showDialog = false
                                    isLoginSuccessful = true
                                }, { errorMessage ->
                                    loginErrorMessage = errorMessage
                                })
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan.copy(alpha = 0.15f))
                        ) {
                            Text("Sign In")
                        }

                        Button(
                            onClick = {
                                val newUser = mapOf("Name" to username, "Pass" to password)
                                db.reference.child("Users").child(username).setValue(newUser)
                                    .addOnSuccessListener {
                                        navController.navigate("map_screen/?$id&username=$username")
                                        showDialog = false
                                    }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan.copy(alpha = 0.15f))
                        ) {
                            Text("Sign Up")
                        }
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
    // Validação para impedir nome de usuário ou senha vazios
    if (username.isBlank()) {
        onError("Username cannot be empty.")
        return
    }
    if (password.isBlank()) {
        onError("Password cannot be empty.")
        return
    }

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
fun ListenForHelpRequestsScreen(db: FirebaseDatabase, userName: String, mapView: MapView) {
    val showDialog = remember { mutableStateOf(false) }
    var helpRequester by remember { mutableStateOf("") }
    var helpRequestTime by remember { mutableStateOf("") }
    var requesterLatitude by remember { mutableStateOf(0.0) }
    var requesterLongitude by remember { mutableStateOf(0.0) }

    // Set to track processed help requests
    val processedRequests = remember { mutableStateOf(mutableSetOf<String>()) }

    // Reference to the UserHelp node for the current user
    val userHelpRef = db.reference.child("UserHelp").child(userName)

    DisposableEffect(userHelpRef) {
        val listener = object : ChildEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val requestTime = snapshot.getValue(String::class.java)
                val requestKey = snapshot.key

                if (requestTime != null && requestKey != null) {
                    if (requestKey !in processedRequests.value) {
                        val requestDateTime = LocalDateTime.parse(requestTime, DateTimeFormatter.ISO_DATE_TIME)
                        val now = LocalDateTime.now()

                        if (Duration.between(requestDateTime, now).toMinutes() <= 5) {
                            // Retrieve user position from database
                            db.reference.child("UserPosition").child(requestKey)
                                .get()
                                .addOnSuccessListener { positionSnapshot ->
                                    val latitude = positionSnapshot.child("latitude").getValue(Double::class.java)
                                    val longitude = positionSnapshot.child("longitude").getValue(Double::class.java)

                                    if (latitude != null && longitude != null) {
                                        requesterLatitude = latitude
                                        requesterLongitude = longitude
                                        helpRequester = requestKey
                                        helpRequestTime = requestTime
                                        showDialog.value = true
                                        processedRequests.value.add(requestKey) // Mark request as processed
                                    }
                                }
                        } else {
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
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", error.message)
            }
        }

        userHelpRef.addChildEventListener(listener)

        onDispose {
            userHelpRef.removeEventListener(listener)
        }
    }

    // Show Notification Dialog when a new help request arrives
    if (showDialog.value) {
        HelpNotificationDialog(
            showDialog = showDialog,
            name = helpRequester,
            time = helpRequestTime,
            onHelpClick = {
                moveMapToCoordinates(mapView, requesterLatitude, requesterLongitude)
                showDialog.value = false
            }
        )

        // Remove the processed request from Firebase after dialog confirmation
        userHelpRef.child(helpRequester).removeValue()
            .addOnSuccessListener {
                Log.d("FirebaseCleanup", "Removed processed help request from $helpRequester")
            }
            .addOnFailureListener {
                Log.e("FirebaseCleanupError", it.message ?: "Error removing processed request")
            }
    }
}

@Composable
fun HelpNotificationDialog(
    showDialog: MutableState<Boolean>,
    name: String,
    time: String,
    onHelpClick: () -> Unit
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
                    onHelpClick()
                    Log.d("HelpRequest", "Accepted help request from $name")
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

@Composable
fun ListenForDungeonInviteScreen(db: FirebaseDatabase, userName: String, mapView: MapView) {
    val showDialog = remember { mutableStateOf(false) }
    var inviteRequester by remember { mutableStateOf("") }
    var requesterLatitude by remember { mutableStateOf(0.0) }
    var requesterLongitude by remember { mutableStateOf(0.0) }
    val inviteQueue = remember { mutableStateListOf<Pair<String, String>>() } // Queue for invites

    // Reference to the UserPortalInvite node for the current user
    val userInviteRef = db.reference.child("UserPortalInvite").child(userName)

    DisposableEffect(userInviteRef) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { child ->
                    val inviteKey = child.key
                    val inviteValue = child.getValue(String::class.java)

                    if (inviteKey != null && inviteValue != null) {
                        // Add invite to the queue if not already present
                        if (!inviteQueue.any { it.first == inviteKey }) {
                            inviteQueue.add(Pair(inviteKey, inviteValue))
                        }
                    }
                }

                // Show the dialog for the first invite in the queue
                if (!showDialog.value && inviteQueue.isNotEmpty()) {
                    processNextInvite(
                        db, userInviteRef, inviteQueue, showDialog,
                        { name -> inviteRequester = name },
                        { lat -> requesterLatitude = lat },
                        { lon -> requesterLongitude = lon }
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", error.message)
            }
        }

        userInviteRef.addValueEventListener(listener)

        onDispose {
            userInviteRef.removeEventListener(listener)
        }
    }

    // Show the dialog when an invite is ready
    if (showDialog.value) {
        InviteDungeonDialog(
            showDialog = showDialog,
            name = inviteRequester,
            onAccept = {
                showDialog.value = false // Close the dialog
                moveMapToCoordinates(mapView, requesterLatitude, requesterLongitude) // Move map to invite location

                // Process the next invite after accepting
                if (inviteQueue.isNotEmpty()) {
                    processNextInvite(
                        db, userInviteRef, inviteQueue, showDialog,
                        { name -> inviteRequester = name },
                        { lat -> requesterLatitude = lat },
                        { lon -> requesterLongitude = lon }
                    )
                }
            }
        )
    }
}

fun processNextInvite(
    db: FirebaseDatabase,
    userInviteRef: DatabaseReference,
    inviteQueue: MutableList<Pair<String, String>>,
    showDialog: MutableState<Boolean>,
    updateRequester: (String) -> Unit,
    updateLatitude: (Double) -> Unit,
    updateLongitude: (Double) -> Unit
) {
    if (inviteQueue.isEmpty()) return // Do nothing if the queue is empty

    val currentInvite = inviteQueue.first() // Get the first invite in the queue
    val (inviteKey, inviteValue) = currentInvite

    // Update the inviteRequester for the dialog
    updateRequester(inviteValue)

    // Fetch position from UserPosition for the inviteRequester
    db.reference.child("UserPosition").child(inviteValue)
        .get()
        .addOnSuccessListener { positionSnapshot ->
            updateLatitude(positionSnapshot.child("latitude").getValue(Double::class.java) ?: 0.0)
            updateLongitude(positionSnapshot.child("longitude").getValue(Double::class.java) ?: 0.0)

            // Show the dialog after coordinates are updated
            showDialog.value = true
        }
        .addOnFailureListener {
            Log.e("InviteDebug", "Failed to fetch position: ${it.message}")
        }

    // Remove the invite from Firebase only after the dialog is closed
    userInviteRef.child(inviteKey).removeValue()
        .addOnSuccessListener {
            Log.d("FirebaseCleanup", "Removed processed invite: $inviteKey")
            inviteQueue.removeFirstOrNull() // Remove invite from the queue after successful deletion
        }
        .addOnFailureListener {
            Log.e("FirebaseCleanupError", "Error removing invite: ${it.message}")
        }
}



@Composable
fun InviteDungeonDialog(
    showDialog: MutableState<Boolean>,
    name: String,
    onAccept: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        title = { Text("New Friend Invite") },
        text = { Text("You have a new invite for a dungeon from $name!") },
        confirmButton = {
            TextButton(onClick = {
                onAccept()
                Log.d("DungeonInvite", "Accepted dungeon invite from $name")
            }) {
                Text(text = "Accept")
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





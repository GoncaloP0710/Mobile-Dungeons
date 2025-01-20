package com.example.sololeveling.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import com.example.sololeveling.R
import com.google.firebase.database.ktx.getValue


@Composable
fun Guild(
    navController: NavController,
    id: Int,
    db: FirebaseDatabase,
    userName: String
) {
    val name = userName

    var friendsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var friendRequestName by remember { mutableStateOf("") }
    var friendRequests by remember { mutableStateOf<List<String>>(emptyList()) }

    // Reference to the user's friends in Firebase
    val userFriendsRef = db.reference.child("UserFriendsList").child(userName)
    val userFriendRequestsRef = db.reference.child("FriendRequests").child(userName)

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    // Set up a real-time listener for the friends list
    DisposableEffect(userFriendsRef) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val friends = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                friendsList = friends
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", error.message)
            }
        }
        userFriendsRef.addValueEventListener(listener)
        onDispose { userFriendsRef.removeEventListener(listener) }
    }

    // Set up a real-time listener for friend requests
    DisposableEffect(userFriendRequestsRef) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requests = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()
                friendRequests = requests
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", error.message)
            }
        }
        userFriendRequestsRef.addValueEventListener(listener)
        onDispose { userFriendRequestsRef.removeEventListener(listener) }
    }

    var showFriendsList by remember { mutableStateOf(false) }
    var showFriendRequests by remember { mutableStateOf(false) }
    var showSentRequests by remember { mutableStateOf(false) }

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
                .padding(16.dp) // Adjust padding as needed
                .fillMaxWidth(),
        ) {

            // State to track the active button
            var activeButton by remember { mutableStateOf("") }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Friends List Button
                Button(
                    onClick = {
                        showFriendsList = true
                        showFriendRequests = false
                        showSentRequests = false
                        activeButton = "friendsList" // Set the active button
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan.copy(alpha = 0.15f))
                ) {
                    Text("List", fontSize = 16.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Friend Requests Button
                Button(
                    onClick = {
                        showFriendsList = false
                        showFriendRequests = true
                        showSentRequests = false
                        activeButton = "friendRequests" // Set the active button
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan.copy(alpha = 0.15f))
                ) {
                    Text("Requests", fontSize = 14.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Send Requests Button
                Button(
                    onClick = {
                        showFriendsList = false
                        showFriendRequests = false
                        showSentRequests = true
                        activeButton = "sendRequests" // Set the active button
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan.copy(alpha = 0.15f))
                ) {
                    Text("Add", fontSize = 16.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Friends List Section
            if (showFriendsList) {

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)), // Semi-transparent card background
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.900f)
                        ) {
                            LazyColumn(
                                modifier = Modifier.padding(8.dp)
                            ) {
                                items(friendsList) { friend ->
                                    // Use a state to store the power level for each friend
                                    var powerLevel by remember { mutableStateOf<String>("") }

                                    // Fetch data from Firebase
                                    val usersRef = db.getReference("UsersInfo")
                                    LaunchedEffect(friend) { // Launching the effect when a new friend is encountered
                                        usersRef.child(friend).get().addOnSuccessListener { snapshot ->
                                            if (snapshot.exists()) {
                                                powerLevel =
                                                    snapshot.child("PowerLevel").getValue<String>()
                                                        .orEmpty()
                                            } else {
                                                powerLevel = "Rank not found"
                                            }
                                        }.addOnFailureListener {
                                            println("Error: ${it.message}")
                                            powerLevel = "Error fetching data"
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween, // Distribute space between items
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        // Left side: Friend's name and image
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.profile), // Replace with your image resource
                                                contentDescription = "User Icon",
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .padding(end = 8.dp)
                                            )
                                            Text(
                                                text = friend,
                                                fontSize = 18.sp,
                                                color = Color.White,
                                                modifier = Modifier.padding(4.dp)
                                            )
                                        }

                                        // Right side: Power level
                                        Text(
                                            text = "Power Level: $powerLevel", // Show power level on the right
                                            fontSize = 18.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(4.dp)
                                        )
                                    }
                                }
                            }
                    }
                }
            }

            // Friend Requests Section
            if (showFriendRequests) {

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)), // Semi-transparent card background
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.900f)
                        ) {
                            LazyColumn(
                                modifier = Modifier.padding(8.dp)
                            ) {
                                items(friendRequests.map { it.removeSuffix("@n") }) { request ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.profile), // Replace with your image resource
                                            contentDescription = "User Icon",
                                            modifier = Modifier
                                                .size(40.dp)
                                                .padding(end = 8.dp)
                                        )
                                        Text(
                                            text = request,
                                            fontSize = 18.sp,
                                            color = Color.White,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                // Accept friend request
                                                val requestRef =
                                                    db.reference.child("FriendRequests").child(userName)
                                                requestRef.get().addOnSuccessListener { snapshot ->
                                                    val requests = snapshot.getValue(object :
                                                        GenericTypeIndicator<List<String>>() {})
                                                        ?: emptyList()

                                                    // Remove the original request with the '@n' part
                                                    val updatedRequests =
                                                        requests.filterNot { it == "$request@n" }
                                                            .filterNot { it == request } // Properly remove the entire request

                                                    // Remove the request from Firebase once accepted
                                                    requestRef.setValue(updatedRequests)

                                                    // Add the user to both users' friend lists
                                                    val userFriendsRef =
                                                        db.reference.child("UserFriendsList")
                                                            .child(userName)
                                                    val friendFriendsRef =
                                                        db.reference.child("UserFriendsList")
                                                            .child(request)

                                                    userFriendsRef.get()
                                                        .addOnSuccessListener { userSnapshot ->
                                                            val userFriends =
                                                                userSnapshot.getValue(object :
                                                                    GenericTypeIndicator<List<String>>() {})
                                                                    ?: emptyList()
                                                            userFriendsRef.setValue(userFriends + request)
                                                        }

                                                    friendFriendsRef.get()
                                                        .addOnSuccessListener { friendSnapshot ->
                                                            val friendFriends =
                                                                friendSnapshot.getValue(object :
                                                                    GenericTypeIndicator<List<String>>() {})
                                                                    ?: emptyList()
                                                            friendFriendsRef.setValue(friendFriends + userName)
                                                        }

                                                    // Optionally, delete the request from the request list of the friend
                                                    val friendRequestRef =
                                                        db.reference.child("FriendRequests")
                                                            .child(request)
                                                    friendRequestRef.get()
                                                        .addOnSuccessListener { friendSnapshot ->
                                                            val friendRequests =
                                                                friendSnapshot.getValue(object :
                                                                    GenericTypeIndicator<List<String>>() {})
                                                                    ?: emptyList()
                                                            val updatedFriendRequests =
                                                                friendRequests.filterNot { it == "$userName@n" }
                                                                    .filterNot { it == "$userName" } // Properly remove the entire request
                                                            friendRequestRef.setValue(
                                                                updatedFriendRequests
                                                            )
                                                        }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Cyan.copy(alpha = 0.15f)
                                            )
                                        ) {
                                            Text("Accept", fontSize = 16.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
            }

            // Sent Requests Section
            if (showSentRequests) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = friendRequestName,
                        onValueChange = { friendRequestName = it },
                        label = { Text("Enter username", color = Color.White)},
                        textStyle = TextStyle(color = Color.White),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White,
                            cursorColor = Color.White
                        ),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    var showDialog by remember { mutableStateOf(false) }
                    var dialogMessage by remember { mutableStateOf("") }

                    // AlertDialog to show notifications
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = { Text("Notification") },
                            text = { Text(dialogMessage) },
                            confirmButton = {
                                Button(onClick = { showDialog = false }) {
                                    Text("OK")
                                }
                            }
                        )
                    }

                    Button(
                        onClick = {
                            Log.d("FriendRequestDebug", "friendRequestName: $friendRequestName")
                            val name = friendRequestName

                            // Check if the friendRequestName is not blank or empty
                            if (friendRequestName.isNotBlank() && friendRequestName.isNotEmpty()) {

                                // Reference to the Users node in Firebase
                                val usersRef = db.reference.child("Users")

                                // Check if the user exists in the Firebase database
                                usersRef.child(friendRequestName).get().addOnSuccessListener { snapshot ->
                                    if (snapshot.exists()) {
                                        // User exists, retrieve user data
                                        val user = snapshot.getValue(User::class.java)

                                        if (user != null) {
                                            // Check if the user is already friends with the current user
                                            val targetFriendsRef = db.reference.child("UserFriendsList").child(friendRequestName)
                                            targetFriendsRef.get().addOnSuccessListener { snapshot ->
                                                val targetFriends = snapshot.children.mapNotNull { it.getValue(String::class.java) }

                                                if (targetFriends.contains(userName) || friendsList.contains(friendRequestName)) {
                                                    // Users are already friends
                                                    Log.d("FriendRequest", "Users are already friends.")
                                                    dialogMessage = "You are already friends."
                                                    showDialog = true
                                                } else {
                                                    // Add the friend request
                                                    val friendRequestsRef = db.reference.child("FriendRequests").child(friendRequestName)
                                                    friendRequestsRef.get().addOnSuccessListener { snapshot ->
                                                        val existingRequests = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()

                                                        if (existingRequests.contains(userName)) {
                                                            // Friend request already exists
                                                            Log.d("FriendRequest", "Friend request already exists.")
                                                            dialogMessage = "Friend request already exists."
                                                            showDialog = true
                                                        } else {
                                                            // Send friend request
                                                            val updatedRequests = existingRequests + (userName + "@n")
                                                            friendRequestsRef.setValue(updatedRequests)
                                                                .addOnSuccessListener {
                                                                    Log.d("FriendRequest", "Friend request sent to $friendRequestName")
                                                                    dialogMessage = "Friend request successfully sent!"
                                                                    showDialog = true
                                                                }
                                                                .addOnFailureListener {
                                                                    Log.e("FriendRequestError", it.message ?: "Error")
                                                                    dialogMessage = "Failed to send friend request."
                                                                    showDialog = true
                                                                }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // User does not exist
                                        Log.d("FriendRequest", "User $friendRequestName does not exist.")
                                        dialogMessage = "User does not exist."
                                        showDialog = true
                                    }
                                }.addOnFailureListener {
                                    Log.e("FirebaseError", "Error checking user existence: ${it.message}")
                                    dialogMessage = "Error checking user existence."
                                    showDialog = true
                                }
                            } else {
                                // Invalid input (blank or empty)
                                dialogMessage = "Invalid input. Please enter a valid username."
                                showDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan.copy(alpha = 0.15f))
                    ) {
                        Text("Send")
                    }
                }
            }
        }

        // Bottom Navigation Buttons
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f)) // Semi-transparent background
                .padding(25.dp)
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = { navController.navigate("map_screen/?$id&username=$name") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan.copy(alpha = 0.15f))) {
                    Text("Map", fontSize = 16.sp)
                }

//                Button(onClick = { navController.navigate("portal_screen/?$id&username=$name") }) {
//                    Text("Scan Portal", fontSize = 16.sp)
//                }

                Button(onClick = { navController.navigate("guild_screen/?$id&username=$name") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black)) {
                    Text("Friends", fontSize = 16.sp)
                }
            }
        }
    }
}

// Data class representing the user in the Firebase database
data class User(
    val Name: String = "",
    val Pass: String = ""
)
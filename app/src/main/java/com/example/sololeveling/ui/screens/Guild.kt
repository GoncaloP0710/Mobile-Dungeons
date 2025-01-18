package com.example.sololeveling.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

@Composable
fun Guild(
    navController: NavController,
    id: Int,
    db: FirebaseDatabase,
    userName: String
) {
    var friendsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var friendRequestName by remember { mutableStateOf("") }
    var friendRequests by remember { mutableStateOf<List<String>>(emptyList()) }

    // Reference to the user's friends in Firebase
    val userFriendsRef = db.reference.child("UserFriendsList").child(userName)
    val userFriendRequestsRef = db.reference.child("FriendRequests").child(userName)

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Home Screen Button
        Button(onClick = { navController.navigate("home_screen/$id") }) {
            Text("Home")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display the list of friends
        Text("Friends List:")
        LazyColumn {
            items(friendsList) { friend ->
                Text(friend)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display the list of friend requests ---------------------------------------------------
        Text("Friend Requests:")
        LazyColumn {
            items(friendRequests.map { it.removeSuffix("@n") }) { request -> // Remove "@n" suffix here
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(request, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        // Accept friend request
                        val requestRef = db.reference.child("FriendRequests").child(userName)
                        requestRef.get().addOnSuccessListener { snapshot ->
                            val requests = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()
                            val updatedRequests = requests.filter { it != "$request@n" } // Remove original request with "@n"
                            requestRef.setValue(updatedRequests)

                            // Add to both users' friend lists
                            val userFriendsRef = db.reference.child("UserFriendsList").child(userName)
                            val friendFriendsRef = db.reference.child("UserFriendsList").child(request)

                            userFriendsRef.get().addOnSuccessListener { userSnapshot ->
                                val userFriends = userSnapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()
                                userFriendsRef.setValue(userFriends + request)
                            }
                            friendFriendsRef.get().addOnSuccessListener { friendSnapshot ->
                                val friendFriends = friendSnapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()
                                friendFriendsRef.setValue(friendFriends + userName)
                            }
                        }
                    }) {
                        Text("Accept")
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        // Add Friend Request Section ---------------------------------------------------
        Text("Send Friend Request:")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = friendRequestName,
                onValueChange = { friendRequestName = it },
                label = { Text("Enter username") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    Log.d("FriendRequestDebug", "friendRequestName: $friendRequestName")
                    val name = friendRequestName

                    if (friendRequestName.isNotBlank() && friendRequestName.isNotEmpty()) {
                        // Check if already friends
                        val targetFriendsRef =
                            db.reference.child("UserFriendsList").child(friendRequestName)
                        targetFriendsRef.get().addOnSuccessListener { snapshot ->
                            val targetFriends = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                            if (targetFriends.contains(userName) || friendsList.contains(friendRequestName)) {
                                Log.d("FriendRequest", "Users are already friends.")
                            } else {
                                // Add the friend request
                                val friendRequestsRef =
                                    db.reference.child("FriendRequests").child(name)
                                friendRequestsRef.get().addOnSuccessListener { snapshot ->
                                    val existingRequests = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()
                                    if (existingRequests.contains(userName)) {
                                        Log.d("FriendRequest", "Friend request already exists.")
                                    } else {
                                        val updatedRequests = existingRequests + (userName + "@n")
                                        friendRequestsRef.setValue(updatedRequests)
                                            .addOnSuccessListener {
                                                Log.d("FriendRequest", "Friend request sent to $friendRequestName")
                                            }
                                            .addOnFailureListener {
                                                Log.e("FriendRequestError", it.message ?: "Error")
                                            }
                                    }
                                }
                            }
                        }
                        friendRequestName = "" // Clear input field
                    }
                }
            ) {
                Text("Send")
            }
        }
    }
}
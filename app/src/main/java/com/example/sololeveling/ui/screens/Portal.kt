package com.example.sololeveling.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sololeveling.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Portal(
    navController: NavController,
    id: Int,
    context: Context, // Pass context to access SensorManager
    name: String,
    db: FirebaseDatabase
){
    var name2 by remember { mutableStateOf(name) }
    var magneticField by remember { mutableDoubleStateOf(0.0) }
    var wait by remember { mutableStateOf(true) }
    var scanning_string by remember { mutableStateOf("Scanning") }

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

        override fun onFinish() {wait = false}
    }
    timer.start()
    Scaffold (

        topBar = {

        },
        content = {
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
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                if(wait){
                    Text(text = scanning_string,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White)
                }else{
                    measureMagneticField(context) { magneticField = it }
                    when(magneticField){
                        in 0.0 .. 33.0 -> Text(text="E rank",
                            style = TextStyle(
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF808080), Color(0xFFB0C4DE)) // Gradient colors
                                )))

                        in 33.0 .. 66.0 -> Text(text="D rank",
                            style = TextStyle(
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF1E90FF), Color(0xFF808080)) // Gradient colors
                                )))

                        in 66.0 .. 100.0 -> Text(text="C rank",
                            style = TextStyle(
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF32CD32), Color(0xFF1E90FF)) // Gradient colors
                                )))

                        in 100.0 .. 133.0 -> Text(text="B rank",
                            style = TextStyle(
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFFFD700), Color(0xFF32CD32)) // Gradient colors
                                )))

                        in 133.0 .. 166.0 -> Text(text="A rank",
                            style = TextStyle(
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFFFA500), Color(0xFFFFD700)) // Gradient colors
                                )))

                        in 166.0 .. 200.0 -> Text(text="S rank",
                            style = TextStyle(
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFFFD700), Color(0xFFFF4500)) // Gradient colors
                                )))


                        else -> Text("Erro: + $magneticField")
                    }

                    Spacer(Modifier.size(128.dp))

                    // Invite friends
                    var friendsList by remember { mutableStateOf<List<String>>(emptyList()) }
                    // Reference to the user's friends in Firebase
                    val userFriendsRef = db.reference.child("UserFriendsList").child(name)

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

                    Text(
                        text = "Invite Friends",
                        style = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)), // Semi-transparent card background
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.600f)
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
                                        usersRef.child(friend).get()
                                            .addOnSuccessListener { snapshot ->
                                                if (snapshot.exists()) {
                                                    powerLevel =
                                                        snapshot.child("PowerLevel")
                                                            .getValue<String>()
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

                                        Button(
                                            onClick = {
                                                val portalInvite =
                                                    db.reference.child("UserPortalInvite")
                                                        .child(friend)
                                                portalInvite.get()
                                                    .addOnSuccessListener { userSnapshot ->
                                                        val userInvited =
                                                            userSnapshot.getValue(object :
                                                                GenericTypeIndicator<List<String>>() {})
                                                                ?: emptyList()
                                                        portalInvite.setValue(userInvited + name)
                                                    }

                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Cyan.copy(
                                                    alpha = 0.15f
                                                )
                                            )
                                        ) {
                                            Text("Send invite", fontSize = 16.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.size(24.dp))
                    // Enter Portal Button
                    Button(
                        onClick = { navController.navigate("dailies_screen/?$id&username=$name2") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f))

                    ) {
                        Text("Enter Portal", fontSize = 18.sp, color = Color.White)
                    }

                    Spacer(Modifier.size(16.dp))

                    Button(
                        onClick = {

                                val portalRef = db.reference.child("Portals").child(id.toString())

                                portalRef.removeValue()
                                    .addOnSuccessListener {
                                        Log.d("ClosePortal", "Portal $id successfully removed.")
                                        // Navegar de volta para a tela do mapa
                                        navController.navigate("map_screen/?$id&username=$name")
                                    }
                                    .addOnFailureListener { error ->
                                        Log.e("ClosePortal", "Failed to close portal: ${error.message}")
                                    }


                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
                    ) {
                        Text("Close Portal", fontSize = 18.sp, color = Color.White)
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
                    Button(onClick = { navController.navigate("map_screen/?$id&username=$name") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan.copy(alpha = 0.15f))) {
                        Text("Map")
                    }

//                    Button(onClick = { navController.navigate("dailies_screen/?$id&username=$name") }) {
//                        Text("Dailies")
//                    }

                    Button(onClick = { navController.navigate("guild_screen/?$id&username=$name") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan.copy(alpha = 0.15f))) {
                        Text("Friends")
                    }
                }
            }
        }
    )
}

@Composable
fun RankText(rank: String, startColor: Color, endColor: Color) {
    Text(
        text = rank,
        style = TextStyle(
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            brush = Brush.linearGradient(
                colors = listOf(startColor, endColor)
            )
        )
    )
}
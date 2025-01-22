package com.example.sololeveling.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sololeveling.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlin.math.sqrt

import okhttp3.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.IOException
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

// Data classes
data class MonsterListResponse(
    @SerializedName("results") val results: List<Monster>
)

data class Monster(
    @SerializedName("index") val index: String,
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String
)

data class MonsterDetails(
    @SerializedName("name") val name: String,
    @SerializedName("size") val size: String,
    @SerializedName("type") val type: String,
    @SerializedName("hit_points") val hitPoints: Int,
    @SerializedName("armor_class") val armorClass: List<Map<String, Any>>?,
    @SerializedName("challenge_rating") val challengeRating: Double
)

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Dailies(
    navController: NavController,
    id: Int,
    context: Context, // Pass context to access SensorManager
    db: FirebaseDatabase,
    userName: String
) {
    val name = userName

    var magneticField by remember { mutableDoubleStateOf(0.0) }
    var ambientTemperature by remember { mutableFloatStateOf(0.0F) }
    var pressure by remember { mutableDoubleStateOf(0.0) }
    var lightLevel by remember { mutableFloatStateOf(0.0F) }
    var humidity by remember { mutableFloatStateOf(0.0F) }

    var monsterDetailsList by remember { mutableStateOf<List<MonsterDetails>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Reference to the help data in Firebase
    val userHelpRef = db.reference.child("UserHelp")

    // Reference to the position in Firebase
    val userPositionRef = db.reference.child("UserPosition")

    // Fetch 5 random monsters
    LaunchedEffect(Unit) {
        fetchRandomMonsters { fetchedDetails ->
            monsterDetailsList = fetchedDetails
            isLoading = false
        }
    }

    // Image as background
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Scaffold(
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
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title Section
                        Text(
                            text = "Environment Metrics",
                            style = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Environmental Data Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
//                            measureMagneticField(context) { magneticField = it }
//                            Text(
//                                text = "Magnetic Field: $magneticField µT",
//                                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
//                            )
                            measureAmbientTemperature(context) { ambientTemperature = it }
                            Text(
                                text = buildAnnotatedString {
                                    append("Ambient Temperature: ")
                                    withStyle(
                                        style = SpanStyle(
                                            color = if (dangerTemp(ambientTemperature)) Color.Red else Color.Green
                                        )
                                    ){
                                        append("$ambientTemperature °C")
                                    }
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                            )
                            measurePressure(context) { pressure = it }
                            Text(
                                text =  buildAnnotatedString{
                                    append("Pressure: ")
                                    withStyle(
                                        style = SpanStyle(
                                            color = if (dangerPress(pressure)) Color.Red else Color.Green
                                        )
                                    ){
                                            append("$pressure hPa")
                                        }
                                } ,
                                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                            )
                            measureHumidity(context) { humidity = it }
                            Text(
                                text = buildAnnotatedString{
                                    append("Humidity: ")
                                    withStyle(
                                        style = SpanStyle(
                                            color = if (dangerHumidity(humidity)) Color.Red else Color.Green
                                        )
                                    ){
                                        append("$humidity %")
                                    }
                                } ,
                                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                            )
                            measureLight(context) { lightLevel = it }
                            Text(
                                text = buildAnnotatedString{
                                    append("Light Level: ")
                                    withStyle(
                                        style = SpanStyle(
                                            color = if (dangerousLum(lightLevel)) Color.Red else Color.Green
                                        )
                                    ){
                                        append("$lightLevel lx")
                                    }
                                } ,
                                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                            )
                        }

                        // Send Help Request Button Section
                        Button(
                            onClick = {
                                checkAndRequestHelpFromFirebase(userPositionRef, userHelpRef, userName)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)) // Vibrant color for the button
                        ) {
                            Text("Send Help Request", color = Color.White)
                        }

                        // Loading Indicator (if applicable)
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        // Monster List Section
                        Text(
                            text = "Most likely Monsters to appear",
                            style = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.80f)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(monsterDetailsList) { monster ->
                                    MonsterCard(monster)
                                }
                            }
                        }
                    }
                }
            }
        )

        // Bottom Navigation Buttons
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.7f)) // Semi-transparent background for the navigation bar
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan.copy(alpha = 0.15f))) {
                    Text("Friends", fontSize = 16.sp)
                }
            }
        }
    }

}

@Composable
fun MonsterCard(monster: MonsterDetails) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .heightIn(min = 140.dp), // Ensure a minimum height for each card
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)) // Semi-transparent card background
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = monster.name,
                style = MaterialTheme.typography.titleLarge.copy(color = Color.White)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Size: ${monster.size}",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )
            Text(
                text = "Type: ${monster.type}",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )
            Text(
                text = "Hit Points: ${monster.hitPoints}",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )
            Text(
                text = "Challenge Rating: ${monster.challengeRating}",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )
        }
    }
}

// Measure Magnetic Field
fun measureMagneticField(context: Context, onResult: (Double) -> Unit) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    if (magneticSensor == null) {
        Toast.makeText(context, "Magnetic field sensor not available", Toast.LENGTH_SHORT).show()
        onResult(0.0)
        return
    }

    val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return

            val magneticFieldX = event.values[0]
            val magneticFieldY = event.values[1]
            val magneticFieldZ = event.values[2]

            val totalMagneticField = sqrt(
                (magneticFieldX * magneticFieldX +
                        magneticFieldY * magneticFieldY +
                        magneticFieldZ * magneticFieldZ).toDouble()
            )

            onResult(totalMagneticField)
            sensorManager.unregisterListener(this)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    sensorManager.registerListener(sensorEventListener, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL)
}

// Measure Ambient Temperature
fun measureAmbientTemperature(context: Context, onResult: (Float) -> Unit) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)

    if (temperatureSensor == null) {
        Toast.makeText(context, "Ambient temperature sensor not available", Toast.LENGTH_SHORT).show()
        onResult(0.0F)
        return
    }

    val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return

            val ambientTemp = event.values[0] // Temperature in degrees Celsius
            onResult(ambientTemp)
            sensorManager.unregisterListener(this)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    sensorManager.registerListener(sensorEventListener, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL)
}

// Measure Pressure
fun measurePressure(context: Context, onResult: (Double) -> Unit) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    if (pressureSensor == null) {
        Toast.makeText(context, "Pressure sensor not available", Toast.LENGTH_SHORT).show()
        onResult(0.0)
        return
    }

    val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return

            val pressureValue = event.values[0] // Pressure in hPa (hectopascal)
            onResult(pressureValue.toDouble())
            sensorManager.unregisterListener(this)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    sensorManager.registerListener(sensorEventListener, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL)
}

// Measure Humidity
fun measureHumidity(context: Context, onResult: (Float) -> Unit) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)

    if (humiditySensor == null) {
        Toast.makeText(context, "Humidity sensor not available", Toast.LENGTH_SHORT).show()
        onResult(0.0F)
        return
    }

    val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return

            val humidity = event.values[0]
            onResult(humidity)
            sensorManager.unregisterListener(this)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    sensorManager.registerListener(sensorEventListener, humiditySensor, SensorManager.SENSOR_DELAY_NORMAL)
}

// Measure Light Level
fun measureLight(context: Context, onResult: (Float) -> Unit) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    if (lightSensor == null) {
        Toast.makeText(context, "Light sensor not available", Toast.LENGTH_SHORT).show()
        onResult(0.0F)
        return
    }

    val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return

            val lightLevel = event.values[0]
            onResult(lightLevel)
            sensorManager.unregisterListener(this)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    sensorManager.registerListener(sensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
}

// Function to fetch 5 random monsters and their details
fun fetchRandomMonsters(onResult: (List<MonsterDetails>) -> Unit) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://www.dnd5eapi.co/api/monsters")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            println("Error fetching monsters: ${e.message}")
            onResult(emptyList())
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { json ->
                val gson = Gson()
                val monsterResponse = gson.fromJson(json, MonsterListResponse::class.java)
                val shuffledMonsters = monsterResponse.results.shuffled().take(5)

                val detailsList = mutableListOf<MonsterDetails>()
                val detailFetchers = shuffledMonsters.map { monster ->
                    fetchMonsterDetails(monster.index) { details ->
                        details?.let { detailsList.add(it) }
                        if (detailsList.size == 5) {
                            onResult(detailsList)
                        }
                    }
                }
            }
        }
    })
}

// Function to fetch individual monster details
fun fetchMonsterDetails(monsterIndex: String, onResult: (MonsterDetails?) -> Unit) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://www.dnd5eapi.co/api/monsters/$monsterIndex")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            println("Error fetching monster details: ${e.message}")
            onResult(null)
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { json ->
                val gson = Gson()
                val details = gson.fromJson(json, MonsterDetails::class.java)
                onResult(details)
            }
        }
    })
}

/**
 * Checks all user positions from Firebase to find nearby users and sends help requests to them.
 */
@RequiresApi(Build.VERSION_CODES.O)
private fun checkAndRequestHelpFromFirebase(
    userPositionRef: DatabaseReference,
    userHelpRef: DatabaseReference,
    userName: String,
    radius: Double = 500.0 // Define proximity radius in meters
) {
    userPositionRef.get().addOnSuccessListener { snapshot ->
        // Fetch the current user's position
        val currentUserSnapshot = snapshot.child(userName)
        val currentLatitude = currentUserSnapshot.child("latitude").getValue(Double::class.java)
        val currentLongitude = currentUserSnapshot.child("longitude").getValue(Double::class.java)

        if (currentLatitude == null || currentLongitude == null) {
            Log.e("FirebaseError", "Current user position not found")
            return@addOnSuccessListener
        }

        // Iterate through other users to check proximity
        snapshot.children.forEach { userSnapshot ->
            val otherUserName = userSnapshot.key ?: return@forEach
            if (otherUserName == userName) return@forEach // Skip current user

            val otherLatitude = userSnapshot.child("latitude").getValue(Double::class.java)
            val otherLongitude = userSnapshot.child("longitude").getValue(Double::class.java)

            if (otherLatitude != null && otherLongitude != null) {
                val distance = calculateDistance(
                    currentLatitude,
                    currentLongitude,
                    otherLatitude,
                    otherLongitude
                )

                if (distance <= radius) {
                    // Send help request to this user
                    val timestamp = System.currentTimeMillis()
                    userHelpRef.child(otherUserName).child(userName).setValue(formatTimestamp(timestamp))
                }
            }
        }
    }.addOnFailureListener {
        Log.e("FirebaseError", "Failed to fetch user positions: ${it.message}")
    }
}

/**
 * Calculates the distance between two geographical points using the Haversine formula.
 */
private fun calculateDistance(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val earthRadius = 6371000.0 // Radius of Earth in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c
}

fun formatTimestamp(timestamp: Long): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val instant = Instant.ofEpochMilli(timestamp)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } else {
        // For older Android versions, use SimpleDateFormat
        val date = Date(timestamp)
        val simpleDateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        simpleDateFormat.format(date)
    }
}

fun dangerTemp(temp: Float): Boolean{
    return temp>50.0 || temp<0.0
}
fun dangerPress(press: Double): Boolean{
    return press>2000.0 || press<700.0
}

fun dangerHumidity(hum: Float): Boolean{
    return hum>80.0 || hum<20.0
}

fun dangerousLum(lum: Float): Boolean{
    return lum>10000.0 || lum<25.5
}
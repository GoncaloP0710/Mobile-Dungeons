package com.example.sololeveling.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.math.sqrt

import okhttp3.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.IOException
import kotlin.random.Random

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

@Composable
fun Dailies(
    navController: NavController,
    id: Int,
    context: Context // Pass context to access SensorManager
) {
    var magneticField by remember { mutableDoubleStateOf(0.0) }
    var ambientTemperature by remember { mutableFloatStateOf(0.0F) }
    var pressure by remember { mutableDoubleStateOf(0.0) }
    var lightLevel by remember { mutableFloatStateOf(0.0F) }
    var humidity by remember { mutableFloatStateOf(0.0F) }

    var monsterDetailsList by remember { mutableStateOf<List<MonsterDetails>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch 5 random monsters
    LaunchedEffect(Unit) {
        fetchRandomMonsters { fetchedDetails ->
            monsterDetailsList = fetchedDetails
            isLoading = false
        }
    }

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(monsterDetailsList) { monster ->
                        MonsterCard(monster)
                    }
                }
            }

            // Display Magnetic Field
            Text(
                text = "Magnetic Field: $magneticField µT",
            )
            Button(onClick = { measureMagneticField(context) { magneticField = it } }) {
                Text("Scan Portal")
            }

            // Display Ambient Temperature
            Text(
                text = "Ambient Temperature: $ambientTemperature °C",
            )
            Button(onClick = { measureAmbientTemperature(context) { ambientTemperature = it } }) {
                Text("Measure Temperature")
            }

            // Display Pressure
            Text(
                text = "Pressure: $pressure hPa",
            )
            Button(onClick = { measurePressure(context) { pressure = it } }) {
                Text("Measure Pressure")
            }

            // Humidity
            Text(text = "Humidity: $humidity %")
            Button(onClick = { measureHumidity(context) { humidity = it } }) {
                Text("Measure Humidity")
            }

            // Light Level
            Text(text = "Light Level: $lightLevel lx")
            Button(onClick = { measureLight(context) { lightLevel = it } }) {
                Text("Measure Light")
            }

            // Home Screen button
            Button(onClick = { navController.navigate("home_screen/$id") }) {
                Text("Home")
            }
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

@Composable
fun MonsterCard(monster: MonsterDetails) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Name: ${monster.name}", style = MaterialTheme.typography.titleMedium)
            Text("Size: ${monster.size}")
            Text("Type: ${monster.type}")
            Text("Hit Points: ${monster.hitPoints}")
            Text("Challenge Rating: ${monster.challengeRating}")
        }
    }
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
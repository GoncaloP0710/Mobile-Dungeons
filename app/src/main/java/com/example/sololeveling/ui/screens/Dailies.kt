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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.awaitAll
import kotlin.math.sqrt

// State to hold the total magnetic field value

@Composable
fun Dailies(
    navController: NavController,
    id: Int,
    context: Context // Pass context to access SensorManager
) {
    var magneticField by remember { mutableDoubleStateOf(0.0) }

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Magnetic Field: $magneticField µT",
            )

            // Magnetic sensor measure button
            Button(onClick = { measureMagneticField(context) { magneticField = it } }) {
                Text("Scan Portal")
            }

            // Home Screen button
            Button(onClick = { navController.navigate("home_screen/$id") }) {
                Text("Home")
            }
        }
    }
}

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

            val magneticFieldX = event.values[0] // µT along X-axis
            val magneticFieldY = event.values[1] // µT along Y-axis
            val magneticFieldZ = event.values[2] // µT along Z-axis

            val totalMagneticField = sqrt(
                (magneticFieldX * magneticFieldX +
                        magneticFieldY * magneticFieldY +
                        magneticFieldZ * magneticFieldZ).toDouble()
            )

            onResult(totalMagneticField)

            // Stop listening after one measurement
            sensorManager.unregisterListener(this)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    sensorManager.registerListener(
        sensorEventListener,
        magneticSensor,
        SensorManager.SENSOR_DELAY_NORMAL
    )
}

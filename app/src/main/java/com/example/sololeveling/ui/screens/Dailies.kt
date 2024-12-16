package com.example.sololeveling.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import kotlin.math.sqrt

@Composable
fun Dailies(
    navController: NavController,
    id: Int,
    context: Context // Pass context to access SensorManager
) {
    // Magnetic sensor measure button
    Button(onClick = { magneticSensorMeasure(context) }) {
        Text("Scan Portal")
    }

    // Home Screen button
    Button(onClick = { navController.navigate("home_screen/$id") }) {
        Text("Home")
    }
}

fun magneticSensorMeasure(context: Context) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    if (magneticSensor == null) {
        Toast.makeText(context, "Magnetic field sensor not available", Toast.LENGTH_SHORT).show()
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

            Toast.makeText(
                context,
                "Magnetic Field: X=$magneticFieldX µT, Y=$magneticFieldY µT, Z=$magneticFieldZ µT, Total=$totalMagneticField µT",
                Toast.LENGTH_LONG
            ).show()

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

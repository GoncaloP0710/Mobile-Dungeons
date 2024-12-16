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
import kotlin.math.sqrt

@Composable
fun Dailies(
    navController: NavController,
    id: Int,
    context: Context // Pass context to access SensorManager
) {
    var magneticField by remember { mutableDoubleStateOf(0.0) }
    var ambientTemperature by remember { mutableDoubleStateOf(0.0) }
    var pressure by remember { mutableDoubleStateOf(0.0) }

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
fun measureAmbientTemperature(context: Context, onResult: (Double) -> Unit) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)

    if (temperatureSensor == null) {
        Toast.makeText(context, "Ambient temperature sensor not available", Toast.LENGTH_SHORT).show()
        onResult(0.0)
        return
    }

    val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return

            val ambientTemp = event.values[0] // Temperature in degrees Celsius
            onResult(ambientTemp.toDouble())
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

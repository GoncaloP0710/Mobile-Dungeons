package com.example.sololeveling.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun Map(
    navController: NavController,
    id: Int,
    context: Context
) {
    // Configure OSMDroid
    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

    // Box layout to hold the map and the button
    Box(modifier = Modifier.fillMaxSize()) {
        // Display the MapView
        AndroidView(
            factory = {
                // Initialize MapView
                val mapView = MapView(context).apply {
                    setMultiTouchControls(true)
                    controller.apply {
                        setZoom(15.0)
                        setCenter(GeoPoint(48.8588443, 2.2943506)) // Example: Eiffel Tower
                    }
                }

                // Add a marker
                val marker = Marker(mapView).apply {
                    position = GeoPoint(48.8588443, 2.2943506)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Eiffel Tower"
                }
                mapView.overlays.add(marker)

                mapView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Back button to navigate home
        Button(onClick = { navController.navigate("home_screen/$id") }) {
            Text("Home")
        }
    }
}

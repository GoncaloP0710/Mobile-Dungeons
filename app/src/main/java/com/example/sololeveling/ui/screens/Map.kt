package com.example.sololeveling.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private const val LOCATION_REFRESH_TIME = 15000L // 15 segundos para atualizar
private const val LOCATION_REFRESH_DISTANCE = 500f // 500 metros para atualizar

@Composable
fun Map(
    navController: NavController,
    id: Int,
    context: Context,
    db: FirebaseDatabase,
    userName: String
) {

    // Reference to the user's friends in Firebase
    val userPositionRef = db.reference.child("UserPosition").child(userName)

    // Configure OSMDroid
    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

    // Estado para armazenar localização atual
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    // Inicializar o LocationManager
    val locationManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    // Listener para atualizações de localização
    val locationListener = remember {
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                currentLocation = GeoPoint(location.latitude, location.longitude)
                // Write the location to Firebase
                val locationMap = mapOf(
                    "latitude" to currentLocation!!.latitude,
                    "longitude" to currentLocation!!.longitude
                )
                userPositionRef.setValue(locationMap)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Location updated to Firebase", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to update location", Toast.LENGTH_SHORT).show()
                    }
            }

            override fun onProviderEnabled(provider: String) {
                Toast.makeText(context, "$provider ativado", Toast.LENGTH_SHORT).show()
            }

            override fun onProviderDisabled(provider: String) {
                Toast.makeText(context, "$provider desativado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Solicitar permissões
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (hasLocationPermission) {
                startLocationUpdates(locationManager, locationListener, context)
            } else {
                Toast.makeText(context, "Permissões de localização necessárias", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Verificar permissões e solicitar, se necessário
    LaunchedEffect(Unit) {
        val fineLocationGranted = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        hasLocationPermission = fineLocationGranted || coarseLocationGranted

        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            startLocationUpdates(locationManager, locationListener, context)
        }
    }

    // Box layout para segurar o mapa e os botões
    Box(modifier = Modifier.fillMaxSize()) {
        // MapView
        AndroidView(
            factory = {
                MapView(context).apply {
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { mapView ->
                currentLocation?.let { location ->
                    mapView.controller.setCenter(location)

                    // Adicionar marcador para localização atual
                    val marker = Marker(mapView).apply {
                        position = location
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Minha localização"
                    }
                    mapView.overlays.clear()
                    mapView.overlays.add(marker)
                }
            }
        )

        // Botão para mostrar localização atual
        Button(
            onClick = {
                if (currentLocation != null) {
                    Toast.makeText(
                        context,
                        "Localização atual: ${currentLocation?.latitude}, ${currentLocation?.longitude}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(context, "Localização não disponível", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("Minha Localização")
        }

        // Botão para voltar à tela inicial
        Button(
            onClick = { navController.navigate("home_screen/$id") },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Home")
        }
    }
}

private fun startLocationUpdates(
    locationManager: LocationManager,
    locationListener: LocationListener,
    context: Context
) {
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            LOCATION_REFRESH_TIME,
            LOCATION_REFRESH_DISTANCE,
            locationListener
        )
    }
}

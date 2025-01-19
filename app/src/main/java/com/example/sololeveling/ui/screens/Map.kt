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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.example.sololeveling.R
import com.google.firebase.database.DatabaseReference
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
    var user = userName
    // References to the user's data and portals in Firebase
    val userPositionRef = db.reference.child("UserPosition").child(user)
    val portalPositionRef = db.reference.child("Portals")

    // Configure OSMDroid
    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

    // Estado para armazenar localização atual e marcadores
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var portalMarkers by remember { mutableStateOf<List<Pair<String, GeoPoint>>>(emptyList()) }
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
                saveCurrentLocation(userPositionRef, currentLocation)
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
            // Se as permissões estiverem concedidas, tentamos obter a última localização conhecida
            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation != null) {
                currentLocation = GeoPoint(lastKnownLocation.latitude, lastKnownLocation.longitude)
                saveCurrentLocation(userPositionRef, currentLocation)
            }
            startLocationUpdates(locationManager, locationListener, context)

        }

        // Recuperar marcadores de portais existentes do Firebase
        portalPositionRef.get().addOnSuccessListener { snapshot ->
            val portals = snapshot.children.mapNotNull { child ->
                val uid = child.key // Obtém o UID
                val latitude = child.child("latitude").getValue(Double::class.java)
                val longitude = child.child("longitude").getValue(Double::class.java)
                if (uid != null && latitude != null && longitude != null) {
                    uid to GeoPoint(latitude, longitude) // Cria um par (UID, GeoPoint)
                } else null
            }
            portalMarkers = portals
        }.addOnFailureListener {
            Toast.makeText(context, "Erro ao carregar portais do Firebase", Toast.LENGTH_SHORT).show()
        }
    }

    // Box layout para segurar o mapa e os botões
    Box(modifier = Modifier.fillMaxSize()) {
        // MapView
        AndroidView(
            factory = {
                MapView(context).apply {
                    setMultiTouchControls(true) // Permite gestos multitouch
                    setBuiltInZoomControls(false) // Desativa os botões de zoom embutidos
                    controller.setZoom(10.0)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { mapView ->
                currentLocation?.let { location ->
                    mapView.controller.setCenter(location)
                }

                // Adicionar marcadores de portais
                mapView.overlays.clear()
                portalMarkers.forEach { (uid, position) ->
                    val marker = Marker(mapView).apply {
                        this.position = position
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Portal $uid" // Mostra o UID no título
                        setOnMarkerClickListener { _, _ ->
                            navController.navigate("portal_screen/?$uid&username=$userName")
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                }
            }
        )

        Column(
            modifier = Modifier
                .padding(16.dp) // Adjust padding as needed
                .fillMaxWidth(),
            horizontalAlignment = Alignment.End // Align content to the start (left)

        ) {
            Box(modifier = Modifier.clickable { navController.navigate("storage_screen/$id?username=$userName") }) {
                Image(
                    painter = painterResource(id = R.drawable.usericon),
                    contentDescription = "Icon Image",
                    modifier = Modifier
                        .size(160.dp) // Adjust size as needed
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.5f)) // Semi-transparent background
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { navController.navigate("guild_screen/?$id&username=$userName") }) {
                    Text("Friends")
                }

                // Botão para salvar posição do portal no Firebase
                Button(
                    onClick = {
                        currentLocation?.let { location ->
                            val newPortalRef = portalPositionRef.push() // Gera uma nova referência com UID único
                            val portalUID = newPortalRef.key // Obtém o UID único gerado
                            val portalMap = mapOf(
                                "uid" to portalUID, // Adiciona o UID ao mapa
                                "latitude" to location.latitude,
                                "longitude" to location.longitude
                            )
                            newPortalRef.setValue(portalMap)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Portal salvo no Firebase", Toast.LENGTH_SHORT).show()
                                    // Atualiza portalMarkers com o novo par (UID, GeoPoint)
                                    portalMarkers = portalMarkers + (portalUID!! to GeoPoint(location.latitude, location.longitude))
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Falha ao salvar portal", Toast.LENGTH_SHORT).show()
                                }
                        } ?: Toast.makeText(context, "Localização não disponível", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Scan Portal")
                }


                Button(onClick = { navController.navigate("map_screen/?$id&username=$userName") }) {
                    Text("Map")
                }
            }
        }
    }
}

// Função para salvar a localização atual no Firebase
private fun saveCurrentLocation(
    userPositionRef: DatabaseReference,
    currentLocation: GeoPoint?
) {
    currentLocation?.let { location ->
        // Criar um mapa para atualizar apenas a localização
        val locationMap = mapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude
        )

        // Usar updateChildren para atualizar apenas a localização
        userPositionRef.updateChildren(locationMap)
            .addOnSuccessListener {
                println("Localização atualizada no Firebase.")
            }
            .addOnFailureListener {
                println("Falha ao atualizar localização no Firebase: ${it.message}")
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

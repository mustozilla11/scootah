package com.scootah.app.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scootah.app.ui.theme.DangerRed
import com.scootah.app.ui.theme.NeutralGray
import com.scootah.app.ui.theme.SafeGreen
import com.scootah.app.ui.theme.ScootahGreen
import com.scootah.app.ui.theme.StreakOrange
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // ── Permission state ─────────────────────────────────────────────────────
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasPermission = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Ask on first entry if not yet granted
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Start / stop location updates depending on permission
    LaunchedEffect(hasPermission) {
        if (hasPermission) viewModel.startLocationUpdates()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopLocationUpdates() }
    }

    if (hasPermission) {
        MapContent(modifier = modifier, viewModel = viewModel, uiState = uiState)
    } else {
        PermissionDeniedScreen(
            modifier = modifier,
            onRequestPermission = {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        )
    }
}

// ── Map content (shown when permission granted) ───────────────────────────────

@Composable
private fun MapContent(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel,
    uiState: RideUiState
) {
    val context = LocalContext.current

    // Istanbul as default center (replaced by real location once fix arrives)
    val defaultCenter = remember { GeoPoint(41.0082, 28.9784) }

    // MapView — created once
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            isTilesScaledToDpi = true
            controller.setZoom(17.5)
            controller.setCenter(defaultCenter)
        }
    }

    // Location overlay (blue arrow that follows user)
    val locationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
        }
    }

    // Add overlays once on first composition
    DisposableEffect(Unit) {
        mapView.overlays.add(locationOverlay)
        mapView.onResume()
        onDispose {
            locationOverlay.disableMyLocation()
            mapView.onPause()
        }
    }

    // When first real GPS fix arrives — centre map and draw mock safety routes
    var routesDrawn by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.currentLocation) {
        val loc = uiState.currentLocation ?: return@LaunchedEffect
        if (routesDrawn) return@LaunchedEffect

        val center = GeoPoint(loc.latitude, loc.longitude)
        // Pan to real location
        mapView.controller.animateTo(center)
        // Draw coloured routes relative to actual position
        drawSafetyRoutes(mapView, center)
        drawWarningMarker(mapView, center)
        routesDrawn = true
        mapView.invalidate()
    }

    // Follow-location mode while riding
    LaunchedEffect(uiState.isRiding) {
        if (uiState.isRiding) locationOverlay.enableFollowLocation()
        else locationOverlay.disableFollowLocation()
    }

    // ── Animation (pulse ring when riding) ───────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulseAlpha"
    )

    // ── Layout ───────────────────────────────────────────────────────────────
    Box(modifier = modifier.fillMaxSize()) {

        // OSMDroid map (full screen, base layer)
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        // "Riding" status badge — top center
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
        ) {
            Text(
                text = if (uiState.isRiding) "🟢  Sürüş Aktif" else "⚫  Sürüş Bekleniyor",
                style = MaterialTheme.typography.labelLarge.copy(color = Color.White),
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Scooter icon overlay at center (decorative — real dot comes from OSMDroid overlay)
        if (uiState.currentLocation != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Pulse ring
                if (uiState.isRiding) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(ScootahGreen.copy(alpha = pulseAlpha * 0.35f), CircleShape)
                    )
                }
            }
        }

        // Road safety legend — top start
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 10.dp, top = 56.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LegendItem(color = SafeGreen, text = "Güvenli yol")
            LegendItem(color = DangerRed, text = "Riskli yol")
            LegendItem(color = NeutralGray, text = "Az tercih edilen")
        }

        // Bottom panel — stats + FAB
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LiveStatsPanel(uiState = uiState)

            FloatingActionButton(
                onClick = viewModel::toggleRide,
                containerColor = if (uiState.isRiding) DangerRed else ScootahGreen,
                contentColor = Color.White,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    imageVector = if (uiState.isRiding) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (uiState.isRiding) "Durdur" else "Başlat",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

// ── OSMDroid overlay helpers ──────────────────────────────────────────────────

/**
 * Draws coloured Polylines around [center] to simulate road safety layer.
 * Green = safe / recommended, Red = risky, Gray = rarely used.
 * All coordinates are small offsets (~200–800m) from the real user location.
 */
@Suppress("DEPRECATION")
private fun drawSafetyRoutes(mapView: MapView, center: GeoPoint) {
    val lat = center.latitude
    val lon = center.longitude

    fun polyline(colorHex: String, width: Float, vararg points: GeoPoint): Polyline =
        Polyline(mapView).apply {
            outlinePaint.color = android.graphics.Color.parseColor(colorHex)
            outlinePaint.strokeWidth = width
            outlinePaint.strokeCap = Paint.Cap.ROUND
            outlinePaint.strokeJoin = Paint.Join.ROUND
            setPoints(points.toList())
        }

    // ── Safe / green routes ───────────────────────────────────────────────
    mapView.overlays.add(0, polyline("#00E676", 18f,
        GeoPoint(lat - 0.007, lon - 0.005),
        GeoPoint(lat - 0.004, lon - 0.002),
        GeoPoint(lat - 0.001, lon + 0.001),
        GeoPoint(lat + 0.002, lon + 0.004),
        GeoPoint(lat + 0.005, lon + 0.006)
    ))
    mapView.overlays.add(0, polyline("#00E676", 14f,
        GeoPoint(lat - 0.001, lon + 0.001),
        GeoPoint(lat + 0.001, lon + 0.005),
        GeoPoint(lat + 0.003, lon + 0.009)
    ))
    mapView.overlays.add(0, polyline("#00E676", 12f,
        GeoPoint(lat + 0.005, lon - 0.003),
        GeoPoint(lat + 0.003, lon - 0.001),
        GeoPoint(lat + 0.002, lon + 0.004)
    ))

    // ── Risky / red routes ────────────────────────────────────────────────
    mapView.overlays.add(0, polyline("#FF3D00", 16f,
        GeoPoint(lat - 0.006, lon + 0.004),
        GeoPoint(lat - 0.003, lon + 0.002),
        GeoPoint(lat, lon - 0.002),
        GeoPoint(lat + 0.001, lon - 0.005)
    ))
    mapView.overlays.add(0, polyline("#FF3D00", 12f,
        GeoPoint(lat - 0.004, lon - 0.006),
        GeoPoint(lat - 0.002, lon - 0.004)
    ))

    // ── Neutral / gray routes ─────────────────────────────────────────────
    mapView.overlays.add(0, polyline("#78909C", 12f,
        GeoPoint(lat + 0.003, lon - 0.007),
        GeoPoint(lat + 0.001, lon - 0.004),
        GeoPoint(lat - 0.001, lon - 0.001)
    ))
    mapView.overlays.add(0, polyline("#78909C", 10f,
        GeoPoint(lat - 0.005, lon + 0.007),
        GeoPoint(lat - 0.003, lon + 0.004)
    ))
}

/**
 * Adds a tappable ⚠️ marker slightly off center to simulate a road works alert.
 */
private fun drawWarningMarker(mapView: MapView, center: GeoPoint) {
    val markerPos = GeoPoint(center.latitude - 0.002, center.longitude + 0.0015)
    Marker(mapView).apply {
        position = markerPos
        title = "⚠️ Yol Çalışması!"
        snippet = "Bu güzergahta bakım çalışması devam etmektedir.\nAlternatif güzergahı tercih edin."
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(this)
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun LiveStatsPanel(uiState: RideUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.82f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LiveStat(
                value = "%.0f".format(uiState.speedKmh),
                unit = "km/s",
                label = "Anlık Hız",
                color = when {
                    uiState.speedKmh > 25f -> DangerRed
                    uiState.speedKmh > 15f -> StreakOrange
                    else -> SafeGreen
                }
            )
            LiveStat(
                value = "%.2f".format(uiState.distanceKm),
                unit = "km",
                label = "Mesafe",
                color = ScootahGreen
            )
            LiveStat(
                value = uiState.formattedTime,
                unit = "",
                label = "Süre",
                color = Color.White
            )
        }
    }
}

@Composable
private fun LiveStat(value: String, unit: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    color = color
                )
            )
            if (unit.isNotBlank()) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall.copy(color = color.copy(alpha = 0.7f)),
                    modifier = Modifier.padding(bottom = 5.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.55f))
        )
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 22.dp, height = 5.dp)
                .background(color = color, shape = RoundedCornerShape(3.dp))
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 10.sp
            )
        )
    }
}

// ── Permission denied screen ──────────────────────────────────────────────────

@Composable
private fun PermissionDeniedScreen(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOff,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Konum İzni Gerekli",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Haritada konumunu görmek, anlık hız ve mesafeyi takip etmek için konum iznine ihtiyacımız var.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                ),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Konum İznini Ver",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

package com.scootah.app.ui.map

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scootah.app.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RideUiState(
    val isRiding: Boolean = false,
    /** Real GPS speed in km/h (0 when not riding or no GPS fix yet) */
    val speedKmh: Float = 0f,
    /** Accumulated distance in km from GPS coordinates */
    val distanceKm: Float = 0f,
    /** Elapsed ride seconds (timer only runs when isRiding = true) */
    val elapsedSeconds: Int = 0,
    /** Latest GPS fix — null until first fix arrives */
    val currentLocation: Location? = null
) {
    val formattedTime: String
        get() {
            val m = elapsedSeconds / 60
            val s = elapsedSeconds % 60
            return "%02d:%02d".format(m, s)
        }
}

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository.getInstance(application)
    private val locationManager =
        application.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _uiState = MutableStateFlow(RideUiState())
    val uiState: StateFlow<RideUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var lastLocation: Location? = null
    private var accumulatedKmSinceLastSave = 0f

    // ── Location listener ────────────────────────────────────────────────────
    private val locationListener = LocationListener { location ->
        val speedKmh = if (location.hasSpeed()) (location.speed * 3.6f).coerceIn(0f, 80f) else 0f

        // Only accumulate distance while the ride is active
        val deltaKm = if (lastLocation != null && _uiState.value.isRiding) {
            (lastLocation!!.distanceTo(location) / 1000f).coerceAtLeast(0f)
        } else 0f

        lastLocation = location
        accumulatedKmSinceLastSave += deltaKm

        _uiState.update { state ->
            state.copy(
                speedKmh = if (state.isRiding) speedKmh else 0f,
                distanceKm = state.distanceKm + deltaKm,
                currentLocation = location
            )
        }

        // Persist every ~500m to avoid hammering DataStore
        if (accumulatedKmSinceLastSave >= 0.5f) {
            val toSave = accumulatedKmSinceLastSave
            accumulatedKmSinceLastSave = 0f
            viewModelScope.launch { repository.addKm(toSave) }
        }
    }

    // ── Public API ───────────────────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        // GPS — most accurate, slower first fix
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1_000L,   // min 1 second
                1f,       // min 1 metre
                locationListener
            )
        }
        // Network — faster first fix, less accurate
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                2_000L,
                5f,
                locationListener
            )
        }
        // Seed with last known location so map centres immediately
        @Suppress("DEPRECATION")
        val lastKnown = try {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (_: Exception) { null }

        lastKnown?.let { loc ->
            _uiState.update { it.copy(currentLocation = loc) }
            lastLocation = loc
        }
    }

    fun stopLocationUpdates() {
        locationManager.removeUpdates(locationListener)
    }

    fun toggleRide() {
        if (_uiState.value.isRiding) stopRide() else startRide()
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private fun startRide() {
        _uiState.update { it.copy(isRiding = true) }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    private fun stopRide() {
        timerJob?.cancel()
        timerJob = null
        // Save any remaining km
        if (accumulatedKmSinceLastSave > 0f) {
            val toSave = accumulatedKmSinceLastSave
            accumulatedKmSinceLastSave = 0f
            viewModelScope.launch { repository.addKm(toSave) }
        }
        _uiState.update { it.copy(isRiding = false, speedKmh = 0f) }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
        timerJob?.cancel()
    }
}

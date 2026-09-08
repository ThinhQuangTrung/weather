package com.example.weather.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager as AndroidLocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Quản lý việc truy xuất vị trí thiết bị thông qua FusedLocationProviderClient.
 * Tách biệt logic lấy toạ độ GPS khỏi ViewModel và UI.
 */
class LocationManager(
    private val context: Context
) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Kiểm tra xem ứng dụng đã được cấp quyền truy cập vị trí (Chính xác hoặc Tương đối) chưa.
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Kiểm tra xem dịch vụ GPS / Vị trí trên thiết bị đã được bật chưa.
     */
    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? AndroidLocationManager
        return locationManager?.let {
            it.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER) ||
                    it.isProviderEnabled(AndroidLocationManager.NETWORK_PROVIDER)
        } ?: false
    }

    /**
     * Lấy toạ độ vị trí hiện tại dạng suspend coroutine an toàn sử dụng suspendCancellableCoroutine.
     * Ưu tiên lấy vị trí mới nhất có độ chính xác cao, nếu không thành công sẽ fallback sang lastLocation.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (continuation.isActive) {
                if (location != null) {
                    continuation.resume(location)
                } else {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { lastLoc ->
                            if (continuation.isActive) continuation.resume(lastLoc)
                        }
                        .addOnFailureListener {
                            if (continuation.isActive) continuation.resume(null)
                        }
                }
            }
        }.addOnFailureListener {
            if (continuation.isActive) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { lastLoc ->
                        if (continuation.isActive) continuation.resume(lastLoc)
                    }
                    .addOnFailureListener {
                        if (continuation.isActive) continuation.resume(null)
                    }
            }
        }

        continuation.invokeOnCancellation {
            cancellationTokenSource.cancel()
        }
    }

    /**
     * Lấy toạ độ vị trí thông qua Callback truyền thống.
     */
    @SuppressLint("MissingPermission")
    fun getCurrentLocationCallback(
        onResult: (latitude: Double, longitude: Double) -> Unit,
        onError: (message: String) -> Unit = {}
    ) {
        if (!hasLocationPermission()) {
            onError("Chưa được cấp quyền vị trí")
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onResult(location.latitude, location.longitude)
                } else {
                    val cts = CancellationTokenSource()
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        cts.token
                    ).addOnSuccessListener { freshLocation ->
                        if (freshLocation != null) {
                            onResult(freshLocation.latitude, freshLocation.longitude)
                        } else {
                            onError("Không thể xác định vị trí hiện tại")
                        }
                    }.addOnFailureListener {
                        onError(it.localizedMessage ?: "Lỗi khi lấy vị trí")
                    }
                }
            }
            .addOnFailureListener {
                onError(it.localizedMessage ?: "Lỗi khi lấy vị trí")
            }
    }

    companion object {
        fun hasLocationPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
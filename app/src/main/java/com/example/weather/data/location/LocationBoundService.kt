package com.example.weather.data.location

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * LocationBoundService:
 * -Bound Service (ràng buộc theo vòng đời ).
 * - Tự động kết nối khi Fragment gọi bindService() trong onStart().
 * - Tự động giải phóng và huỷ hoàn toàn khi Fragment gọi unbindService() trong onStop().
 */
class LocationBoundService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var locationManager: LocationManager //Khai báo biến quản lý định vị, sẽ được khởi tạo sau khi Service được tạo

    inner class LocalBinder : Binder() {
        fun getService(): LocationBoundService = this@LocationBoundService
    }

    override fun onCreate() {
//         khởi tạo và chỉ chạy duy nhất 1 lần sau khi service đc tạo ra
        super.onCreate()
        locationManager = LocationManager(applicationContext) //kiểm tra quyền GPS kiểm tra GPS có bật không và lấy vị trí hiện tại
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    /**
     * Yêu cầu lấy toạ độ vị trí GPS hiện tại từ FusedLocationProviderClient
     */
    fun requestCurrentLocation(onResult: (Location?) -> Unit) {
        serviceScope.launch {
            if (!locationManager.hasLocationPermission() || !locationManager.isLocationEnabled()) {
                onResult(null)
                return@launch
            }
            val location = locationManager.getCurrentLocation()
            onResult(location)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

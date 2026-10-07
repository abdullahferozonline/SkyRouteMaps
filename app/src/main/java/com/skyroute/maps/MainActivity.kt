package com.skyroute.maps

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.maplibre.android.MapLibre
import com.maplibre.android.geometry.LatLng
import com.maplibre.android.maps.MapView
import com.maplibre.android.maps.MapLibreMap
import com.maplibre.android.maps.Style
import com.maplibre.android.location.LocationComponentActivationOptions
import com.maplibre.android.location.modes.CameraMode
import com.maplibre.android.location.modes.RenderMode

class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager

    private lateinit var gpsStatus: TextView
    private lateinit var locationText: TextView
    private lateinit var accuracyText: TextView
    private lateinit var speedText: TextView

    private var map: MapLibreMap? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapLibre.getInstance(this)

        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)

        gpsStatus = findViewById(R.id.gpsStatus)
        locationText = findViewById(R.id.locationText)
        accuracyText = findViewById(R.id.accuracyText)
        speedText = findViewById(R.id.speedText)

        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync { mapLibreMap ->

            map = mapLibreMap

            mapLibreMap.setStyle(
                Style.Builder()
                    .fromUri("file:///android_asset/style.json")
            ) {

                enableLocationComponent(mapLibreMap)

            }
        }

        locationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager

        requestLocationPermission()
    }

    private fun enableLocationComponent(
        mapLibreMap: MapLibreMap
    ) {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {

            val locationComponent =
                mapLibreMap.locationComponent

            val activationOptions =
                LocationComponentActivationOptions
                    .builder(this, mapLibreMap.style!!)
                    .useDefaultLocationEngine(true)
                    .build()

            locationComponent.activateLocationComponent(
                activationOptions
            )

            locationComponent.isLocationComponentEnabled = true

            locationComponent.cameraMode =
                CameraMode.TRACKING

            locationComponent.renderMode =
                RenderMode.COMPASS

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Map location component unavailable",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun requestLocationPermission() {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST
            )

        } else {

            startGPS()
        }
    }

    private fun startGPS() {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1f,
                this
            )

            gpsStatus.text = "● GPS SEARCHING..."
            gpsStatus.setTextColor(0xFFFFC107.toInt())

        } catch (e: Exception) {

            gpsStatus.text = "● GPS UNAVAILABLE"
            gpsStatus.setTextColor(0xFFFF5252.toInt())
        }
    }

    override fun onLocationChanged(location: Location) {

        val latitude = location.latitude
        val longitude = location.longitude

        val accuracy = location.accuracy

        val speedKmh =
            if (location.hasSpeed()) {
                location.speed * 3.6f
            } else {
                0f
            }

        gpsStatus.text = "● GPS LOCKED"
        gpsStatus.setTextColor(0xFF55E6A5.toInt())

        locationText.text =
            String.format(
                "📍 %.6f°, %.6f°",
                latitude,
                longitude
            )

        accuracyText.text =
            String.format(
                "Accuracy: %.1f m",
                accuracy
            )

        speedText.text =
            String.format(
                "Speed: %.1f km/h",
                speedKmh
            )

        map?.let { mapLibreMap ->

            mapLibreMap.animateCamera(
                com.maplibre.android.camera.CameraUpdateFactory
                    .newLatLngZoom(
                        LatLng(latitude, longitude),
                        15.0
                    )
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == LOCATION_PERMISSION_REQUEST) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {

                startGPS()

                map?.let {
                    mapLibreMap ->
                    mapLibreMap.getStyle {
                        enableLocationComponent(mapLibreMap)
                    }
                }

            } else {

                gpsStatus.text =
                    "● LOCATION PERMISSION DENIED"
            }
        }
    }

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

    @Deprecated("Deprecated in Android API")
    override fun onStatusChanged(
        provider: String?,
        status: Int,
        extras: Bundle?
    ) {
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onDestroy() {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            locationManager.removeUpdates(this)
        }

        mapView.onDestroy()

        super.onDestroy()
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {

        super.onSaveInstanceState(outState)

        mapView.onSaveInstanceState(outState)
    }
}

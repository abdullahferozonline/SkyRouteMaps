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

class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var locationManager: LocationManager

    private lateinit var gpsStatus: TextView
    private lateinit var locationText: TextView
    private lateinit var accuracyText: TextView
    private lateinit var speedText: TextView

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        gpsStatus = findViewById(R.id.gpsStatus)
        locationText = findViewById(R.id.locationText)
        accuracyText = findViewById(R.id.accuracyText)
        speedText = findViewById(R.id.speedText)

        locationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager

        requestLocationPermission()
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

            gpsStatus.text = "● GPS searching..."
            gpsStatus.setTextColor(0xFFFFC107.toInt())

        } catch (e: Exception) {

            gpsStatus.text = "● GPS unavailable"
            gpsStatus.setTextColor(0xFFFF5252.toInt())

            Toast.makeText(
                this,
                "GPS could not be started",
                Toast.LENGTH_LONG
            ).show()
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

            } else {

                gpsStatus.text = "● Location permission denied"
                gpsStatus.setTextColor(0xFFFF5252.toInt())

                Toast.makeText(
                    this,
                    "Location permission is required",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onProviderEnabled(provider: String) {

        if (provider == LocationManager.GPS_PROVIDER) {
            gpsStatus.text = "● GPS enabled"
        }
    }

    override fun onProviderDisabled(provider: String) {

        if (provider == LocationManager.GPS_PROVIDER) {
            gpsStatus.text = "● GPS disabled"
            gpsStatus.setTextColor(0xFFFF5252.toInt())
        }
    }

    @Deprecated("Deprecated in Android API")
    override fun onStatusChanged(
        provider: String?,
        status: Int,
        extras: Bundle?
    ) {
    }

    override fun onDestroy() {

        super.onDestroy()

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.removeUpdates(this)
        }
    }
}

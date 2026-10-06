package com.skyroute.maps

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var locationManager: LocationManager

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

            Toast.makeText(
                this,
                "GPS started 🛰️",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Unable to start GPS",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onLocationChanged(location: Location) {

        val latitude = location.latitude
        val longitude = location.longitude

        val accuracy = location.accuracy

        val speed =
            if (location.hasSpeed()) {
                location.speed * 3.6f
            } else {
                0f
            }

        println(
            "SkyRouteMaps GPS: " +
                    "Lat=$latitude " +
                    "Lon=$longitude " +
                    "Accuracy=$accuracy m " +
                    "Speed=$speed km/h"
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

                Toast.makeText(
                    this,
                    "Location permission is required for GPS",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

    override fun onStatusChanged(
        provider: String?,
        status: Int,
        extras: Bundle?
    ) {}
}

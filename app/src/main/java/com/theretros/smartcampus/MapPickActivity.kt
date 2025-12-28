package com.theretros.smartcampus

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapPickActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var selectButton: Button
    private lateinit var map: GoogleMap
    private var selectedLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_pick)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        selectButton = findViewById(R.id.selectButton)

        selectButton.setOnClickListener {
            selectedLatLng?.let {
                val data = intent.apply {
                    putExtra("LAT", it.latitude)
                    putExtra("LNG", it.longitude)
                }
                setResult(Activity.RESULT_OK, data)

            } ?: Toast.makeText(this, "Select a location first", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.0, 0.0), 2f))

        map.setOnMapClickListener { latLng ->
            selectedLatLng = latLng
            map.clear()
            map.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
        }
    }
}
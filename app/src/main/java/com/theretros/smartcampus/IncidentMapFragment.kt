package com.theretros.smartcampus

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.theretros.smartcampus.data.getIncidentMapInfo
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder

class IncidentMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_incident_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        loadIncidentsAndAddMarkers()
    }

    private fun loadIncidentsAndAddMarkers() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val incidents = getIncidentMapInfo()

                for (incident in incidents) {
                    val latLng = decodeWKBPoint(incident.location)
                    val iconRes = when (incident.class_id) {
                        1 -> R.drawable.ic_health_24
                        2 -> R.drawable.ic_safety_24
                        3 -> R.drawable.ic_environmental_24
                        4 -> R.drawable.ic_lost_and_found_24
                        5 -> R.drawable.ic_maintenance_24
                        else -> R.drawable.ic_location_24
                    }

                    val marker = googleMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(incident.incident_id.toString())
                            .icon(BitmapDescriptorFactory.fromBitmap(vectorToBitmap(iconRes)))
                    )
                    marker?.tag = incident.incident_id
                }

                googleMap.setOnMarkerClickListener { marker ->
                    val id = marker.tag as? Int ?: return@setOnMarkerClickListener false
                    val intent = Intent(requireContext(), NotificationDetailsActivity::class.java)
                    intent.putExtra("incidentId", id)
                    startActivity(intent)
                    true
                }

            } catch (e: Exception) {
                e.printStackTrace()
                println("Error loading incidents: ${e.message}")
            }
        }
    }

    fun decodeWKBPoint(hex: String): LatLng {
        // Convert hex string to byte array
        val bytes = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

        // Byte order: little endian (first byte 01 = little endian)
        val order = if (bytes[0].toInt() == 0x01) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN
        val buffer = ByteBuffer.wrap(bytes).order(order)

        buffer.position(1) // Skip byte order marker
        buffer.int // Skip geometry type (4 bytes)
        buffer.int // Skip SRID (4 bytes)

        val lng = buffer.double
        val lat = buffer.double
        return LatLng(lat, lng) // Google Maps uses (lat, lng)
    }

    fun vectorToBitmap(drawableRes: Int): Bitmap {
        val drawable: Drawable = ContextCompat.getDrawable(requireContext(), drawableRes)!!
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
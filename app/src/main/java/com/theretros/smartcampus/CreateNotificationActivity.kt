package com.theretros.smartcampus

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.theretros.smartcampus.data.insertIncident
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await


class CreateNotificationActivity : AppCompatActivity() {

    private var userId: Int = -1
    private lateinit var toolbar: MaterialToolbar
    // Type, Title, and Description Fields
    private lateinit var typeAutoComplete: AutoCompleteTextView
    private lateinit var titleTextLayout: TextInputLayout
    private lateinit var titleTextField: TextInputEditText
    private lateinit var descriptionTextLayout: TextInputLayout
    private lateinit var descriptionTextField: TextInputEditText

    // Location Choice
    private lateinit var locationChoice: RadioGroup
    private lateinit var optionUseCurrentLocation: MaterialRadioButton
    private lateinit var optionPickFromMap: MaterialRadioButton

    // Photo Section
    private lateinit var buttonAddPhoto: MaterialButton
    // You also have a button with id 'photo', I've named it buttonPhotoPreview
    private lateinit var buttonPhotoPreview: MaterialButton

    // Submit Button
    private lateinit var buttonSubmit: MaterialButton

    private lateinit var mMap: GoogleMap
    private var selectedLocation: LatLng? = null
    private var currentMarker: Marker? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                lifecycleScope.launch { getCurrentLocation() }
            }
        }

    @OptIn(ExperimentalTime::class)
    private val pickLocationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val lat = data?.getDoubleExtra("LAT", 0.0) ?: 0.0
                val lng = data?.getDoubleExtra("LNG", 0.0) ?: 0.0
                selectedLocation = LatLng(lat, lng)
                val locationString = "POINT($lng $lat)"

                val selectedClass = typeAutoComplete.text.toString()
                val classId = selectedClassToId(selectedClass)
                val title = titleTextField.text.toString()
                val description = descriptionTextField.text.toString()
                val reportTime = Clock.System.now()

                lifecycleScope.launch {
                    insertIncident(title, description, reportTime, locationString, classId, userId, "Open")
                }
                navigateToNotificationsList()
            }
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_notification)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userId = intent.getIntExtra("USER_ID", 1)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        toolbar = findViewById(R.id.toolbar)

        typeAutoComplete = findViewById(R.id.typeAutoComplete)
        fillClassDropdown()

        titleTextLayout = findViewById(R.id.titleTextLayout)
        titleTextField = findViewById(R.id.titleTextField)
        descriptionTextLayout = findViewById(R.id.descriptionTextLayout)
        descriptionTextField = findViewById(R.id.descriptionTextField)

        optionUseCurrentLocation = findViewById(R.id.option_1)
        optionPickFromMap = findViewById(R.id.option_2)

        buttonAddPhoto = findViewById(R.id.buttonAddPhoto)
        buttonPhotoPreview = findViewById(R.id.photo)

        locationChoice = findViewById(R.id.locationChoice)

        buttonSubmit = findViewById(R.id.buttonSubmit)
        buttonSubmit.setOnClickListener { validateFields() }
    }

    fun fillClassDropdown() {
        val types = listOf(
            "Health",
            "Safety",
            "Environmental",
            "Lost and Found",
            "Maintenance"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            types
        )

        typeAutoComplete.setAdapter(adapter)
    }

    @OptIn(ExperimentalTime::class)
    fun onSubmitButtonClick() {
        val selectedClass = typeAutoComplete.text.toString()
        val classId = selectedClassToId(selectedClass)
        val title = titleTextField.text.toString().trim()
        val description = descriptionTextField.text.toString().trim()
        val reportTime = Clock.System.now()

        val checkedId = locationChoice.checkedRadioButtonId

        when (checkedId) {
            R.id.option_1 -> {
                // Use current location
                lifecycleScope.launch {
                    val location = requestLocation()
                    insertIncident(title, description, reportTime, location, classId, userId, "Open")
                    navigateToNotificationsList()
                }
            }
            R.id.option_2 -> {
                // Pick location from map
                val intent = Intent(this@CreateNotificationActivity, MapPickActivity::class.java)
                pickLocationLauncher.launch(intent)

            }
            else -> {
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun validateFields() {
        val title = titleTextField.text.toString().trim()
        val description = descriptionTextField.text.toString().trim()
        val selectedClass = typeAutoComplete.text.toString().trim()
        if (title.isEmpty()) {
            clearErrors()
            titleTextLayout.error = "Title is required"
        } else if (description.isEmpty()) {
            clearErrors()
            descriptionTextLayout.error = "Description is required"
        } else if (selectedClass.isEmpty()) {
            clearErrors()
            Toast.makeText(this, "Please select a class", Toast.LENGTH_SHORT).show()
        } else {
            clearErrors()
            onSubmitButtonClick()
        }
    }

    fun clearErrors() {
        titleTextLayout.error = null
        descriptionTextLayout.error = null
    }

    fun selectedClassToId(selectedClass: String): Int {
        return when (selectedClass) {
            "Health" -> 1
            "Safety" -> 2
            "Environmental" -> 3
            "Lost and Found" -> 4
            "Maintenance" -> 5
            else -> 0
        }
    }


    private suspend fun requestLocation(): String {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return getCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            throw IllegalStateException("Location permission not granted yet")
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(): String {
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        val location = fusedLocationClient
            .getCurrentLocation(request, null)
            .await()

        if (location == null) {
            throw IllegalStateException("Location unavailable")
        }

        val lat = location.latitude
        val lng = location.longitude

        return "POINT($lng $lat)"
    }

    fun navigateToNotificationsList() {
        val intent = Intent(this, NotificationListActivity::class.java)
        startActivity(intent)
        finish()
    }
}
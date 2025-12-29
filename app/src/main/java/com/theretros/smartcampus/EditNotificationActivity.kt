package com.theretros.smartcampus

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.theretros.smartcampus.data.getIncidentDetails
import com.theretros.smartcampus.data.insertImageUrl
import com.theretros.smartcampus.data.updateIncident
import com.theretros.smartcampus.data.uploadImage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class EditNotificationActivity : AppCompatActivity() {

    private var userId: Int = 3
    private var incidentId: Int = 0
    private lateinit var savedLocation: String

    private lateinit var typeAutoComplete: MaterialAutoCompleteTextView
    private lateinit var selectStatusAutoComplete: MaterialAutoCompleteTextView
    private lateinit var titleTextLayout: TextInputLayout
    private lateinit var titleTextField: TextInputEditText
    private lateinit var descriptionTextLayout: TextInputLayout
    private lateinit var descriptionTextField: TextInputEditText

    private lateinit var locationChoice: RadioGroup
    private lateinit var optionUseCurrentLocation: MaterialRadioButton
    private lateinit var optionPickFromMap: MaterialRadioButton

    private lateinit var buttonAddPhoto: MaterialButton
    private lateinit var buttonSubmit: MaterialButton

    private val selectedImageUris = mutableListOf<Uri>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val mainScope = MainScope()

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) mainScope.launch { getCurrentLocation() }
        }

    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNotEmpty()) {
                selectedImageUris.addAll(uris)
                Toast.makeText(this, "${uris.size} images added", Toast.LENGTH_SHORT).show()
            }
        }

    private val pickLocationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val lat = data.getDoubleExtra("LAT", 0.0)
                val lng = data.getDoubleExtra("LNG", 0.0)
                mainScope.launch { submitIncident("POINT($lng $lat)") }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_edit_notification)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        println("Loaded edit activity")
        incidentId = intent.getIntExtra("incidentId", -1)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        typeAutoComplete = findViewById(R.id.typeAutoComplete)
        selectStatusAutoComplete = findViewById(R.id.selectStatusAutoComplete)

        titleTextLayout = findViewById(R.id.titleTextLayout)
        titleTextField = findViewById(R.id.titleTextField)
        descriptionTextLayout = findViewById(R.id.descriptionTextLayout)
        descriptionTextField = findViewById(R.id.descriptionTextField)

        locationChoice = findViewById(R.id.locationChoice)
        optionUseCurrentLocation = findViewById(R.id.option_1)
        optionPickFromMap = findViewById(R.id.option_2)

        buttonAddPhoto = findViewById(R.id.buttonAddPhoto)
        buttonSubmit = findViewById(R.id.buttonSubmit)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)

        toolbar.setNavigationOnClickListener { finish() }

        fillFields(incidentId)
        fillClassDropdown()
        fillStatusDropdown()

        buttonAddPhoto.setOnClickListener {
            pickMultipleMedia.launch("image/*")
        }

        buttonSubmit.setOnClickListener {
            validateFields()
        }
    }

    private fun fillStatusDropdown() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            listOf("Open", "Under Review", "Resolved")
        )
        selectStatusAutoComplete.setAdapter(adapter)
    }

    private fun fillClassDropdown() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            listOf("Health", "Safety", "Environmental", "Lost and Found", "Maintenance")
        )
        typeAutoComplete.setAdapter(adapter)
    }

    private fun fillFields(incidentId: Int) {
        lifecycleScope.launch {
            val incidentDetail = getIncidentDetails(incidentId)
            titleTextField.setText(incidentDetail.title)
            descriptionTextField.setText(incidentDetail.description)
            typeAutoComplete.setText(classIdToName(incidentDetail.class_id), false)
            selectStatusAutoComplete.setText(incidentDetail.status, false)
            savedLocation = incidentDetail.location
        }
    }

    private fun classIdToName(classId: Int): String {
        return when (classId) {
            1 -> "Health"
            2 -> "Safety"
            3 -> "Environmental"
            4 -> "Lost and Found"
            5 -> "Maintenance"
            else -> ""
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun validateFields() {
        when {
            titleTextField.text.isNullOrBlank() ->
                titleTextLayout.error = "Title is required"

            descriptionTextField.text.isNullOrBlank() ->
                descriptionTextLayout.error = "Description is required"

            typeAutoComplete.text.isNullOrBlank() ->
                Toast.makeText(this, "Select a class", Toast.LENGTH_SHORT).show()

            else -> submitIncidentLocation()
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun submitIncidentLocation() {
        when (locationChoice.checkedRadioButtonId) {
            R.id.option_1 ->
                mainScope.launch {
                    submitIncident(savedLocation)
                }
            R.id.option_2 -> {
                mainScope.launch {
                    submitIncident(requestLocation())
                }
            }
            R.id.option_3 -> {
                pickLocationLauncher.launch(
                    Intent(this, MapPickActivity::class.java)
                )
            }

            else -> Toast.makeText(this, "Select location", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun submitIncident(location: String) {
        val classId = selectedClassToId(typeAutoComplete.text.toString())
        val title = titleTextField.text.toString()
        val description = descriptionTextField.text.toString()

        updateIncident(
            incidentId,
            title,
            description,
            Clock.System.now(),
            location,
            classId,
            userId,
            selectStatusAutoComplete.text.toString()
        )

        selectedImageUris.forEach {
            val path = uploadImage("incident-images", it, contentResolver)
            insertImageUrl(incidentId, path)
        }

        Toast.makeText(this, "Incident submitted", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun selectedClassToId(text: String) = when (text) {
        "Health" -> 1
        "Safety" -> 2
        "Environmental" -> 3
        "Lost and Found" -> 4
        "Maintenance" -> 5
        else -> 0
    }

    private suspend fun requestLocation(): String {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return getCurrentLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            throw IllegalStateException("Permission not granted")
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(): String {
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        val location = fusedLocationClient.getCurrentLocation(request, null).await()
        return "POINT(${location.longitude} ${location.latitude})"
    }
}
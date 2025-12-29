package com.theretros.smartcampus

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.theretros.smartcampus.data.insertImageUrl
import com.theretros.smartcampus.data.insertIncident
import com.theretros.smartcampus.data.uploadImage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class CreateNotificationFragment : Fragment() {

    private var userId: Int = 0
    private lateinit var typeAutoComplete: MaterialAutoCompleteTextView
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

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) lifecycleScope.launch { getCurrentLocation() }
        }

    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNotEmpty()) {
                selectedImageUris.addAll(uris)
                Toast.makeText(requireContext(), "${uris.size} images added", Toast.LENGTH_SHORT).show()
            }
        }

    private val pickLocationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val lat = data.getDoubleExtra("LAT", 0.0)
                val lng = data.getDoubleExtra("LNG", 0.0)
                submitIncident("POINT($lng $lat)")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.activity_create_notification, container, false)
    }

    @OptIn(ExperimentalTime::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = SessionManager(requireContext())
        userId = session.getUserId()!!.toInt()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        typeAutoComplete = view.findViewById(R.id.typeAutoComplete)
        titleTextLayout = view.findViewById(R.id.titleTextLayout)
        titleTextField = view.findViewById(R.id.titleTextField)
        descriptionTextLayout = view.findViewById(R.id.descriptionTextLayout)
        descriptionTextField = view.findViewById(R.id.descriptionTextField)

        locationChoice = view.findViewById(R.id.locationChoice)
        optionUseCurrentLocation = view.findViewById(R.id.option_1)
        optionPickFromMap = view.findViewById(R.id.option_2)

        buttonAddPhoto = view.findViewById(R.id.buttonAddPhoto)
        buttonSubmit = view.findViewById(R.id.buttonSubmit)

        fillClassDropdown()

        buttonAddPhoto.setOnClickListener {
            pickMultipleMedia.launch("image/*")
        }

        buttonSubmit.setOnClickListener {
            validateFields()
        }
    }

    private fun fillClassDropdown() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            listOf("Health", "Safety", "Environmental", "Lost and Found", "Maintenance")
        )
        typeAutoComplete.setAdapter(adapter)
    }

    @OptIn(ExperimentalTime::class)
    private fun validateFields() {
        when {
            titleTextField.text.isNullOrBlank() ->
                titleTextLayout.error = "Title is required"

            descriptionTextField.text.isNullOrBlank() ->
                descriptionTextLayout.error = "Description is required"

            typeAutoComplete.text.isNullOrBlank() ->
                Toast.makeText(requireContext(), "Select a class", Toast.LENGTH_SHORT).show()

            else -> submitByLocationChoice()
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun submitByLocationChoice() {
        when (locationChoice.checkedRadioButtonId) {
            R.id.option_1 -> lifecycleScope.launch {
                submitIncident(requestLocation())
            }

            R.id.option_2 -> {
                pickLocationLauncher.launch(
                    Intent(requireContext(), MapPickActivity::class.java)
                )
            }

            else -> Toast.makeText(requireContext(), "Select location", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun submitIncident(location: String) {
        val classId = selectedClassToId(typeAutoComplete.text.toString())
        val title = titleTextField.text.toString()
        val description = descriptionTextField.text.toString()

        lifecycleScope.launch {
            val id = insertIncident(
                title,
                description,
                Clock.System.now(),
                location,
                classId,
                userId,
                "Open"
            )

            selectedImageUris.forEach {
                val path = uploadImage("incident-images", it, requireContext().contentResolver)
                insertImageUrl(id, path)
            }
            parentFragmentManager.popBackStack()
        }
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
                requireContext(),
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

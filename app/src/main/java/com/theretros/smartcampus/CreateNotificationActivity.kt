package com.theretros.smartcampus

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import android.location.Location
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Marker


class CreateNotificationActivity : AppCompatActivity() {

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_notification)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.toolbar)

        typeAutoComplete = findViewById(R.id.typeAutoComplete)
        titleTextLayout = findViewById(R.id.titleTextLayout)
        titleTextField = findViewById(R.id.titleTextField)
        descriptionTextLayout = findViewById(R.id.descriptionTextLayout)
        descriptionTextField = findViewById(R.id.descriptionTextField)

        locationChoice = findViewById(R.id.locationChoice)
        optionUseCurrentLocation = findViewById(R.id.option_1)
        optionPickFromMap = findViewById(R.id.option_2)

        buttonAddPhoto = findViewById(R.id.buttonAddPhoto)
        buttonPhotoPreview = findViewById(R.id.photo)

        buttonSubmit = findViewById(R.id.buttonSubmit)

        buttonSubmit.setOnClickListener {
            val selectedType = typeAutoComplete.text.toString()
            val title = titleTextField.text.toString()
            val description = descriptionTextField.text.toString()

        }

    }

}
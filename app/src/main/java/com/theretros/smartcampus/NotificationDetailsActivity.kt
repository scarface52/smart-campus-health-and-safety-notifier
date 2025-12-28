package com.theretros.smartcampus

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.theretros.smartcampus.data.followIncident
import com.theretros.smartcampus.data.getImages
import com.theretros.smartcampus.data.getIncidentDetails
import com.theretros.smartcampus.data.isIncidentFollowed
import com.theretros.smartcampus.data.unfollowIncident
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime
import coil.load
import com.theretros.smartcampus.data.ImagePagerAdapter

class NotificationDetailsActivity : AppCompatActivity() {

    private var incidentId: Int = -1
    private var userId: Int = -1
    private lateinit var titleView: TextView
    private lateinit var dateView: TextView
    private lateinit var statusText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var statusIcon: ImageView
    private lateinit var incidentCard: MaterialButton
    private lateinit var starButton: MaterialButton
    private lateinit var imagePager: ViewPager2
    private lateinit var photosCardView: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        incidentId = intent.getIntExtra("INCIDENT_ID", 2)
        userId = intent.getIntExtra("USER_ID", 1)

        if (incidentId == -1) {
            finish()
        }

        println("incident id: $incidentId")

        titleView = findViewById(R.id.titleView)
        incidentCard = findViewById(R.id.incidentCard)
        dateView = findViewById(R.id.dateView)
        statusIcon = findViewById(R.id.statusIcon)
        statusText = findViewById(R.id.statusText)
        descriptionText = findViewById(R.id.descriptionText)
        starButton = findViewById(R.id.starButton)
        imagePager = findViewById(R.id.imagePager)
        photosCardView = findViewById(R.id.photosCardView)

        setStarButton()
        starButton.setOnClickListener { onStarButtonClick() }

        setIncidentDetails()
        setPhotosView()
    }

    fun setPhotosView() {
        lifecycleScope.launch {
            val images = getImages(incidentId)
            if (images.isEmpty()) {
                photosCardView.visibility = View.GONE
                return@launch
            }
            val pager = findViewById<ViewPager2>(R.id.imagePager)
            pager.adapter = ImagePagerAdapter(images.map { it.image_url } )
        }
    }

    fun onStarButtonClick() {
        lifecycleScope.launch {
            if (starButton.isChecked) {
                followIncident(userId, incidentId)
                starButton.icon = getDrawable(R.drawable.ic_filled_star_24)
            }
            else {
                unfollowIncident(userId, incidentId)
                starButton.icon = getDrawable(R.drawable.ic_empty_star_24)
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun setIncidentDetails() {
        lifecycleScope.launch {
            val incidentDetails = getIncidentDetails(incidentId)
            titleView.text = incidentDetails.title
            dateView.text = incidentDetails.report_time.toString()
            statusText.text = incidentDetails.status
            setStatusIcon(incidentDetails.status)
            descriptionText.text = incidentDetails.description
            setIncidentCard()
            setStarButton()
        }
    }

    fun setIncidentCard() {
        when (incidentId) {
            1 -> {
                incidentCard.text = getString(R.string.health)
                incidentCard.icon = getDrawable(R.drawable.ic_health_24)
            }
            2 -> {
                incidentCard.text = getString(R.string.safety)
                incidentCard.icon = getDrawable(R.drawable.ic_safety_24)
            }
            3 -> {
                incidentCard.text = getString(R.string.environmental)
                incidentCard.icon = getDrawable(R.drawable.ic_environmental_24)
            }
            4 -> {
                incidentCard.text = getString(R.string.lost_and_found)
                incidentCard.icon = getDrawable(R.drawable.ic_lost_and_found_24)
            }
            5 -> {
                incidentCard.text = getString(R.string.maintenance)
                incidentCard.icon = getDrawable(R.drawable.ic_maintenance_24)
            }
        }
    }

    fun setStatusIcon(status: String) {
        when (status) {
            "Open" -> statusIcon.setImageResource(R.drawable.filled_circle)
            "Under Review" -> statusIcon.setImageResource(R.drawable.ic_maintenance_24)
            "Resolved" -> statusIcon.setImageResource(R.drawable.tick_circle)
            else -> statusIcon.setImageResource(R.drawable.ic_search_24)
        }
    }

    fun setStarButton() {
        lifecycleScope.launch {
            if (isIncidentFollowed(incidentId, userId))
            {
                starButton.isChecked = true
                starButton.icon = getDrawable(R.drawable.ic_filled_star_24)
            }
            else {
                starButton.isChecked = false
                starButton.icon = getDrawable(R.drawable.ic_empty_star_24)
            }
        }
    }
}
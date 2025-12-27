package com.theretros.smartcampus

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.theretros.smartcampus.data.followIncidentClass
import com.theretros.smartcampus.data.getFollowedIncidentClasses
import com.theretros.smartcampus.data.getUserInfo
import com.theretros.smartcampus.data.unfollowIncidentClass
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private var followedClasses = mutableSetOf<Int>()
    private lateinit var nameView: TextView
    private lateinit var emailView: TextView
    private lateinit var roleView: TextView
    private lateinit var unitView: TextView
    private lateinit var healthButton: MaterialButton
    private lateinit var safetyButton: MaterialButton
    private lateinit var environmentButton: MaterialButton
    private lateinit var lostAndFoundButton: MaterialButton
    private lateinit var maintenanceButton: MaterialButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // TODO: get userId from intent
        //val userId = intent.getIntExtra("USER_ID", -1)
        val userId = 11

        nameView = findViewById(R.id.nameView)
        emailView = findViewById(R.id.emailView)
        roleView = findViewById(R.id.roleView)
        unitView = findViewById(R.id.unitView)
        healthButton = findViewById(R.id.healthButton)
        safetyButton = findViewById(R.id.safetyButton)
        environmentButton = findViewById(R.id.environmentButton)
        lostAndFoundButton = findViewById(R.id.lostAndFoundButton)
        maintenanceButton = findViewById(R.id.maintenanceButton)

        setUserInfo(userId)
        updateFollowedIncidentClasses(userId)
        setupListeners(userId)
    }

    fun setUserInfo(userId: Int) {
        lifecycleScope.launch {
            val userInfo = getUserInfo(userId)
            nameView.text = "${userInfo.name} ${userInfo.last_name}"
            emailView.text = userInfo.email
            roleView.text = "Role: ${userInfo.role}"
            unitView.text = "Faculty: ${userInfo.faculty}"
        }
    }

    fun updateFollowedIncidentClasses(userId: Int) {

        healthButton.isCheckable = true
        safetyButton.isCheckable = true
        environmentButton.isCheckable = true
        lostAndFoundButton.isCheckable = true
        maintenanceButton.isCheckable = true

        healthButton.isChecked = false
        safetyButton.isChecked = false
        environmentButton.isChecked = false
        lostAndFoundButton.isChecked = false
        maintenanceButton.isChecked = false

        lifecycleScope.launch {
            followedClasses = getFollowedIncidentClasses(userId)
            for (id in followedClasses) {
                when (id) {
                    1 -> healthButton.isChecked = true
                    2 -> safetyButton.isChecked = true
                    3 -> environmentButton.isChecked = true
                    4 -> lostAndFoundButton.isChecked = true
                    5 -> maintenanceButton.isChecked = true
                }
            }
        }
    }

    fun setupListeners(userId: Int) {
        println("setup listeners")

        healthButton.addOnCheckedChangeListener { _, isChecked ->
            println("health button clicked")
            lifecycleScope.launch {
                if (isChecked) {
                    followIncidentClass(userId, 1)
                    followedClasses.add(1)
                } else {
                    unfollowIncidentClass(userId, 1)
                    followedClasses.remove(1)
                }
            }
        }

        safetyButton.addOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                if (isChecked) {
                    followIncidentClass(userId, 2)
                    followedClasses.add(2)
                } else {
                    unfollowIncidentClass(userId, 2)
                    followedClasses.remove(2)
                }
            }
        }

        environmentButton.addOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                if (isChecked) {
                    followIncidentClass(userId, 3)
                    followedClasses.add(3)
                } else {
                    unfollowIncidentClass(userId, 3)
                    followedClasses.remove(3)
                }
            }
        }

        lostAndFoundButton.addOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                if (isChecked) {
                    followIncidentClass(userId, 4)
                    followedClasses.add(4)
                } else {
                    unfollowIncidentClass(userId, 4)
                    followedClasses.remove(4)
                }
            }
        }

        maintenanceButton.addOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                if (isChecked) {
                    followIncidentClass(userId, 5)
                    followedClasses.add(5)
                } else {
                    unfollowIncidentClass(userId, 5)
                    followedClasses.remove(5)
                }
            }
        }
    }
}
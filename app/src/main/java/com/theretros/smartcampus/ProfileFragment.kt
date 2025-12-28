package com.theretros.smartcampus

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.theretros.smartcampus.data.followIncidentClass
import com.theretros.smartcampus.data.getFollowedIncidentClasses
import com.theretros.smartcampus.data.getUserInfo
import com.theretros.smartcampus.data.unfollowIncidentClass
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Get userId from Fragment arguments
        val session = SessionManager(requireContext())
        val userId = session.getUserId()!!.toInt()
        if (userId == -1) {
            navigateToLogin()
            return view
        }

        // Bind views
        nameView = view.findViewById(R.id.nameView)
        emailView = view.findViewById(R.id.emailView)
        roleView = view.findViewById(R.id.roleView)
        unitView = view.findViewById(R.id.unitView)

        healthButton = view.findViewById(R.id.healthButton)
        safetyButton = view.findViewById(R.id.safetyButton)
        environmentButton = view.findViewById(R.id.environmentButton)
        lostAndFoundButton = view.findViewById(R.id.lostAndFoundButton)
        maintenanceButton = view.findViewById(R.id.maintenanceButton)

        setUserInfo(userId)
        updateFollowedIncidentClasses(userId)
        setupListeners(userId)

        return view
    }

    private fun setUserInfo(userId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val userInfo = getUserInfo(userId)
            nameView.text = "${userInfo.name} ${userInfo.last_name}"
            emailView.text = userInfo.email
            roleView.text = "Role: ${userInfo.role}"
            unitView.text = "Faculty: ${userInfo.faculty}"
        }
    }

    private fun updateFollowedIncidentClasses(userId: Int) {

        val buttons = listOf(
            healthButton,
            safetyButton,
            environmentButton,
            lostAndFoundButton,
            maintenanceButton
        )

        buttons.forEach {
            it.isCheckable = true
            it.isChecked = false
        }

        viewLifecycleOwner.lifecycleScope.launch {
            followedClasses = getFollowedIncidentClasses(userId)
            followedClasses.forEach { id ->
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

    private fun setupListeners(userId: Int) {

        healthButton.addOnCheckedChangeListener { _, isChecked ->
            toggleFollow(userId, 1, isChecked)
        }

        safetyButton.addOnCheckedChangeListener { _, isChecked ->
            toggleFollow(userId, 2, isChecked)
        }

        environmentButton.addOnCheckedChangeListener { _, isChecked ->
            toggleFollow(userId, 3, isChecked)
        }

        lostAndFoundButton.addOnCheckedChangeListener { _, isChecked ->
            toggleFollow(userId, 4, isChecked)
        }

        maintenanceButton.addOnCheckedChangeListener { _, isChecked ->
            toggleFollow(userId, 5, isChecked)
        }
    }

    private fun toggleFollow(userId: Int, classId: Int, isChecked: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            if (isChecked) {
                followIncidentClass(userId, classId)
                followedClasses.add(classId)
            } else {
                unfollowIncidentClass(userId, classId)
                followedClasses.remove(classId)
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}

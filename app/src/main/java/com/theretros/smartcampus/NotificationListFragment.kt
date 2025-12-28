package com.theretros.smartcampus

import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.theretros.smartcampus.adapters.NotificationCardAdapter
import com.theretros.smartcampus.data.dataclasses.IncidentWithFollow
import com.theretros.smartcampus.data.getFollowedIncidents
import com.theretros.smartcampus.data.getIncidentsWithFollowStatus
import com.theretros.smartcampus.data.searchIncidents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime

class NotificationListFragment :
    Fragment(R.layout.fragment_notification_list) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var autoCompleteTextView: MaterialAutoCompleteTextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        autoCompleteTextView = view.findViewById(R.id.autoCompleteTextView)

        val items = listOf(
            "Date", "Health", "Safety", "Environmental", "Lost and Found",
            "Maintenance", "Followed", "Open", "Under Review", "Resolved"
        )

        autoCompleteTextView.setSimpleItems(items.toTypedArray())

        fillRecyclerViewWithFilter(null, null)

        autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position).toString()

            when {
                position == 0 -> fillRecyclerViewWithFilter(null, null)
                position <= 5 -> fillRecyclerViewWithFilter(position, null)
                position == 6 -> fillRecyclerViewWithFollowed()
                else -> fillRecyclerViewWithFilter(null, selectedItem)
            }
        }

        val searchTextField =
            view.findViewById<TextInputEditText>(R.id.searchTextField)
        val searchLayout =
            view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.searchLayout)

        searchTextField.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                searchLayout.startIconDrawable = null
                searchLayout.endIconDrawable =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.ic_search_24)

                searchLayout.setEndIconOnClickListener {
                    val query = searchTextField.text?.toString().orEmpty()
                    fillRecyclerViewWithSearch(query)
                }
            } else {
                searchLayout.endIconDrawable = null
                searchLayout.startIconDrawable =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.ic_search_24)
            }
        }
    }

    fun fillRecyclerViewWithFilter(typeFilter: Int?, statusFilter: String?) {
        viewLifecycleOwner.lifecycleScope.launch {
            val notifications = withContext(Dispatchers.IO) {
                getIncidentsWithFollowStatus(
                    userId = 11,
                    classId = typeFilter,
                    status = statusFilter
                )
            }
            recyclerView.adapter = NotificationCardAdapter(notifications)
        }
    }

    @OptIn(ExperimentalTime::class)
    fun fillRecyclerViewWithFollowed() {
        viewLifecycleOwner.lifecycleScope.launch {
            val tmpNotifications = withContext(Dispatchers.IO) {
                getFollowedIncidents(userId = 11)
            }

            val notifications = tmpNotifications.map {
                IncidentWithFollow(
                    incident_id = it.incident_id,
                    title = it.title,
                    description = it.description,
                    report_time = it.report_time,
                    class_id = it.class_id,
                    status = it.status,
                    is_followed = true
                )
            }

            recyclerView.adapter = NotificationCardAdapter(notifications)
        }
    }

    fun fillRecyclerViewWithSearch(query: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val notifications = withContext(Dispatchers.IO) {
                searchIncidents(query)
            }
            recyclerView.adapter = NotificationCardAdapter(notifications)
        }
    }
}

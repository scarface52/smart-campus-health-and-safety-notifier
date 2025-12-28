package com.theretros.smartcampus

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

class NotificationListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var autoCompleteTextView: MaterialAutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        fillRecyclerViewWithFilter(null, null)


        autoCompleteTextView = findViewById(R.id.autoCompleteTextView)
        val items = listOf(
            "Date", "Health", "Safety", "Environmental", "Lost and Found",
            "Maintenance","Followed", "Open", "Under Review", "Resolved"
        )
        val session = SessionManager(this)
        println(session.getUserId())
        println(session.isAdmin())

        autoCompleteTextView.setSimpleItems(items.toTypedArray())

        autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position).toString()

            when {
                position == 0 -> fillRecyclerViewWithFilter(null, null)
                position <= 5 -> fillRecyclerViewWithFilter(position, null)
                position == 6 -> fillRecyclerViewWithFollowed()
                else -> fillRecyclerViewWithFilter(null, selectedItem)
            }


        }
        val searchTextField = findViewById<TextInputEditText>(R.id.searchTextField)
        val searchLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.searchLayout)


        searchTextField.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Move icon to END
                searchLayout.startIconDrawable = null
                searchLayout.endIconDrawable =
                    AppCompatResources.getDrawable(this, R.drawable.ic_search_24)

                searchLayout.setEndIconOnClickListener {
                    val query = searchTextField.text?.toString().orEmpty()
                    fillRecyclerViewWithSearch(query)
                }
            } else {
                // Move icon back to START
                searchLayout.endIconDrawable = null
                searchLayout.startIconDrawable =
                    AppCompatResources.getDrawable(this, R.drawable.ic_search_24)
            }
        }
    }
    fun fillRecyclerViewWithFilter(typeFilter: Int?, statusFilter: String?) {
        lifecycleScope.launch {

            val notifications = withContext(Dispatchers.IO) {
                getIncidentsWithFollowStatus(userId = 11, classId = typeFilter, status = statusFilter)
            }
            recyclerView = findViewById(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(this@NotificationListActivity)
            recyclerView.adapter = NotificationCardAdapter(notifications)
        }
    }

    @OptIn(ExperimentalTime::class)
    fun fillRecyclerViewWithFollowed(){
        lifecycleScope.launch {
            println(456789876545678)
            val tmpNotifications = withContext(Dispatchers.IO) {
                getFollowedIncidents(userId = 11)
            }
            println(tmpNotifications)
            println(1234567890)
            val notifications = mutableListOf<IncidentWithFollow>()
            tmpNotifications.forEach {
                val notification = IncidentWithFollow(
                    incident_id = it.incident_id,
                    title = it.title,
                    description = it.description,
                    report_time = it.report_time,
                    class_id = it.class_id,
                    status = it.status,
                    is_followed = true
                )
                notifications += notification
            }

            recyclerView = findViewById(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(this@NotificationListActivity)
            recyclerView.adapter = NotificationCardAdapter(notifications)
        }
    }

    fun fillRecyclerViewWithSearch(query: String){
        lifecycleScope.launch {

            val notifications = withContext(Dispatchers.IO) {
                searchIncidents(query)
            }
            recyclerView = findViewById(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(this@NotificationListActivity)
            recyclerView.adapter = NotificationCardAdapter(notifications)
        }


}
}
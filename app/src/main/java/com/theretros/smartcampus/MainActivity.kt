package com.theretros.smartcampus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.theretros.smartcampus.adapters.NotificationCardAdapter
import com.theretros.smartcampus.data.checkLoginInfo
import com.theretros.smartcampus.data.classes
import com.theretros.smartcampus.data.getIncidentsWithFollowStatus
import com.theretros.smartcampus.data.getUserInfo
import com.theretros.smartcampus.data.searchIncidents
import kotlinx.coroutines.delay


class MainActivity : AppCompatActivity() {

    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var fragmentContainer: androidx.fragment.app.FragmentContainerView
    private lateinit var currentFragment: String
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        currentFragment = ""

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView = findViewById<NavigationView>(R.id.navView)
        drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

        session = SessionManager(this)
        session.saveSession("3", true)

        setupClickListeners()
        fillPersonalInfoOnNavBar()
    }

    fun setupClickListeners() {
        val headerView = navView.getHeaderView(0)
        headerView.findViewById<MaterialButton>(R.id.buttonIncidents).setOnClickListener {
            if (currentFragment != "Incident Notifications") {
                openFragment(NotificationListFragment(), true)
                currentFragment = "Incident Notifications"
            }
            drawerLayout.closeDrawers()
        }
        headerView.findViewById<MaterialButton>(R.id.buttonNew).setOnClickListener {
            if (currentFragment != "New Notification") {
                openFragment(NotificationListFragment(), true)
                currentFragment = "New Notification"
            }
            drawerLayout.closeDrawers()
        }
        headerView.findViewById<MaterialButton>(R.id.buttonIncidents).setOnClickListener {
            if (currentFragment != "See Locations") {
                openFragment(NotificationListFragment(), true)
                currentFragment = "See Locations"
            }
            drawerLayout.closeDrawers()
        }
        headerView.findViewById<MaterialButton>(R.id.logoutButton).setOnClickListener {
            session.clearSession()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        headerView.findViewById<ConstraintLayout>(R.id.profileLayout).setOnClickListener {
            if (currentFragment != "Profile") {
                openFragment(ProfileFragment(), true)
                currentFragment = "Profile"
            }
            drawerLayout.closeDrawers()
        }
    }

    fun openFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    fun fillPersonalInfoOnNavBar() {
        lifecycleScope.launch {

            val userInfo  = withContext(Dispatchers.IO) {
                getUserInfo(session.getUserId()!!.toInt())
            }
            val name = "${userInfo.name} ${userInfo.last_name}"
            navView.findViewById<TextView>(R.id.nameText).text = name
            navView.findViewById<TextView>(R.id.mailText).text = userInfo.email
        }
    }
}
package com.theretros.smartcampus.data

import com.theretros.smartcampus.data.dataclasses.Incident
import com.theretros.smartcampus.data.dataclasses.IncidentSummary
import com.theretros.smartcampus.data.dataclasses.User
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


val supabase = createSupabaseClient(
    supabaseUrl = DATABASE_URL,
    supabaseKey = anon
) {
    // Install the necessary modules
    install(Postgrest)
    install(Storage)
}

@OptIn(ExperimentalTime::class)
fun insertIncident(
                   title: String,
                   description: String,
                   report_time: Instant,
                   location: String,
                   class_id: Int,
                   owner_id: Int,
                   status: String) {
    val incident = Incident( title, description, report_time, location, class_id, owner_id, status)
    CoroutineScope(Dispatchers.IO).launch {
        try {
            supabase.from("incidents").insert(incident)
            println("Incident successfully inserted!")
        } catch (e: Exception) {
            println("Error inserting incident: ${e.message}")
            throw e
        }
    }
}

fun getFollowedIncidents(userId: Int, classId: Int, status: String): MutableList<IncidentSummary> {

    val followedIncidents = mutableListOf<Int>()
    val incidents = mutableListOf<IncidentSummary>()

    CoroutineScope(Dispatchers.IO).launch {

        val result1 = supabase.from("followed_incidents").select(
            columns = Columns.list(
                "incident_id"
            )
        ) {
            filter {
                eq("user_id", userId)
            }
        }.decodeList<Int>()
        followedIncidents.addAll(result1)

        val result2 = supabase.from("incidents").select(
            columns = Columns.list(
                "incident_id",
                "title",
                "description",
                "report_time",
                "class_id",
                "status"
            )
        ) {
            filter {
                and {
                    isIn("incident_id", followedIncidents)
                }
                and {
                    eq("class_id", classId)
                }
                and {
                    eq("status", status)

                }
            }
        }.decodeList<IncidentSummary>()
        incidents.addAll(result2)
    }
    return incidents
}

// how to use suspend functions
fun insertUser(user_id: Int,
               name: String,
               last_name: String,
               email: String,
               password: String,
               faculty: String,
               role: String,
               jurisdiction: String){
    val user = User(user_id, name, last_name, email, password, faculty, role, jurisdiction)
    // suspend functions must be inside a coroutine scope
    CoroutineScope(Dispatchers.IO).launch {
        // Data insertion
        try {
            supabase.from("users").insert(user)
            println("User successfully inserted!")
        } catch (e: Exception) {
            println("Error inserting user: ${e.message}")
            throw e
        }
    }
}

// get all users
fun getAllUsers() {
    val users = mutableListOf<User>()
    CoroutineScope(Dispatchers.IO).launch {
        val result = supabase.from("users").select().decodeList<User>()
        users.addAll(result)
        for (user in users) {
            println(user)
        }
    }
}
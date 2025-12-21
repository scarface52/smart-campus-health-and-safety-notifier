package com.theretros.smartcampus.data

import com.theretros.smartcampus.data.dataclasses.FollowedIncident
import com.theretros.smartcampus.data.dataclasses.Incident
import com.theretros.smartcampus.data.dataclasses.IncidentDetail
import com.theretros.smartcampus.data.dataclasses.IncidentMapInfo
import com.theretros.smartcampus.data.dataclasses.IncidentSummary
import com.theretros.smartcampus.data.dataclasses.User
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.Storage
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


// Create the supabase client
val supabase = createSupabaseClient(
    supabaseUrl = DATABASE_URL,
    supabaseKey = anon
) {
    // Install the necessary modules
    install(Postgrest)
    install(Storage)
}

// Inserts a new incident
@OptIn(ExperimentalTime::class)
suspend fun insertIncident(
                   title: String,
                   description: String,
                   report_time: Instant,
                   location: String,
                   class_id: Int,
                   owner_id: Int,
                   status: String) {
    val incident = Incident(title, description, report_time, location, class_id, owner_id, status)
    try {
        supabase.from("incidents").insert(incident)
        println("Incident successfully inserted!")
    } catch (e: Exception) {
        println("Error inserting incident: ${e.message}")
        throw e
    }
}

// Gets followed incidents from a user id with class id and status filters
suspend fun getFollowedIncidents(userId: Int, classId: Int, status: String): List<IncidentSummary> {
    val followedIncidents = supabase.from("followed_incidents").select(
        columns = Columns.list(
            "incident_id"
        )
    ) {
        filter {
            eq("user_id", userId)
        }
    }.decodeList<Int>()

    val incidents = supabase.from("incidents").select(
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
                eq("class_id", classId)
                eq("status", status)
            }
        }
    }.decodeList<IncidentSummary>()

    return incidents
}

// Gets incident information for the map screen
suspend fun getIncidentMapInfo(incidentId: Int): List<IncidentMapInfo> {
    val incidentMapInfo = supabase.from("incidents").select(
        columns = Columns.list(
            "location",
            "class_id"
        )
    ) {
        filter {
            eq("incident_id", incidentId)
        }
    }.decodeList<IncidentMapInfo>()

    return incidentMapInfo
}

// Gets incident details from an incident id
suspend fun getIncidentDetail(incidentId: Int): IncidentDetail {
    val incidentDetail = supabase.from("incidents").select(
        columns = Columns.raw("""
            title,
            description,
            report_time,
            location,
            class_id,
            status,
            incident_images (
                image_id,
                incident_id,
                image_url
            )
        """)
    ) {
        filter {
            eq("incident_id", incidentId)
        }
    }.decodeSingle<IncidentDetail>()

    return incidentDetail
}

// Follows an incident if not already followed (database constraint exists)
suspend fun followIncident(userId: Int, incidentId: Int) {
    val followedIncident = FollowedIncident(userId, incidentId)
    supabase.from("followed_incidents").upsert(followedIncident)
}

// Unfollows an incident
suspend fun unfollowIncident(userId: Int, incidentId: Int) {
    supabase.from("followed_incidents").delete {
        filter {
            eq("user_id", userId)
            eq("incident_id", incidentId)
        }
    }
}

// Updates incident status
suspend fun updateIncidentStatus(incidentId: Int, status: String) {
    supabase.from("incidents").update(
        {
            set("status", status)
        }
    ) {
        filter {
            eq("incident_id", incidentId)
        }
    }
}

// Updates incident description
suspend fun updateIncidentDescription(incidentId: Int, description: String) {
    supabase.from("incidents").update(
        {
            set("description", description)
        }
    ) {
        filter {
            eq("incident_id", incidentId)
        }
    }
}

// Deletes an incident
suspend fun deleteIncident(incidentId: Int) {
    supabase.from("incidents").delete {
        filter {
            eq("incident_id", incidentId)
        }
    }
}

// Searches incidents by title or description
suspend fun searchIncidents(title: String): List<Incident> {
    val incidents = supabase.from("incidents").select() {
        filter {
            or {
                ilike("title", "%$title%")
                ilike("description", "%$title%")
            }
        }
    }.decodeList<Incident>()
    return incidents
}

// Inserts a new user
suspend fun insertUser(userId: Int,
                       name: String,
                       lastName: String,
                       email: String,
                       password: String,
                       faculty: String,
                       role: String,
                       jurisdiction: String){
    val user = User(userId, name, lastName, email, password, faculty, role, jurisdiction)
    try {
        supabase.from("users").insert(user)
        println("User successfully inserted!")
    } catch (e: Exception) {
        println("Error inserting user: ${e.message}")
        throw e
    }
}

// Gets all users
suspend fun getAllUsers() {
    val users = supabase.from("users").select().decodeList<User>()
    for (user in users) {
        println(user)
    }
}
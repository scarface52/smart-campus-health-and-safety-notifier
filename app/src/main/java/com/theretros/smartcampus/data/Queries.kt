package com.theretros.smartcampus.data

import android.content.ContentResolver
import android.net.Uri
import com.theretros.smartcampus.data.dataclasses.Email
import com.theretros.smartcampus.data.dataclasses.FollowedIncident
import com.theretros.smartcampus.data.dataclasses.FollowedIncidentClass
import com.theretros.smartcampus.data.dataclasses.ImageUrl
import com.theretros.smartcampus.data.dataclasses.Incident
import com.theretros.smartcampus.data.dataclasses.IncidentClassId
import com.theretros.smartcampus.data.dataclasses.IncidentDetail
import com.theretros.smartcampus.data.dataclasses.IncidentId
import com.theretros.smartcampus.data.dataclasses.IncidentImage
import com.theretros.smartcampus.data.dataclasses.IncidentMapInfo
import com.theretros.smartcampus.data.dataclasses.IncidentSummary
import com.theretros.smartcampus.data.dataclasses.IncidentWithFollow
import com.theretros.smartcampus.data.dataclasses.IncidentWithFollowDto
import com.theretros.smartcampus.data.dataclasses.User
import com.theretros.smartcampus.data.dataclasses.UserId
import com.theretros.smartcampus.data.dataclasses.UserInfo
import com.theretros.smartcampus.data.dataclasses.UserSessionCredentials
import com.theretros.smartcampus.data.dataclasses.toDomain
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import kotlin.collections.toMutableSet
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

val classes = mapOf(
    1 to "Health",
    2 to "Safety",
    3 to "Environmental",
    4 to "Lost and Found",
    5 to "Maintenance"
)

// Inserts a new incident
@OptIn(ExperimentalTime::class)
suspend fun insertIncident(
    title: String,
    description: String,
    reportTime: Instant,
    location: String?,
    classId: Int,
    ownerId: Int,
    status: String): Int {
    val incident = Incident(title, description, reportTime, location, classId, ownerId, status)
    try {
        return supabase.from("incidents").insert(incident) {
            select(
                columns = Columns.list(
                    "incident_id"
                )
            )
        }.decodeSingle<IncidentId>().incident_id

        println("Incident successfully inserted!")
    } catch (e: Exception) {
        println("Error inserting incident: ${e.message}")
        throw e
    }
}

// Gets followed incidents from a user id with class id and status filters, ordered by date
suspend fun getFollowedIncidents(userId: Int): List<IncidentSummary> {
    val followedIncidents = supabase.from("followed_incidents").select(
        columns = Columns.list(
            "incident_id"
        )
    ) {
        filter {
            eq("user_id", userId)
        }
    }.decodeList<Map<String, Int>>().map { it["incident_id"]!! }

    if (followedIncidents.isEmpty()) return emptyList()

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
        order(column = "report_time", order = Order.DESCENDING)
        filter {
            isIn("incident_id", followedIncidents)
        }
    }.decodeList<IncidentSummary>()

    return incidents
}

// Gets incidents with follow status from a user id with class id and status filters, ordered by date
suspend fun getIncidentsWithFollowStatus(userId: Int, classId: Int? = null, status: String? = null): List<IncidentWithFollow> {
    val incidentsWithFollow = supabase.from("incidents").select(
            Columns.raw("""
                incident_id,
                title,
                description,
                report_time,
                class_id,
                status,
                followed_incidents (
                    user_id
                )
            """)
        ) {
            order(column = "report_time", order = Order.DESCENDING)
            filter {
                    if (classId != null) eq("class_id", classId)
                    if (status != null) eq("status", status)
                }
            }.decodeList<IncidentWithFollowDto>().map { it.toDomain() }

    return incidentsWithFollow
}

// Gets incident information for the map screen
suspend fun getIncidentMapInfo(): List<IncidentMapInfo> {
    val incidentMapInfo = supabase.from("incidents").select(
        columns = Columns.list(
            "location",
            "class_id",
            "incident_id"
        )
    ).decodeList<IncidentMapInfo>()
    println("Map info size: ${incidentMapInfo.size}")
    return incidentMapInfo
}

// Gets incident details from an incident id
suspend fun getIncidentDetails(incidentId: Int): IncidentDetail {
    try {
        return supabase.from("incidents").select(
            columns = Columns.raw("""
            title,
            description,
            report_time,
            location,
            class_id,
            status,
            owner_id,
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
    }
    catch (e: Exception) {
        println("Error getting incident details: ${e.message}")
        throw e
    }
}

// Follows an incident if not already followed (database constraint exists)
suspend fun followIncident(userId: Int, incidentId: Int) {
    val followedIncident = FollowedIncident(userId, incidentId)
    supabase.from("followed_incidents").upsert(followedIncident)
}

// Follows an incident class if not already followed (database constraint exists)
suspend fun followIncidentClass(userId: Int, classId: Int) {
    val followedIncidentClass = FollowedIncidentClass(userId, classId)
    supabase.from("followed_incident_classes").upsert(followedIncidentClass)
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

// Unfollows an incident class
suspend fun unfollowIncidentClass(userId: Int, classId: Int) {
    supabase.from("followed_incident_classes").delete {
        filter {
            eq("user_id", userId)
            eq("class_id", classId)
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

// Searches incidents by title and description
suspend fun searchIncidents(title: String): List<IncidentWithFollow> {
    val incidents = supabase.from("incidents").select(
        Columns.raw("""
                incident_id,
                title,
                description,
                report_time,
                class_id,
                status,
                followed_incidents (
                    user_id
                )
            """)
    ) {
        filter {
            or {
                ilike("title", "%$title%")
                ilike("description", "%$title%")
            }
        }
    }.decodeList<IncidentWithFollowDto>().map { it.toDomain() }
    return incidents
}

// Inserts a new user
suspend fun insertUser(name: String,
                       lastName: String,
                       email: String,
                       password: String,
                       faculty: String,
                       role: String,
                       jurisdiction: String): Int {
    val user = User(name, lastName, email, password, faculty, role, jurisdiction)
    try {
        val userId = supabase.from("users")
            .insert(user) {
                select(
                    columns = Columns.list(
                        "user_id"
                    )
                )
            }
            .decodeSingle<UserId>()
        println("User successfully inserted!")
        return userId.user_id
    } catch (e: Exception) {
        println("Error inserting user: ${e.message}")
        throw e
    }
}

// Update user information
suspend fun updateUserPassword(user_id: String, password: String) {


    supabase.from("users").update (
        {
            set("password", password)
        }
    ) {
        filter {
            eq("user_id", user_id)
        }
    }
}

// Returns userId if login info is correct, 0 otherwise
suspend fun checkLoginInfo(email: String, password: String?): UserSessionCredentials {
    try {
        println(email)
        println(password)

        val result = supabase.from("users").select(
            columns = Columns.list(
                "user_id",
                            "role",
                            "email",
                            "password"
            )
        ) {

        }.decodeList<UserSessionCredentials>()
        result.forEach {
            print(it)
            if (it.email == email && it.password == password)
                return it
            else if (it.email == email && password == null)
                return it
        }
        return UserSessionCredentials(0, "", "", "")

    } catch (e: Exception) {
        println("message: ${e.message}")
        return UserSessionCredentials(0, "", "", "")
    }


}

// Gets user information from user id
suspend fun getUserInfo(userId: Int): UserInfo {
    val userInfo = supabase.from("users").select(
        columns = Columns.list(
            "name",
            "last_name",
            "email",
            "role",
            "faculty"
        )
    ) {
        filter {
            eq("user_id", userId)
        }
    }.decodeSingle<UserInfo>()

    return userInfo
}

// Gets followed incident classes from a user id
suspend fun getFollowedIncidentClasses(userId: Int): MutableSet<Int> {
    val followedIncidentClasses = supabase.from("followed_incident_classes").select(
        columns = Columns.list(
            "class_id"
        )
    ){
        filter {
            eq("user_id", userId)
        }
    }.decodeList<IncidentClassId>().map { it.class_id }

    return followedIncidentClasses.toMutableSet()
}

// Checks if user with email exists
suspend fun checkUserExists(email: String): Boolean {
    val userId = supabase.from("users").select(
        columns = Columns.list(
            "user_id"
        )
    ) {
        filter {
            eq("email", email)
        }
    }.decodeList<Email>()

    return userId.isNotEmpty()
}

// Checks if an incident is followed by user
suspend fun isIncidentFollowed(incidentId: Int, userId: Int): Boolean {
    val result = supabase
        .from("followed_incidents")
        .select(columns = Columns.list("incident_id")) {
            filter {
                eq("incident_id", incidentId)
                eq("user_id", userId)
            }
            limit(1)
        }
        .decodeList<Map<String, Int>>()

    return result.isNotEmpty()
}

// Inserts an image to incident_images
suspend fun uploadImage(bucketName: String, uri: Uri, contentResolver: ContentResolver): String {
    return withContext(Dispatchers.IO) {
        val inputStream = contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes() ?: throw Exception("Could not read file")

        val fileName = "${java.util.UUID.randomUUID()}.jpg"
        val imagePath = fileName

        supabase.storage.from(bucketName).upload(imagePath, bytes)
        imagePath
    }
}

suspend fun insertImageUrl(incidentId: Int, imagePath: String) {
    val publicUrl = supabase.storage
        .from("incident-images")
        .publicUrl(imagePath)

    val incidentImage = IncidentImage(incidentId, publicUrl)
    supabase.from("incident_images").insert(incidentImage)
}

suspend fun getImages(incidentId: Int): List<ImageUrl> {
    return supabase.from("incident_images").select(
        columns = Columns.list("image_url")
    ) {
        filter {
            eq("incident_id", incidentId)
        }
    }.decodeList<ImageUrl>()
}
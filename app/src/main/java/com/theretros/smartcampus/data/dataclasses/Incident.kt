package com.theretros.smartcampus.data.dataclasses

import com.theretros.smartcampus.data.dataclasses.User
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class Incident(
    val id: Int,
    val title: String,
    val description: String?,
    @Serializable
    //val report_time:Date,
    val location: String,
    val type: String,
    val user: User, // (Reporter)
    val status: String,
)
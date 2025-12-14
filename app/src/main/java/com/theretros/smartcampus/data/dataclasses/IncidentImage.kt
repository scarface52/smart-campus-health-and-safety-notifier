package com.theretros.smartcampus.data.dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class IncidentImage(
    val image_id: Int,
    val incident_id: Int,
    val image_url: String
)
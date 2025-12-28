package com.theretros.smartcampus.data.dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class IncidentMapInfo(
    val incident_id: Int,
    val location: String,
    val class_id: Int
)
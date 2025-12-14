package com.theretros.smartcampus.data.dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class IncidentMapInfo(
    val location: String,
    val type: String
)
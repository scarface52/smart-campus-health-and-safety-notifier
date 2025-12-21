package com.theretros.smartcampus.data.dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class FollowedIncident(
    val user_id: Int,
    val incident_id: Int
)
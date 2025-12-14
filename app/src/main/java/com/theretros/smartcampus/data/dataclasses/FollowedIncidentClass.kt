package com.theretros.smartcampus.data.dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class FollowedIncidentClass(
    val user_id: Int,
    val class_id: Int
)
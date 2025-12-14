package com.theretros.smartcampus.data.dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class UserPersonalInfo(
    val name: String,
    val last_name: String,
    val email: String,
    val faculty: String,
    val role: String
)
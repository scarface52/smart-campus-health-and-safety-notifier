package com.theretros.smartcampus.data.dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class UserSessionCredentials(
    val user_id: Int,
    val role: String,
)